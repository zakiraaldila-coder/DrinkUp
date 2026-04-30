package com.example.drinkup

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class IntakeEntry(
    val id        : String = "",
    val amount    : Int    = 0,
    val timestamp : Long   = System.currentTimeMillis()
)

data class IntakeState(
    val todayTotal   : Int              = 0,
    val todayEntries : List<IntakeEntry> = emptyList(),
    val history      : Map<String, Int>  = emptyMap(),   // "MM-dd" -> total ml
    val streak       : Int              = 0,
    val lastDrinkAt  : Long?            = null           // timestamp terakhir minum
)

class IntakeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(IntakeState())
    val state: StateFlow<IntakeState> = _state.asStateFlow()

    private var listenerReg: ListenerRegistration? = null

    private val todayKey: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val historyKey: String
        get() = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date())

    // ── Mulai listen data hari ini dari Firestore ─────────────────────────────
    fun startListening() {
        val uid = auth.currentUser?.uid ?: return
        listenerReg?.remove()

        // Listen entries hari ini secara realtime
        listenerReg = db.collection("users")
            .document(uid)
            .collection("intake")
            .document(todayKey)
            .collection("entries")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener

                val entries = snap.documents.map { doc ->
                    IntakeEntry(
                        id        = doc.id,
                        amount    = doc.getLong("amount")?.toInt() ?: 0,
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }.sortedBy { it.timestamp }

                val total      = entries.sumOf { it.amount }
                val lastDrink  = entries.lastOrNull()?.timestamp

                _state.value = _state.value.copy(
                    todayTotal   = total,
                    todayEntries = entries,
                    lastDrinkAt  = lastDrink
                )

                // Update summary harian di parent doc (untuk history)
                if (entries.isNotEmpty()) {
                    db.collection("users").document(uid)
                        .collection("intake").document(todayKey)
                        .set(mapOf("date" to todayKey, "total" to total))
                }
            }

        // Load history (semua hari) sekali
        loadHistory(uid)
    }

    private fun loadHistory(uid: String) {
        db.collection("users").document(uid)
            .collection("intake")
            .get()
            .addOnSuccessListener { snap ->
                val historyMap = mutableMapOf<String, Int>()
                var streak     = 0

                val sortedDates = snap.documents
                    .mapNotNull { doc ->
                        val date  = doc.getString("date") ?: return@mapNotNull null
                        val total = doc.getLong("total")?.toInt() ?: 0
                        date to total
                    }
                    .sortedByDescending { it.first }

                sortedDates.forEach { (dateStr, total) ->
                    // Convert "yyyy-MM-dd" ke "MM-dd" untuk history map
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        val mmdd = "${parts[1]}-${parts[2]}"
                        historyMap[mmdd] = total
                    }
                }

                // Hitung streak (hari berturut-turut dengan target tercapai)
                // Ambil target dari state user (default 2000)
                val cal = Calendar.getInstance()
                for (i in 0..30) {
                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    val dayTotal = sortedDates.find { it.first == key }?.second ?: 0
                    if (dayTotal >= 2000) streak++ else if (i > 0) break  // hari ini boleh belum selesai
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                _state.value = _state.value.copy(
                    history = historyMap,
                    streak  = streak
                )
            }
    }

    // ── Tambah intake baru ────────────────────────────────────────────────────
    fun addIntake(
        amount    : Int,
        onSuccess : () -> Unit = {},
        onError   : (String) -> Unit = {}
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("Belum login"); return }

        val entry = hashMapOf(
            "amount"    to amount,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .collection("intake").document(todayKey)
            .collection("entries")
            .add(entry)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal menyimpan") }
    }

    // ── Stop listener saat tidak dipakai ─────────────────────────────────────
    fun stopListening() {
        listenerReg?.remove()
        listenerReg = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
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
    val todayTotal       : Int               = 0,
    val todayEntries     : List<IntakeEntry> = emptyList(),
    val history          : Map<String, Int>  = emptyMap(),  // "MM-dd" -> total ml
    val weeklyHistory    : Map<String, Int>  = emptyMap(),  // "yyyy-MM-dd" -> total ml (7 hari terakhir)
    val streak           : Int               = 0,
    val bestStreak       : Int               = 0,
    val glassesThisMonth : Int               = 0,
    val avgPerDayLiter   : Float             = 0f,
    val lastDrinkAt      : Long?             = null,
    val userTarget       : Int               = 2000
)

class IntakeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(IntakeState())
    val state: StateFlow<IntakeState> = _state.asStateFlow()

    private var listenerReg     : ListenerRegistration? = null
    private var userListenerReg : ListenerRegistration? = null

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val todayKey: String
        get() = sdf.format(Date())

    // ── Mulai listen ──────────────────────────────────────────────────────────
    fun startListening() {
        val uid = auth.currentUser?.uid ?: return
        listenerReg?.remove()
        userListenerReg?.remove()

        // Listen target user (realtime)
        userListenerReg = db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val kebutuhan  = doc.getLong("kebutuhanAir")?.toInt()
                    val beratBadan = doc.getLong("beratBadan")?.toInt() ?: 0
                    val target     = kebutuhan ?: if (beratBadan > 0) beratBadan * 35 else 2000
                    if (target != _state.value.userTarget) {
                        _state.value = _state.value.copy(userTarget = target)
                        // Reload history karena target berubah (streak bisa berubah)
                        loadHistory(uid)
                    }
                }
            }

        // Listen entries hari ini (realtime)
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

                val total     = entries.sumOf { it.amount }
                val lastDrink = entries.lastOrNull()?.timestamp

                _state.value = _state.value.copy(
                    todayTotal   = total,
                    todayEntries = entries,
                    lastDrinkAt  = lastDrink
                )

                // Update weeklyHistory untuk hari ini secara realtime di state
                val updatedWeekly = _state.value.weeklyHistory.toMutableMap()
                updatedWeekly[todayKey] = total
                _state.value = _state.value.copy(weeklyHistory = updatedWeekly)

                // FIX: Simpan summary ke Firestore, lalu reload history SETELAH tersimpan
                // Ini menghilangkan race condition antara write dan read
                if (entries.isNotEmpty()) {
                    db.collection("users").document(uid)
                        .collection("intake").document(todayKey)
                        .set(mapOf("date" to todayKey, "total" to total))
                        .addOnSuccessListener {
                            loadHistory(uid)
                            loadWeeklyHistory(uid)
                        }
                } else {
                    // Hari baru / belum minum — tetap reload supaya streak konsisten
                    loadHistory(uid)
                }
            }

        // Load data awal
        loadHistory(uid)
        loadWeeklyHistory(uid)
    }

    // ── Load & hitung semua statistik dari history ────────────────────────────
    private fun loadHistory(uid: String) {
        db.collection("users").document(uid)
            .collection("intake")
            .get()
            .addOnSuccessListener { snap ->
                val historyMap = mutableMapOf<String, Int>()

                // Kumpulkan semua data dari Firestore
                val firestoreDates = snap.documents
                    .mapNotNull { doc ->
                        val date  = doc.getString("date") ?: return@mapNotNull null
                        val total = doc.getLong("total")?.toInt() ?: 0
                        date to total
                    }
                    .toMutableList()

                // FIX: Selalu gabungkan todayTotal dari state ke dalam perhitungan.
                // Ini memastikan hari ini selalu ikut dihitung meski summary
                // belum tersimpan ke Firestore (misalnya pertama kali minum hari ini).
                val todayFromState = _state.value.todayTotal
                val existingToday  = firestoreDates.find { it.first == todayKey }
                when {
                    existingToday == null && todayFromState > 0 -> {
                        firestoreDates.add(todayKey to todayFromState)
                    }
                    existingToday != null && todayFromState > existingToday.second -> {
                        firestoreDates.removeAll { it.first == todayKey }
                        firestoreDates.add(todayKey to todayFromState)
                    }
                }

                val sortedDates = firestoreDates.sortedByDescending { it.first }

                // Build history map ("MM-dd" -> total)
                sortedDates.forEach { (dateStr, total) ->
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        historyMap["${parts[1]}-${parts[2]}"] = total
                    }
                }

                val target = _state.value.userTarget.takeIf { it > 0 } ?: 2000

                // ── Hitung streak saat ini ────────────────────────────────────
                // Logika: mulai dari hari ini ke belakang.
                // Kalau hari ini belum capai target, tidak putus streak (boleh belum selesai).
                // Kalau kemarin atau sebelumnya tidak capai target, streak putus.
                var streak = 0
                val cal    = Calendar.getInstance()
                for (i in 0..365) {
                    val key      = sdf.format(cal.time)
                    val dayTotal = sortedDates.find { it.first == key }?.second ?: 0
                    when {
                        dayTotal >= target -> {
                            streak++
                        }
                        i == 0 -> {
                            // Hari ini belum capai target — lanjut cek kemarin
                            // (streak hari sebelumnya tetap valid)
                        }
                        else -> break // Hari sebelumnya tidak capai target → putus
                    }
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                // ── Hitung bestStreak ─────────────────────────────────────────
                var bestStreak  = 0
                var currentBest = 0
                var prevCal     : Calendar? = null
                val sdfParse    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                sortedDates.sortedBy { it.first }.forEach { (dateStr, total) ->
                    if (total >= target) {
                        val thisCal = Calendar.getInstance().apply {
                            time = sdfParse.parse(dateStr) ?: Date()
                        }
                        currentBest = if (prevCal == null) 1
                        else {
                            val diff = ((thisCal.timeInMillis - prevCal!!.timeInMillis) / 86_400_000L).toInt()
                            if (diff == 1) currentBest + 1 else 1
                        }
                        if (currentBest > bestStreak) bestStreak = currentBest
                        prevCal = thisCal
                    } else {
                        prevCal     = null
                        currentBest = 0
                    }
                }
                if (streak > bestStreak) bestStreak = streak

                // ── Gelas bulan ini ───────────────────────────────────────────
                val thisMonth        = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                val mlThisMonth      = sortedDates.filter { it.first.startsWith(thisMonth) }.sumOf { it.second }
                val glassesThisMonth = mlThisMonth / 250

                // ── Rata-rata per hari ────────────────────────────────────────
                val daysWithData   = sortedDates.count { it.second > 0 }
                val totalAllTime   = sortedDates.sumOf { it.second }
                val avgPerDayLiter = if (daysWithData > 0) {
                    val raw = totalAllTime.toFloat() / daysWithData / 1000f
                    (raw * 10).toInt() / 10f
                } else 0f

                _state.value = _state.value.copy(
                    history          = historyMap,
                    streak           = streak,
                    bestStreak       = bestStreak,
                    glassesThisMonth = glassesThisMonth,
                    avgPerDayLiter   = avgPerDayLiter
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

    // ── Load 7 hari terakhir ──────────────────────────────────────────────────
    private fun loadWeeklyHistory(uid: String) {
        val last7Days = (0..6).map { offset ->
            val c = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -offset) }
            sdf.format(c.time)
        }

        db.collection("users").document(uid)
            .collection("intake")
            .get()
            .addOnSuccessListener { snap ->
                val weeklyMap = mutableMapOf<String, Int>()
                snap.documents.forEach { doc ->
                    val date  = doc.getString("date") ?: return@forEach
                    val total = doc.getLong("total")?.toInt() ?: 0
                    if (date in last7Days) weeklyMap[date] = total
                }
                // Selalu pakai state untuk hari ini (paling fresh)
                weeklyMap[todayKey] = _state.value.todayTotal
                _state.value = _state.value.copy(weeklyHistory = weeklyMap)
            }
    }

    // ── Refresh hari ini di weeklyHistory ────────────────────────────────────
    fun refreshWeeklyToday() {
        val updated = _state.value.weeklyHistory.toMutableMap()
        updated[todayKey] = _state.value.todayTotal
        _state.value = _state.value.copy(weeklyHistory = updated)
    }

    // ── Stop listener ─────────────────────────────────────────────────────────
    fun stopListening() {
        listenerReg?.remove()
        userListenerReg?.remove()
        listenerReg     = null
        userListenerReg = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
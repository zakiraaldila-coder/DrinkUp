package com.example.drinkup

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserData(
    val uid         : String = "",
    val namaLengkap : String = "",
    val email       : String = "",
    val photoUrl    : String = "",
    val beratBadan  : Int    = 0,
    val gender      : String = "",
    val createdAt   : Long   = System.currentTimeMillis()
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()


    // ── StateFlow user data (realtime dari Firestore) ─────────────────────────
    private val _userData = MutableStateFlow(UserData())
    val userData: StateFlow<UserData> = _userData.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) startListeningUser(uid)
    }

    fun startListeningUser(uid: String) {
        db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _userData.value = UserData(
                        uid         = doc.getString("uid") ?: uid,
                        namaLengkap = doc.getString("namaLengkap") ?: "",
                        email       = doc.getString("email") ?: "",
                        photoUrl    = doc.getString("photoUrl") ?: "",
                        beratBadan  = doc.getLong("beratBadan")?.toInt() ?: 0,
                        gender      = doc.getString("gender") ?: ""
                    )
                }
            }
    }

    // ── Cek login ────────────────────────────────────────────────────────────
    fun isLoggedIn() = auth.currentUser != null

    // ── Register email/password ──────────────────────────────────────────────
    fun register(
        namaLengkap        : String,
        email              : String,
        password           : String,
        konfirmasiPassword : String,
        gender             : String,
        beratBadan         : Int = 0,
        onSuccess          : () -> Unit,
        onError            : (String) -> Unit
    ) {
        when {
            namaLengkap.isBlank() -> {
                onError("Nama tidak boleh kosong"); return
            }

            email.isBlank() -> {
                onError("Email tidak boleh kosong"); return
            }

            password.length < 6 -> {
                onError("Password minimal 6 karakter"); return
            }

            password != konfirmasiPassword -> {
                onError("Password tidak sama"); return
            }

            gender.isEmpty() -> {
                onError("Pilih jenis kelamin dulu"); return
            }
        }

        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->

                val user = result.user
                if (user == null) {
                    onError("User gagal dibuat")
                    return@addOnSuccessListener
                }

                val kebutuhanAir = beratBadan * 35

                val userData = hashMapOf(
                    "uid" to user.uid,
                    "namaLengkap" to namaLengkap.trim(),
                    "email" to email.trim(),
                    "gender" to gender,
                    "beratBadan" to beratBadan,
                    "kebutuhanAir" to kebutuhanAir,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .addOnSuccessListener {

                        auth.signOut()

                        onSuccess()
                    }
                    .addOnFailureListener {

                        onError("Gagal menyimpan data ke database")
                    }

            }
            .addOnFailureListener {
                onError(mapError(it.message))
            }
    }

    // ── Google Sign-In Step 1: buat intent ───────────────────────────────────
    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut() // force account picker tampil
        return client.signInIntent
    }

    // ── Google Sign-In Step 2: handle result ─────────────────────────────────
    // onNewUser  = user baru → arahkan ke CompleteProfile untuk isi gender
    // onOldUser  = user lama → langsung ke Dashboard
    fun handleGoogleSignInResult(
        data      : Intent?,
        onNewUser : () -> Unit,
        onOldUser : () -> Unit,
        onError   : (String) -> Unit
    ) {
        try {
            val task    = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: run { onError("Token Google tidak ditemukan"); return }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user      = result.user ?: return@addOnSuccessListener
                    val isNewUser = result.additionalUserInfo?.isNewUser == true

                    if (isNewUser) {
                        // Simpan data awal ke Firestore (gender masih kosong)
                        val userData = UserData(
                            uid         = user.uid,
                            namaLengkap = user.displayName ?: "",
                            email       = user.email ?: "",
                            photoUrl    = user.photoUrl?.toString() ?: "",
                            gender      = ""
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener { startListeningUser(user.uid); onNewUser() }   // → ke CompleteProfile
                            .addOnFailureListener { onError(it.message ?: "Error") }
                    } else {
                        // User lama — cek apakah gender sudah diisi
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                val gender = doc.getString("gender") ?: ""
                                startListeningUser(user.uid)
                                if (gender.isEmpty()) onNewUser()   // belum lengkap → CompleteProfile
                                else onOldUser()                    // sudah lengkap → Dashboard
                            }
                            .addOnFailureListener { startListeningUser(user.uid); onOldUser() }   // fallback langsung masuk
                    }
                }
                .addOnFailureListener { onError(mapError(it.message)) }

        } catch (e: ApiException) {
            if (e.statusCode == 12501) onError("Login Google dibatalkan")
            else onError("Google Sign-In gagal (kode: ${e.statusCode})")
        }
    }

    // ── Simpan gender setelah Google Sign-In ─────────────────────────────────
    fun saveGender(
        gender   : String,
        onSuccess: () -> Unit,
        onError  : (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run { onError("Tidak ada user aktif"); return }
        db.collection("users").document(uid)
            .update("gender", gender)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Gagal menyimpan gender") }
    }

    // ── Logout ───────────────────────────────────────────────────────────────
    fun logout() {
        auth.signOut()
    }

    // ── Helper error message ──────────────────────────────────────────────────
    private fun mapError(msg: String?) = when {
        msg == null                              -> "Terjadi kesalahan"
        msg.contains("email address is already") -> "Email sudah terdaftar, silakan login"
        msg.contains("password is invalid")      -> "Password salah"
        msg.contains("no user record")           -> "Email tidak terdaftar"
        msg.contains("badly formatted")          -> "Format email tidak valid"
        msg.contains("network error")            -> "Tidak ada koneksi internet"
        msg.contains("too-many-requests")        -> "Terlalu banyak percobaan, coba lagi nanti"
        msg.contains("supplied auth credential") -> "Email atau password salah"
        else                                     -> msg
    }
}
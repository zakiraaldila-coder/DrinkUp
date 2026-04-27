package com.example.drinkup

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val EP_NavyDark      = Color(0xFF0D1B4B)
private val EP_NavyMid       = Color(0xFF1A2F6B)
private val EP_TealAccent    = Color(0xFF00BFA5)
private val EP_BgGray        = Color(0xFFF2F4F8)
private val EP_CardWhite     = Color(0xFFFFFFFF)
private val EP_TextPrimary   = Color(0xFF0D1B4B)
private val EP_TextSecondary = Color(0xFF8A94A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val auth      = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val uid       = auth.currentUser?.uid

    var namaLengkap   by remember { mutableStateOf("") }
    var emailDisplay  by remember { mutableStateOf("") }
    var beratBadanStr by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("") }
    var isLoading     by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var snackMsg      by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Load data dari Firestore sekali
    LaunchedEffect(uid) {
        if (uid != null) {
            firestore.collection("users").document(uid)
                .addSnapshotListener { doc, _ ->
                    if (doc != null && doc.exists() && !isInitialized) {
                        namaLengkap   = doc.getString("namaLengkap") ?: ""
                        emailDisplay  = doc.getString("email") ?: auth.currentUser?.email ?: ""
                        beratBadanStr = doc.getLong("beratBadan")?.takeIf { it > 0 }?.toString() ?: ""
                        gender        = doc.getString("gender") ?: ""
                        isInitialized = true
                    }
                }
        }
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let { snackbarHostState.showSnackbar(it); snackMsg = null }
    }

    val beratBadan     = beratBadanStr.toIntOrNull() ?: 0
    val targetOtomatis = beratBadan * 35

    fun saveProfile() {
        if (namaLengkap.isBlank()) { snackMsg = "Nama tidak boleh kosong"; return }
        if (uid == null) { snackMsg = "Tidak ada user aktif"; return }
        isLoading = true
        firestore.collection("users").document(uid)
            .update(mapOf("namaLengkap" to namaLengkap.trim(), "beratBadan" to beratBadan))
            .addOnSuccessListener { isLoading = false; snackMsg = "Profil berhasil disimpan ✓"; onNavigateBack() }
            .addOnFailureListener { isLoading = false; snackMsg = "Gagal: ${it.message}" }
    }

    Scaffold(
        containerColor = EP_BgGray,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Top App Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EP_CardWhite)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = EP_TextPrimary)
                }
                Text("Edit Profil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EP_TextPrimary)
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(EP_BgGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = EP_TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Avatar berdasarkan gender ────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GenderAvatar(
                    gender   = gender,
                    size     = 110.dp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text     = when (gender.uppercase()) {
                    "L"  -> "♂ Laki-laki"
                    "P"  -> "♀ Perempuan"
                    else -> "—"
                },
                fontSize  = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color     = when (gender.uppercase()) {
                    "L"  -> Color(0xFF1565C0)
                    "P"  -> Color(0xFFAD1457)
                    else -> EP_TextSecondary
                },
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text          = "PREMIUM MEMBER",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = EP_TextSecondary,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Form ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                Text("Nama Lengkap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EP_TextPrimary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = namaLengkap,
                    onValueChange = { namaLengkap = it },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    placeholder   = { Text("Nama kamu", color = EP_TextSecondary) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = EP_TealAccent,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedContainerColor   = EP_CardWhite,
                        unfocusedContainerColor = EP_CardWhite
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                Text("Alamat Email", fontSize = 14.sp, color = EP_TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = emailDisplay,
                    onValueChange = {},
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    enabled       = false,
                    trailingIcon  = { Icon(Icons.Filled.Lock, null, tint = EP_TextSecondary, modifier = Modifier.size(18.dp)) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor     = Color.Transparent,
                        disabledContainerColor  = EP_CardWhite,
                        disabledTextColor       = EP_TextSecondary
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Berat Badan (kg)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EP_TextPrimary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = beratBadanStr,
                    onValueChange = { v -> if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder   = { Text("Contoh: 65", color = EP_TextSecondary) },
                    trailingIcon  = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EP_BgGray)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) { Text("kg", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EP_TextSecondary) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = EP_TealAccent,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedContainerColor   = EP_CardWhite,
                        unfocusedContainerColor = EP_CardWhite
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                // Kalkulasi Target Otomatis
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = Color(0xFFD6EEF8)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", fontSize = 18.sp, color = EP_NavyMid)
                            Spacer(Modifier.width(8.dp))
                            Text("Kalkulasi Target Otomatis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EP_NavyMid)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Target harian Anda disesuaikan secara dinamis berdasarkan berat badan saat ini.",
                            fontSize = 12.sp, color = EP_TextSecondary, lineHeight = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                            Text(
                                text       = if (beratBadan > 0) "%,d".format(targetOtomatis) else "—",
                                fontSize   = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = EP_NavyDark
                            )
                            if (beratBadan > 0) {
                                Spacer(Modifier.width(4.dp))
                                Column { Spacer(Modifier.height(16.dp)); Text("MILILITER / HARI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EP_TextSecondary, letterSpacing = 0.5.sp) }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick  = { saveProfile() },
                    enabled  = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(28.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = EP_NavyDark, contentColor = Color.White)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else { Text("✓ ", fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("Simpan Perubahan", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
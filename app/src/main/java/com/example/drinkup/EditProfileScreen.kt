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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

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

    LaunchedEffect(uid) {
        if (uid != null && !isInitialized) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
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
        val kebutuhanAir = beratBadan * 35
        firestore.collection("users").document(uid)
            .update(mapOf(
                "namaLengkap"  to namaLengkap.trim(),
                "beratBadan"   to beratBadan,
                "kebutuhanAir" to kebutuhanAir
            ))
            .addOnSuccessListener {
                isLoading = false
                onNavigateBack()
            }
            .addOnFailureListener { isLoading = false; snackMsg = "Gagal: ${it.message}" }
    }

    Scaffold(
        containerColor = colorScheme.background,
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
                    .background(colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Kembali", tint = colorScheme.onSurface)
                }
                Text("Edit Profil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Avatar berdasarkan gender ────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GenderAvatar(gender = gender, size = 110.dp)
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
                    else -> colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            Text(
                text          = "PREMIUM MEMBER",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Form ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                Text("Nama Lengkap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = namaLengkap,
                    onValueChange = { namaLengkap = it },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    placeholder   = { Text("Nama kamu", color = colorScheme.onSurfaceVariant) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = colorScheme.secondary,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedContainerColor   = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                Text("Alamat Email", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = emailDisplay,
                    onValueChange = {},
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    enabled       = false,
                    trailingIcon  = { Icon(Icons.Filled.Lock, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor     = Color.Transparent,
                        disabledContainerColor  = colorScheme.surface,
                        disabledTextColor       = colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(20.dp))

                Text("Berat Badan (kg)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = beratBadanStr,
                    onValueChange = { v -> if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder   = { Text("Contoh: 65", color = colorScheme.onSurfaceVariant) },
                    trailingIcon  = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) { Text("kg", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = colorScheme.secondary,
                        unfocusedBorderColor    = Color.Transparent,
                        focusedContainerColor   = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(20.dp))

                // Kalkulasi Target Otomatis
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = colorScheme.tertiaryContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", fontSize = 18.sp, color = colorScheme.onTertiaryContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Kalkulasi Target Otomatis",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Target harian Anda disesuaikan secara dinamis berdasarkan berat badan saat ini.",
                            fontSize = 12.sp,
                            color = colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text       = if (beratBadan > 0) "%,d".format(targetOtomatis) else "—",
                                fontSize   = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = colorScheme.onTertiaryContainer
                            )
                            if (beratBadan > 0) {
                                Spacer(Modifier.width(4.dp))
                                Column {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "MILILITER / HARI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                        letterSpacing = 0.5.sp
                                    )
                                }
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
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor   = colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("✓ ", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Simpan Perubahan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
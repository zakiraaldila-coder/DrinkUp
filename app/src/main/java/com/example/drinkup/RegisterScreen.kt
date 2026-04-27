package com.example.drinkup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val R_NavyDark      = Color(0xFF0D1B4B)
private val R_NavyMid       = Color(0xFF1A2F6B)
private val R_TealAccent    = Color(0xFF00BFA5)
private val R_BgGray        = Color(0xFFF2F4F8)
private val R_CardWhite     = Color(0xFFFFFFFF)
private val R_TextPrimary   = Color(0xFF0D1B4B)
private val R_TextSecondary = Color(0xFF8A94A6)
private val R_MaleBlue      = Color(0xFF1565C0)
private val R_FemalePink    = Color(0xFFAD1457)
private val R_HydroBlue     = Color(0xFFD6EEF8)  // warna card kalkulator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel    : AuthViewModel,
    onRegisterSuccess: () -> Unit = {},
    onLoginClick     : () -> Unit = {}
) {
    var nama               by remember { mutableStateOf("") }
    var email              by remember { mutableStateOf("") }
    var password           by remember { mutableStateOf("") }
    var konfirmasiPassword by remember { mutableStateOf("") }
    var gender             by remember { mutableStateOf("") }
    var beratBadanStr      by remember { mutableStateOf("") }   // ← BARU
    var showPassword       by remember { mutableStateOf(false) }
    var showKonfirmasi     by remember { mutableStateOf(false) }
    var errorMsg           by remember { mutableStateOf("") }
    var isLoading          by remember { mutableStateOf(false) }

    // Kalkulasi target hidrasi otomatis (30ml × berat badan)
    val beratBadan     = beratBadanStr.toIntOrNull() ?: 0
    val targetHidrasi  = beratBadan * 30   // ml/hari

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(R_BgGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Header navy gradient ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(listOf(R_NavyDark, R_NavyMid))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "DrinkUp",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle  = FontStyle.Italic,
                        color      = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text  = "Buat akun baru",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // ── Form card ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-24).dp),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = R_CardWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // ── Nama Lengkap ─────────────────────────────────────────
                    OutlinedTextField(
                        value         = nama,
                        onValueChange = { nama = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Nama Lengkap") },
                        leadingIcon   = { Icon(Icons.Filled.Person, null, tint = R_TealAccent) },
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = R_TealAccent,
                            unfocusedBorderColor = Color(0xFFDDE2EC)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Email ────────────────────────────────────────────────
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Alamat Email") },
                        leadingIcon   = { Icon(Icons.Filled.Email, null, tint = R_TealAccent) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape         = RoundedCornerShape(14.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = R_TealAccent,
                            unfocusedBorderColor = Color(0xFFDDE2EC)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Password ─────────────────────────────────────────────
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Password") },
                        leadingIcon   = { Icon(Icons.Filled.Lock, null, tint = R_TealAccent) },
                        trailingIcon  = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null, tint = R_TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = R_TealAccent,
                            unfocusedBorderColor = Color(0xFFDDE2EC)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // ── Konfirmasi Password ──────────────────────────────────
                    OutlinedTextField(
                        value         = konfirmasiPassword,
                        onValueChange = { konfirmasiPassword = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Konfirmasi Password") },
                        leadingIcon   = { Icon(Icons.Filled.Lock, null, tint = R_TealAccent) },
                        trailingIcon  = {
                            IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                                Icon(
                                    if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null, tint = R_TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = R_TealAccent,
                            unfocusedBorderColor = Color(0xFFDDE2EC)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(22.dp))

                    // ════════════════════════════════════════════════════════
                    // ── BERAT BADAN + KALKULATOR HIDRASI (BARU) ─────────────
                    // ════════════════════════════════════════════════════════

                    // Label section
                    Text(
                        text          = "BERAT BADAN",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = R_TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    // Input berat badan dengan badge "KG"
                    OutlinedTextField(
                        value         = beratBadanStr,
                        onValueChange = { v ->
                            if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v
                        },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Berat Badan (kg)") },
                        leadingIcon   = {
                            Icon(Icons.Rounded.MonitorWeight, null, tint = R_TealAccent)
                        },
                        trailingIcon  = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(R_BgGray)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "KG",
                                    fontSize      = 12.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = R_TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = R_TealAccent,
                            unfocusedBorderColor = Color(0xFFDDE2EC)
                        ),
                        singleLine = true
                    )

                    // Card kalkulator hidrasi — muncul animasi saat berat diisi
                    AnimatedVisibility(
                        visible = beratBadan > 0,
                        enter   = fadeIn() + slideInVertically(initialOffsetY = { -20 })
                    ) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(18.dp),
                                colors    = CardDefaults.cardColors(containerColor = R_HydroBlue),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    // Header kalkulator
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💧", fontSize = 16.sp)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Kalkulator Hidrasi",
                                            fontSize   = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = R_NavyMid
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    // Label BERAT BADAN (KG)
                                    Text(
                                        text          = "BERAT BADAN (KG)",
                                        fontSize      = 10.sp,
                                        fontWeight    = FontWeight.SemiBold,
                                        color         = R_TextSecondary,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(Modifier.height(4.dp))

                                    // Angka berat badan + badge KG
                                    Row(
                                        modifier          = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text       = "$beratBadan",
                                            fontSize   = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color      = R_NavyDark
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White.copy(alpha = 0.7f))
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                "KG",
                                                fontSize      = 12.sp,
                                                fontWeight    = FontWeight.Bold,
                                                color         = R_TextSecondary,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(10.dp))

                                    // Card TARGET HARIAN
                                    Card(
                                        modifier  = Modifier.fillMaxWidth(),
                                        shape     = RoundedCornerShape(14.dp),
                                        colors    = CardDefaults.cardColors(
                                            containerColor = Color(0xFFB3DFF0).copy(alpha = 0.6f)
                                        ),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        Column(
                                            modifier            = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text          = "TARGET HARIAN",
                                                fontSize      = 10.sp,
                                                fontWeight    = FontWeight.SemiBold,
                                                color         = R_NavyMid,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text       = "%,d".format(targetHidrasi),
                                                    fontSize   = 42.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color      = R_NavyDark
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text       = "ml",
                                                    fontSize   = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color      = R_NavyMid,
                                                    modifier   = Modifier.padding(bottom = 6.dp)
                                                )
                                            }
                                            Text(
                                                text      = "30ml × ${beratBadan}kg",
                                                fontSize  = 12.sp,
                                                color     = R_TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))

                    // ── JENIS KELAMIN ────────────────────────────────────────
                    Text(
                        text          = "JENIS KELAMIN",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = R_TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenderSelectCard(
                            modifier    = Modifier.weight(1f),
                            gender      = "L",
                            label       = "Laki-laki",
                            isSelected  = gender == "L",
                            activeColor = R_MaleBlue,
                            onClick     = { gender = "L" }
                        )
                        GenderSelectCard(
                            modifier    = Modifier.weight(1f),
                            gender      = "P",
                            label       = "Perempuan",
                            isSelected  = gender == "P",
                            activeColor = R_FemalePink,
                            onClick     = { gender = "P" }
                        )
                    }

                    // ── Error ────────────────────────────────────────────────
                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Text(
                                text     = errorMsg,
                                color    = Color(0xFFD32F2F),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Tombol Daftar ────────────────────────────────────────
                    Button(
                        onClick = {
                            errorMsg = ""
                            when {
                                nama.isBlank()                 -> errorMsg = "Nama tidak boleh kosong"
                                email.isBlank()                -> errorMsg = "Email tidak boleh kosong"
                                password.length < 6            -> errorMsg = "Password minimal 6 karakter"
                                password != konfirmasiPassword -> errorMsg = "Password tidak cocok"
                                beratBadan <= 0                -> errorMsg = "Masukkan berat badan kamu"
                                gender.isEmpty()               -> errorMsg = "Pilih jenis kelamin dulu"
                                else -> {
                                    isLoading = true
                                    authViewModel.register(
                                        namaLengkap        = nama.trim(),
                                        email              = email.trim(),
                                        password           = password,
                                        konfirmasiPassword = konfirmasiPassword,
                                        gender             = gender,
                                        beratBadan         = beratBadan,      // ← pass ke ViewModel
                                        onSuccess          = {
                                            isLoading = false
                                            onRegisterSuccess()
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMsg  = err
                                        }
                                    )
                                }
                            }
                        },
                        enabled  = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape    = RoundedCornerShape(27.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = R_NavyDark,
                            contentColor   = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Daftar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Link ke login ────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Sudah punya akun? ", fontSize = 13.sp, color = R_TextSecondary)
                        Text(
                            text       = "Masuk",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = R_TealAccent,
                            modifier   = Modifier.clickable { onLoginClick() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Gender Select Card ─────────────────────────────────────────────────────────
@Composable
fun GenderSelectCard(
    modifier    : Modifier,
    gender      : String,
    label       : String,
    isSelected  : Boolean,
    activeColor : Color,
    onClick     : () -> Unit
) {
    val borderColor = if (isSelected) activeColor else Color(0xFFDDE2EC)
    val bgColor     = if (isSelected) activeColor.copy(alpha = 0.07f) else Color(0xFFF8F9FC)

    Card(
        modifier  = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GenderAvatar(gender = gender, size = 64.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                text       = label,
                fontSize   = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = if (isSelected) activeColor else Color(0xFF8A94A6),
                textAlign  = TextAlign.Center
            )
            if (isSelected) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier         = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(activeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
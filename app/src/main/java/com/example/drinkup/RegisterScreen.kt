package com.example.drinkup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
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

// ── Warna tema navy konsisten ──────────────────────────────────────────────────
private val R_NavyDark   = Color(0xFF0A1F5C)
private val R_NavyCard   = Color(0xFF112870)
private val R_NavyDeep   = Color(0xFF0D2B6B)
private val R_NavyBrd    = Color(0xFF1E3FA0)
private val R_Teal       = Color(0xFF4FC3F7)
private val R_Gray       = Color(0xFF7A9CC5)
private val R_GrayDim    = Color(0xFF4A6B9A)
private val R_Error      = Color(0xFFF87171)
private val R_ErrorBg    = Color(0x997F1D1D)

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
    var beratBadanStr      by remember { mutableStateOf("") }
    var showPassword       by remember { mutableStateOf(false) }
    var showKonfirmasi     by remember { mutableStateOf(false) }
    var errorMsg           by remember { mutableStateOf("") }
    var isLoading          by remember { mutableStateOf(false) }

    // Kalkulasi target hidrasi otomatis (30ml × berat badan)
    val beratBadan    = beratBadanStr.toIntOrNull() ?: 0
    val targetHidrasi = beratBadan * 30

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1F5C), Color(0xFF0D3B8E), Color(0xFF0A1F5C))
                )
            )
    ) {
        // ── Dekorasi wave — kanan atas ────────────────────────────────────────
        NavyWaveDecoration(fromLeft = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Text(
                "DrinkUp",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle  = FontStyle.Italic,
                    textAlign  = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            // ── Form Card ─────────────────────────────────────────────────────
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = R_NavyCard),
                elevation = CardDefaults.cardElevation(0.dp),
                border    = BorderStroke(1.dp, R_NavyBrd)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {

                    // Judul
                    Text(
                        "Buat Akun Baru",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        "Mulailah perjalanan hidrasi Anda hari ini.",
                        style    = MaterialTheme.typography.bodySmall.copy(color = R_Gray),
                        modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                    )

                    // ── Nama Lengkap ──────────────────────────────────────────
                    RNavyFormLabel("Nama Lengkap")
                    OutlinedTextField(
                        value         = nama,
                        onValueChange = { nama = it },
                        modifier      = Modifier.fillMaxWidth().height(52.dp),
                        placeholder   = { Text("John Doe", color = R_GrayDim, fontSize = 14.sp) },
                        leadingIcon   = { Icon(Icons.Filled.Person, null, tint = R_Teal, modifier = Modifier.size(18.dp)) },
                        shape         = RoundedCornerShape(12.dp),
                        colors        = rNavyFieldColors(),
                        singleLine    = true,
                        textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Email ─────────────────────────────────────────────────
                    RNavyFormLabel("Email")
                    OutlinedTextField(
                        value           = email,
                        onValueChange   = { email = it },
                        modifier        = Modifier.fillMaxWidth().height(52.dp),
                        placeholder     = { Text("contoh@email.com", color = R_GrayDim, fontSize = 14.sp) },
                        leadingIcon     = { Icon(Icons.Filled.Email, null, tint = R_Teal, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = rNavyFieldColors(),
                        singleLine      = true,
                        textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Kata Sandi ────────────────────────────────────────────
                    RNavyFormLabel("Kata Sandi")
                    OutlinedTextField(
                        value                = password,
                        onValueChange        = { password = it },
                        modifier             = Modifier.fillMaxWidth().height(52.dp),
                        leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = R_Teal, modifier = Modifier.size(18.dp)) },
                        trailingIcon         = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null, tint = R_Gray, modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape                = RoundedCornerShape(12.dp),
                        colors               = rNavyFieldColors(),
                        singleLine           = true,
                        textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Konfirmasi Kata Sandi ─────────────────────────────────
                    RNavyFormLabel("Konfirmasi Kata Sandi")
                    OutlinedTextField(
                        value                = konfirmasiPassword,
                        onValueChange        = { konfirmasiPassword = it },
                        modifier             = Modifier.fillMaxWidth().height(52.dp),
                        leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = R_Teal, modifier = Modifier.size(18.dp)) },
                        trailingIcon         = {
                            IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                                Icon(
                                    if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null, tint = R_Gray, modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape                = RoundedCornerShape(12.dp),
                        colors               = rNavyFieldColors(),
                        singleLine           = true,
                        textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Kalkulator Hidrasi ────────────────────────────────────
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = R_NavyDeep),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border    = BorderStroke(1.dp, R_NavyBrd)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💧", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Kalkulator Hidrasi",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color      = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "BERAT BADAN (KG)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = R_Gray,
                                    letterSpacing = 0.8.sp,
                                    fontWeight    = FontWeight.SemiBold
                                )
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value         = beratBadanStr,
                                onValueChange = { v ->
                                    if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v
                                },
                                modifier      = Modifier.fillMaxWidth().height(52.dp),
                                placeholder   = {
                                    Text("70", color = R_GrayDim, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                },
                                leadingIcon   = {
                                    Icon(Icons.Rounded.MonitorWeight, null, tint = R_Teal, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon  = {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("KG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = R_Gray, letterSpacing = 1.sp)
                                    }
                                },
                                textStyle       = LocalTextStyle.current.copy(
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape           = RoundedCornerShape(12.dp),
                                colors          = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor      = R_Teal,
                                    unfocusedBorderColor    = Color.Transparent,
                                    focusedContainerColor   = R_NavyDark,
                                    unfocusedContainerColor = R_NavyDark,
                                    focusedTextColor        = Color.White,
                                    unfocusedTextColor      = Color.White,
                                    cursorColor             = R_Teal
                                ),
                                singleLine = true
                            )

                            // Target harian — muncul saat ada input
                            AnimatedVisibility(
                                visible = beratBadan > 0,
                                enter   = fadeIn() + slideInVertically(initialOffsetY = { -20 })
                            ) {
                                Column {
                                    Spacer(Modifier.height(10.dp))
                                    Card(
                                        modifier  = Modifier.fillMaxWidth(),
                                        shape     = RoundedCornerShape(12.dp),
                                        colors    = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.4f)),
                                        elevation = CardDefaults.cardElevation(0.dp)
                                    ) {
                                        Row(
                                            modifier              = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "TARGET HARIAN",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color         = R_Gray,
                                                    letterSpacing = 0.8.sp,
                                                    fontWeight    = FontWeight.SemiBold
                                                )
                                            )
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    "%,d".format(targetHidrasi),
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        color = Color.White, fontWeight = FontWeight.ExtraBold
                                                    )
                                                )
                                                Spacer(Modifier.width(3.dp))
                                                Text(
                                                    "ml",
                                                    style    = MaterialTheme.typography.bodyMedium.copy(color = R_Teal, fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // ── Jenis Kelamin — Toggle Pill menyatu ───────────────────
                    RNavyFormLabel("Jenis Kelamin")
                    Spacer(Modifier.height(8.dp))
                    GenderToggleRow(
                        selected = gender,
                        onSelect = { gender = it }
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Error ─────────────────────────────────────────────────
                    if (errorMsg.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = CardDefaults.cardColors(containerColor = R_ErrorBg)
                        ) {
                            Text(
                                errorMsg,
                                color    = R_Error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // ── Tombol Daftar ─────────────────────────────────────────
                    Button(
                        onClick = {
                            errorMsg = ""
                            when {
                                nama.isBlank()                         -> errorMsg = "Nama tidak boleh kosong"
                                email.isBlank()                        -> errorMsg = "Email tidak boleh kosong"
                                password.length < 6                    -> errorMsg = "Password minimal 6 karakter"
                                password != konfirmasiPassword         -> errorMsg = "Password tidak cocok"
                                beratBadan <= 0                        -> errorMsg = "Masukkan berat badan kamu"
                                gender.isEmpty()                       -> errorMsg = "Pilih jenis kelamin dulu"
                                else -> {
                                    isLoading = true
                                    authViewModel.register(
                                        namaLengkap        = nama.trim(),
                                        email              = email.trim(),
                                        password           = password,
                                        konfirmasiPassword = konfirmasiPassword,
                                        gender             = gender,
                                        beratBadan         = beratBadan,
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
                            containerColor = Color.White,
                            contentColor   = R_NavyDark
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = R_NavyDark,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── Link ke login ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Sudah punya akun? ", style = MaterialTheme.typography.bodySmall.copy(color = R_Gray))
                        Text(
                            "Masuk ke sini",
                            style    = MaterialTheme.typography.bodySmall.copy(
                                color      = R_Teal,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onLoginClick() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Terms ─────────────────────────────────────────────────────────
            Text(
                "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan dan Kebijakan Privasi DrinkUp.",
                style    = MaterialTheme.typography.bodySmall.copy(
                    color      = Color(0xFF7A9CC5),
                    textAlign  = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// GENDER TOGGLE — pill horizontal menyatu dalam satu bar
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun GenderToggleRow(
    selected : String,
    onSelect : (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D2B6B))
            .border(1.dp, Color(0xFF1E3FA0), RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            GenderToggleItem(
                modifier    = Modifier.weight(1f),
                label       = "♂  Laki-laki",
                isSelected  = selected == "L",
                activeColor = Color(0xFF4FC3F7),
                onClick     = { onSelect("L") }
            )
            GenderToggleItem(
                modifier    = Modifier.weight(1f),
                label       = "♀  Perempuan",
                isSelected  = selected == "P",
                activeColor = Color(0xFFEC407A),
                onClick     = { onSelect("P") }
            )
        }
    }
}

@Composable
private fun GenderToggleItem(
    modifier    : Modifier,
    label       : String,
    isSelected  : Boolean,
    activeColor : Color,
    onClick     : () -> Unit
) {
    val bg    = if (isSelected) activeColor.copy(alpha = 0.18f) else Color.Transparent
    val brd   = if (isSelected) activeColor else Color.Transparent
    val color = if (isSelected) activeColor else Color(0xFF7A9CC5)
    val fw    = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, brd, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color      = color,
                fontWeight = fw,
                fontSize   = 14.sp
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// GenderSelectCard — dipertahankan untuk kompatibilitas dengan LoginScreen
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun GenderSelectCard(
    modifier    : Modifier,
    gender      : String,
    label       : String,
    isSelected  : Boolean,
    activeColor : Color,
    onClick     : () -> Unit
) {
    val borderColor = if (isSelected) activeColor else Color(0xFF1E3FA0)
    val bgColor     = if (isSelected) activeColor.copy(alpha = 0.15f) else Color(0xFF0D2B6B)

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
                color      = if (isSelected) activeColor else Color(0xFF7A9CC5),
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

// ── Helper label ──────────────────────────────────────────────────────────────
@Composable
private fun RNavyFormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelMedium.copy(
            color      = Color(0xFF7A9CC5),
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rNavyFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = Color(0xFF4FC3F7),
    unfocusedBorderColor      = Color(0xFF1E3FA0),
    focusedContainerColor     = Color(0xFF0D2B6B),
    unfocusedContainerColor   = Color(0xFF0D2B6B),
    focusedTextColor          = Color.White,
    unfocusedTextColor        = Color.White,
    cursorColor               = Color(0xFF4FC3F7),
    focusedPlaceholderColor   = Color(0xFF4A6B9A),
    unfocusedPlaceholderColor = Color(0xFF4A6B9A)
)
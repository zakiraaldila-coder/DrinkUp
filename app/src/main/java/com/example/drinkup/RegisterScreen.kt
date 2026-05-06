package com.example.drinkup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
// POPPINS FONT
// ─────────────────────────────────────────────────────────────
private val RPoppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ─────────────────────────────────────────────────────────────
// WARNA TEMA GELAP — selaras WelcomeScreen
// ─────────────────────────────────────────────────────────────
private val RBgDeep    = Color(0xFF08112A)
private val RBgMid     = Color(0xFF0A1535)
private val RBgCard    = Color(0xFF0F1E3A)
private val RBgField   = Color(0xFF0D1A34)
private val RTeal      = Color(0xFF00D4AA)
private val RTealDim   = Color(0xFF00D4AA).copy(alpha = 0.18f)
private val RBorderOn  = Color(0xFF00D4AA).copy(alpha = 0.5f)
private val RBorderOff = Color(0xFF1A2E52)
private val RTextGray  = Color(0xFF8AAAC8)
private val RTextDim   = Color(0xFF3D5A80)
private val RErrorCol  = Color(0xFFF87171)
private val RErrorBg   = Color(0x557F1D1D)

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
    var setujuTerms        by remember { mutableStateOf(false) }

    val beratBadan    = beratBadanStr.toIntOrNull() ?: 0
    val targetHidrasi = beratBadan * 30

    // Fade-in animasi
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")
    LaunchedEffect(Unit) { visible = true }

    // Blob animasi
    val inf = rememberInfiniteTransition(label = "inf")
    val blobAnim by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blob"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(RBgDeep, RBgMid, Color(0xFF0D1A3E)))
            )
    ) {
        // ── Blob dekorasi background ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-90).dp, y = (-60).dp)
                .alpha(0.20f + blobAnim * 0.06f)
                .background(
                    Brush.radialGradient(listOf(RTeal, Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 80.dp)
                .alpha(0.12f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00BFFF), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .alpha(0.10f)
                .background(
                    Brush.radialGradient(listOf(RTeal, Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-80).dp)
                .alpha(0.08f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF7B2FBE), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RTealDim),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "D",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color      = RTeal,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = RPoppins
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "DrinkUp",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle  = FontStyle.Italic,
                        fontFamily = RPoppins,
                        textAlign  = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "Hidrasi Sehat, Hidup Optimal",
                style = MaterialTheme.typography.labelMedium.copy(
                    color         = RTeal.copy(alpha = 0.7f),
                    fontFamily    = RPoppins,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(Modifier.height(28.dp))

            // ── Form Card ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(RTeal.copy(alpha = 0.4f), RBorderOff, RBorderOff)),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(RBgCard)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {

                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier         = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RTealDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "REG",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color      = RTeal,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = RPoppins,
                                        fontSize   = 10.sp
                                    )
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Buat Akun Baru",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color      = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = RPoppins
                                    )
                                )
                                Text(
                                    "Mulailah perjalanan hidrasi hari ini.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color      = RTextGray,
                                        fontFamily = RPoppins
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = RTeal.copy(alpha = 0.15f), thickness = 1.dp)
                        Spacer(Modifier.height(18.dp))

                        // Nama Lengkap
                        RDarkFormLabel("NAMA LENGKAP")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value         = nama,
                            onValueChange = { nama = it },
                            modifier      = Modifier.fillMaxWidth().height(52.dp),
                            placeholder   = { Text("John Doe", color = RTextDim, fontSize = 14.sp, fontFamily = RPoppins) },
                            leadingIcon   = { Icon(Icons.Filled.Person, null, tint = RTeal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                            shape         = RoundedCornerShape(12.dp),
                            colors        = rDarkFieldColors(),
                            singleLine    = true,
                            textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = RPoppins)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Email
                        RDarkFormLabel("EMAIL")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value           = email,
                            onValueChange   = { email = it },
                            modifier        = Modifier.fillMaxWidth().height(52.dp),
                            placeholder     = { Text("contoh@email.com", color = RTextDim, fontSize = 14.sp, fontFamily = RPoppins) },
                            leadingIcon     = { Icon(Icons.Filled.Email, null, tint = RTeal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape           = RoundedCornerShape(12.dp),
                            colors          = rDarkFieldColors(),
                            singleLine      = true,
                            textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = RPoppins)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Kata Sandi
                        RDarkFormLabel("KATA SANDI")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value                = password,
                            onValueChange        = { password = it },
                            modifier             = Modifier.fillMaxWidth().height(52.dp),
                            leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = RTeal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                            trailingIcon         = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        null, tint = RTextGray, modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape                = RoundedCornerShape(12.dp),
                            colors               = rDarkFieldColors(),
                            singleLine           = true,
                            textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = RPoppins)
                        )

                        Spacer(Modifier.height(12.dp))

                        // Konfirmasi Kata Sandi
                        RDarkFormLabel("KONFIRMASI KATA SANDI")
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value                = konfirmasiPassword,
                            onValueChange        = { konfirmasiPassword = it },
                            modifier             = Modifier.fillMaxWidth().height(52.dp),
                            leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = RTeal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                            trailingIcon         = {
                                IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                                    Icon(
                                        if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        null, tint = RTextGray, modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape                = RoundedCornerShape(12.dp),
                            colors               = rDarkFieldColors(),
                            singleLine           = true,
                            textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = RPoppins)
                        )

                        Spacer(Modifier.height(16.dp))

                        // ── Kalkulator Hidrasi ────────────────────────────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(RTeal.copy(alpha = 0.12f), RBgField)
                                    )
                                )
                                .border(1.dp, RTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier         = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(RTealDim),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "H2O",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color      = RTeal,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = RPoppins,
                                                fontSize   = 9.sp
                                            )
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Kalkulator Hidrasi",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color      = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = RPoppins
                                        )
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(RTeal.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("AUTO", fontSize = 9.sp, color = RTeal,
                                            fontWeight = FontWeight.Bold, fontFamily = RPoppins, letterSpacing = 0.5.sp)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "BERAT BADAN (KG)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color         = RTextGray,
                                        letterSpacing = 0.8.sp,
                                        fontWeight    = FontWeight.SemiBold,
                                        fontFamily    = RPoppins
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
                                        Text("70", color = RTextDim, fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold, fontFamily = RPoppins)
                                    },
                                    leadingIcon   = {
                                        Icon(Icons.Rounded.MonitorWeight, null, tint = RTeal.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp))
                                    },
                                    trailingIcon  = {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(RTeal.copy(alpha = 0.12f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("KG", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                                color = RTeal, letterSpacing = 1.sp, fontFamily = RPoppins)
                                        }
                                    },
                                    textStyle       = LocalTextStyle.current.copy(
                                        fontSize   = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = Color.White,
                                        fontFamily = RPoppins
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape           = RoundedCornerShape(12.dp),
                                    colors          = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor      = RTeal,
                                        unfocusedBorderColor    = Color.Transparent,
                                        focusedContainerColor   = RBgField,
                                        unfocusedContainerColor = RBgField,
                                        focusedTextColor        = Color.White,
                                        unfocusedTextColor      = Color.White,
                                        cursorColor             = RTeal
                                    ),
                                    singleLine = true
                                )

                                AnimatedVisibility(
                                    visible = beratBadan > 0,
                                    enter   = fadeIn() + slideInVertically(initialOffsetY = { -20 })
                                ) {
                                    Column {
                                        Spacer(Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(RTeal.copy(alpha = 0.20f), RTeal.copy(alpha = 0.05f))
                                                    )
                                                )
                                                .border(1.dp, RTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier              = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment     = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        "TARGET HARIAN",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color         = RTextGray,
                                                            letterSpacing = 0.8.sp,
                                                            fontFamily    = RPoppins
                                                        )
                                                    )
                                                    Text(
                                                        "30 ml × $beratBadan kg",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color      = RTeal.copy(alpha = 0.6f),
                                                            fontFamily = RPoppins,
                                                            fontSize   = 10.sp
                                                        )
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        "%,d".format(targetHidrasi),
                                                        style = MaterialTheme.typography.titleLarge.copy(
                                                            color      = Color.White,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontFamily = RPoppins
                                                        )
                                                    )
                                                    Spacer(Modifier.width(3.dp))
                                                    Text(
                                                        "ml",
                                                        style    = MaterialTheme.typography.bodyMedium.copy(
                                                            color      = RTeal,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = RPoppins
                                                        ),
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

                        // ── Jenis Kelamin ─────────────────────────────────────
                        RDarkFormLabel("JENIS KELAMIN")
                        Spacer(Modifier.height(8.dp))
                        GenderToggleRow(selected = gender, onSelect = { gender = it })

                        Spacer(Modifier.height(16.dp))

                        // ── Checkbox Syarat ───────────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(RBgField)
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked         = setujuTerms,
                                onCheckedChange = { setujuTerms = it },
                                colors          = CheckboxDefaults.colors(
                                    checkedColor   = RTeal,
                                    uncheckedColor = RBorderOff
                                )
                            )
                            Text(
                                "Saya menyetujui Syarat & Ketentuan serta Kebijakan Privasi DrinkUp.",
                                style    = MaterialTheme.typography.bodySmall.copy(
                                    color      = RTextGray,
                                    lineHeight = 18.sp,
                                    fontFamily = RPoppins
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Error
                        if (errorMsg.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RErrorBg)
                                    .border(1.dp, RErrorCol.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("!", style = MaterialTheme.typography.labelLarge.copy(color = RErrorCol, fontWeight = FontWeight.ExtraBold, fontFamily = RPoppins))
                                    Spacer(Modifier.width(8.dp))
                                    Text(errorMsg, color = RErrorCol, fontSize = 13.sp, fontFamily = RPoppins)
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Tombol Daftar — gradient teal
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(27.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(RTeal, Color(0xFF00B894)))
                                )
                                .clickable {
                                    if (!isLoading) {
                                        errorMsg = ""
                                        when {
                                            nama.isBlank()                     -> errorMsg = "Nama tidak boleh kosong"
                                            email.isBlank()                    -> errorMsg = "Email tidak boleh kosong"
                                            password.length < 6                -> errorMsg = "Password minimal 6 karakter"
                                            password != konfirmasiPassword     -> errorMsg = "Password tidak cocok"
                                            beratBadan <= 0                    -> errorMsg = "Masukkan berat badan kamu"
                                            gender.isEmpty()                   -> errorMsg = "Pilih jenis kelamin dulu"
                                            !setujuTerms                       -> errorMsg = "Setujui syarat & ketentuan dulu"
                                            else -> {
                                                isLoading = true
                                                authViewModel.register(
                                                    namaLengkap        = nama.trim(),
                                                    email              = email.trim(),
                                                    password           = password,
                                                    konfirmasiPassword = konfirmasiPassword,
                                                    gender             = gender,
                                                    beratBadan         = beratBadan,
                                                    onSuccess          = { isLoading = false; onRegisterSuccess() },
                                                    onError            = { err -> isLoading = false; errorMsg = err }
                                                )
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = RBgDeep, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    "Daftar Sekarang →",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color      = RBgDeep,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = RPoppins
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Link ke login
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sudah punya akun? ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = RTextGray,
                                    fontFamily = RPoppins
                                )
                            )
                            Text(
                                "Masuk ke sini",
                                style    = MaterialTheme.typography.bodySmall.copy(
                                    color      = RTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RPoppins
                                ),
                                modifier = Modifier.clickable { onLoginClick() }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Terms ─────────────────────────────────────────────────────────
            Text(
                "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan dan Kebijakan Privasi DrinkUp.",
                style    = MaterialTheme.typography.bodySmall.copy(
                    color      = RTextGray.copy(alpha = 0.6f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 18.sp,
                    fontFamily = RPoppins
                ),
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helper label ──────────────────────────────────────────────────────────────
@Composable
private fun RDarkFormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelSmall.copy(
            color         = RTextGray,
            fontWeight    = FontWeight.SemiBold,
            fontFamily    = RPoppins,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun RNavyFormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelMedium.copy(
            color      = RTextGray,
            fontWeight = FontWeight.SemiBold,
            fontFamily = RPoppins
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rDarkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = RTeal.copy(alpha = 0.8f),
    unfocusedBorderColor      = RBorderOff,
    focusedContainerColor     = RBgField,
    unfocusedContainerColor   = RBgField,
    focusedTextColor          = Color.White,
    unfocusedTextColor        = Color.White,
    cursorColor               = RTeal,
    focusedPlaceholderColor   = RTextDim,
    unfocusedPlaceholderColor = RTextDim
)

// Tetap ada untuk kompatibilitas dengan kode lama
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rNavyFieldColors() = rDarkFieldColors()
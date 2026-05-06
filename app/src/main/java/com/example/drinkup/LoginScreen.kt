package com.example.drinkup

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size

// ─────────────────────────────────────────────────────────────
// POPPINS FONT
// ─────────────────────────────────────────────────────────────
private val Poppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ─────────────────────────────────────────────────────────────
// WARNA TEMA — selaras dengan WelcomeScreen
// ─────────────────────────────────────────────────────────────
private val BgDeep    = Color(0xFF08112A)
private val BgMid     = Color(0xFF0A1535)
private val BgCard    = Color(0xFF0F1E3A)
private val BgField   = Color(0xFF0D1A34)
private val Teal      = Color(0xFF00D4AA)
private val TealDim   = Color(0xFF00D4AA).copy(alpha = 0.18f)
private val BorderOn  = Color(0xFF00D4AA).copy(alpha = 0.6f)
private val BorderOff = Color(0xFF1A2E52)
private val TextGray  = Color(0xFF8AAAC8)
private val TextDim   = Color(0xFF3D5A80)
private val ErrorCol  = Color(0xFFF87171)
private val ErrorBg   = Color(0x557F1D1D)

// ─────────────────────────────────────────────────────────────
// TAB ENUM
// ─────────────────────────────────────────────────────────────
private enum class AuthTab { SIGN_IN, REGISTER }

// ════════════════════════════════════════════════════════════════════════════
// LOGIN SCREEN ROOT
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel    : AuthViewModel,
    initialTab       : String = "login",
    onLoginSuccess   : () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    onGoogleNewUser  : () -> Unit = {},
    onGoogleOldUser  : () -> Unit = {}
) {
    var activeTab by remember {
        mutableStateOf(if (initialTab == "register") AuthTab.REGISTER else AuthTab.SIGN_IN)
    }

    // Animasi fade-in masuk
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")
    LaunchedEffect(Unit) { visible = true }

    // Animasi blob background
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
                Brush.verticalGradient(listOf(BgDeep, BgMid, Color(0xFF0D1A3E)))
            )
    ) {
        // ── Blob dekorasi — identik dengan WelcomeScreen ──────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-90).dp, y = (-60).dp)
                .alpha(0.20f + blobAnim * 0.06f)
                .background(
                    Brush.radialGradient(listOf(Teal, Color.Transparent)),
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
                    Brush.radialGradient(listOf(Teal, Color.Transparent)),
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
            Spacer(Modifier.height(56.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TealDim),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "D",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color      = Teal,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = Poppins
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
                        fontFamily = Poppins,
                        textAlign  = TextAlign.Center
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            // Tagline kecil
            Text(
                "Hidrasi Sehat, Hidup Optimal",
                style = MaterialTheme.typography.labelMedium.copy(
                    color      = Teal.copy(alpha = 0.7f),
                    fontFamily = Poppins,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(Modifier.height(28.dp))

            // ── Tab Switcher ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BgCard)
                    .border(1.dp, BorderOff, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AuthTabButton(
                        text     = "Sign In",
                        selected = activeTab == AuthTab.SIGN_IN,
                        modifier = Modifier.weight(1f),
                        onClick  = { activeTab = AuthTab.SIGN_IN }
                    )
                    AuthTabButton(
                        text     = "Register",
                        selected = activeTab == AuthTab.REGISTER,
                        modifier = Modifier.weight(1f),
                        onClick  = { activeTab = AuthTab.REGISTER }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Konten tab ────────────────────────────────────────────────────
            AnimatedContent(
                targetState   = activeTab,
                transitionSpec = {
                    if (targetState == AuthTab.REGISTER) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "tab"
            ) { tab ->
                when (tab) {
                    AuthTab.SIGN_IN  -> SignInForm(authViewModel, onLoginSuccess, onGoogleNewUser, onGoogleOldUser)
                    AuthTab.REGISTER -> RegisterForm(authViewModel, onRegisterSuccess)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Terms ─────────────────────────────────────────────────────────
            Text(
                "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan dan Kebijakan Privasi DrinkUp.",
                style    = MaterialTheme.typography.bodySmall.copy(
                    color      = TextGray.copy(alpha = 0.6f),
                    textAlign  = TextAlign.Center,
                    lineHeight = 18.sp,
                    fontFamily = Poppins
                ),
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// TAB BUTTON
// ════════════════════════════════════════════════════════════════════════════
@Composable
private fun AuthTabButton(
    text     : String,
    selected : Boolean,
    modifier : Modifier,
    onClick  : () -> Unit
) {
    val bg    = if (selected) Brush.horizontalGradient(listOf(Teal.copy(alpha = 0.25f), Teal.copy(alpha = 0.12f)))
    else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    val color = if (selected) Teal           else TextGray
    val fw    = if (selected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier         = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(if (selected) Modifier.border(1.dp, Teal.copy(alpha = 0.4f), RoundedCornerShape(50)) else Modifier)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(
            color = color, fontWeight = fw, fontFamily = Poppins
        ))
    }
}

// ════════════════════════════════════════════════════════════════════════════
// SIGN IN FORM
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignInForm(
    authViewModel   : AuthViewModel,
    onLoginSuccess  : () -> Unit,
    onGoogleNewUser : () -> Unit = {},
    onGoogleOldUser : () -> Unit = {}
) {
    val context         = LocalContext.current
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var showPass        by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleGoogleSignInResult(
                data      = result.data,
                onNewUser = { onGoogleNewUser() },
                onOldUser = { onGoogleOldUser() },
                onError   = { err -> errorMsg = err }
            )
        } else {
            errorMsg = "Login Google dibatalkan"
        }
    }

    // Card dengan gradient border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Teal.copy(alpha = 0.15f), Color.Transparent, Color.Transparent)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(Teal.copy(alpha = 0.5f), BorderOff, BorderOff)),
                RoundedCornerShape(24.dp)
            )
    ) {
        // Inner background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // Header section dengan ikon dekoratif
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "SI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color      = Teal,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Poppins
                            )
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Selamat Datang!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color      = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Poppins
                            )
                        )
                        Text(
                            "Masuk untuk melanjutkan",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = TextGray,
                                fontFamily = Poppins
                            )
                        )
                    }
                }

                // Divider tipis teal
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Teal.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(Modifier.height(20.dp))

                // EMAIL ADDRESS
                DarkFormLabel("EMAIL ADDRESS")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it },
                    modifier        = Modifier.fillMaxWidth(),
                    placeholder     = {
                        Text("hello@drinkup.com", color = TextDim, fontFamily = Poppins, fontSize = 14.sp)
                    },
                    leadingIcon     = {
                        Icon(Icons.Filled.Email, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape           = RoundedCornerShape(14.dp),
                    colors          = darkFieldColors(),
                    singleLine      = true,
                    textStyle       = LocalTextStyle.current.copy(fontFamily = Poppins, fontSize = 14.sp)
                )

                Spacer(Modifier.height(16.dp))

                // PASSWORD
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    DarkFormLabel("PASSWORD")
                    Text(
                        "LUPA?",
                        style    = MaterialTheme.typography.labelSmall.copy(
                            color         = Teal,
                            letterSpacing = 1.sp,
                            fontWeight    = FontWeight.Bold,
                            fontFamily    = Poppins
                        ),
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it },
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon          = {
                        Icon(Icons.Filled.Lock, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon         = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = TextGray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape           = RoundedCornerShape(14.dp),
                    colors          = darkFieldColors(),
                    singleLine      = true,
                    textStyle       = LocalTextStyle.current.copy(fontFamily = Poppins, fontSize = 14.sp)
                )

                // Error
                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ErrorBg)
                            .border(1.dp, ErrorCol.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("!", style = MaterialTheme.typography.labelLarge.copy(color = ErrorCol, fontWeight = FontWeight.ExtraBold, fontFamily = Poppins))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMsg, color = ErrorCol, fontSize = 13.sp, fontFamily = Poppins)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Tombol Masuk — gradient teal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Teal, Color(0xFF00B894)))
                        )
                        .clickable {
                            if (!isLoading) {
                                errorMsg = ""
                                when {
                                    email.isBlank()    -> errorMsg = "Email tidak boleh kosong"
                                    password.isBlank() -> errorMsg = "Password tidak boleh kosong"
                                    else -> {
                                        isLoading = true
                                        FirebaseAuth.getInstance()
                                            .signInWithEmailAndPassword(email.trim(), password)
                                            .addOnSuccessListener { isLoading = false; onLoginSuccess() }
                                            .addOnFailureListener { isLoading = false; errorMsg = it.message ?: "Login gagal" }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = BgDeep, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text(
                            "Masuk ke DrinkUp",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color      = BgDeep,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Poppins
                            )
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// REGISTER FORM
// ════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    authViewModel    : AuthViewModel,
    onRegisterSuccess: () -> Unit
) {
    var nama           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var konfirmasi     by remember { mutableStateOf("") }
    var beratBadanStr  by remember { mutableStateOf("") }
    var gender         by remember { mutableStateOf("") }
    var showPass       by remember { mutableStateOf(false) }
    var showKonfirmasi by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var setujuTerms    by remember { mutableStateOf(false) }

    val beratBadan    = beratBadanStr.toIntOrNull() ?: 0
    val targetHidrasi = beratBadan * 30

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.linearGradient(listOf(Teal.copy(alpha = 0.4f), BorderOff, BorderOff)),
                RoundedCornerShape(24.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BgCard)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {

                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier         = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "REG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color      = Teal,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Poppins,
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
                                fontFamily = Poppins
                            )
                        )
                        Text(
                            "Mulailah perjalanan hidrasi hari ini.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = TextGray,
                                fontFamily = Poppins
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Teal.copy(alpha = 0.15f), thickness = 1.dp)
                Spacer(Modifier.height(18.dp))

                // Nama Lengkap
                DarkFormLabel("NAMA LENGKAP")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value         = nama,
                    onValueChange = { nama = it },
                    modifier      = Modifier.fillMaxWidth().height(52.dp),
                    placeholder   = { Text("John Doe", color = TextDim, fontSize = 14.sp, fontFamily = Poppins) },
                    leadingIcon   = { Icon(Icons.Filled.Person, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = darkFieldColors(),
                    singleLine    = true,
                    textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = Poppins)
                )

                Spacer(Modifier.height(12.dp))

                // Email
                DarkFormLabel("EMAIL")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value           = email,
                    onValueChange   = { email = it },
                    modifier        = Modifier.fillMaxWidth().height(52.dp),
                    placeholder     = { Text("contoh@email.com", color = TextDim, fontSize = 14.sp, fontFamily = Poppins) },
                    leadingIcon     = { Icon(Icons.Filled.Email, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape           = RoundedCornerShape(12.dp),
                    colors          = darkFieldColors(),
                    singleLine      = true,
                    textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = Poppins)
                )

                Spacer(Modifier.height(12.dp))

                // Kata Sandi
                DarkFormLabel("KATA SANDI")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it },
                    modifier             = Modifier.fillMaxWidth().height(52.dp),
                    leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                    trailingIcon         = {
                        IconButton(onClick = { showPass = !showPass }) {
                            Icon(
                                if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                null, tint = TextGray, modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    colors               = darkFieldColors(),
                    singleLine           = true,
                    textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = Poppins)
                )

                Spacer(Modifier.height(12.dp))

                // Konfirmasi Kata Sandi
                DarkFormLabel("KONFIRMASI KATA SANDI")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value                = konfirmasi,
                    onValueChange        = { konfirmasi = it },
                    modifier             = Modifier.fillMaxWidth().height(52.dp),
                    leadingIcon          = { Icon(Icons.Filled.Lock, null, tint = Teal.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) },
                    trailingIcon         = {
                        IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                            Icon(
                                if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                null, tint = TextGray, modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    colors               = darkFieldColors(),
                    singleLine           = true,
                    textStyle            = LocalTextStyle.current.copy(fontSize = 14.sp, fontFamily = Poppins)
                )

                Spacer(Modifier.height(16.dp))

                // ── Kalkulator Hidrasi ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Teal.copy(alpha = 0.12f), BgField)
                            )
                        )
                        .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier         = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(TealDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "H2O",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color      = Teal,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = Poppins,
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
                                    fontFamily = Poppins
                                )
                            )
                            Spacer(Modifier.weight(1f))
                            // Badge "AUTO"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Teal.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("AUTO", fontSize = 9.sp, color = Teal, fontWeight = FontWeight.Bold,
                                    fontFamily = Poppins, letterSpacing = 0.5.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "BERAT BADAN (KG)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = TextGray,
                                letterSpacing = 0.8.sp,
                                fontWeight    = FontWeight.SemiBold,
                                fontFamily    = Poppins
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
                                Text("70", color = TextDim, fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold, fontFamily = Poppins)
                            },
                            leadingIcon   = {
                                Icon(Icons.Rounded.MonitorWeight, null, tint = Teal.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp))
                            },
                            trailingIcon  = {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Teal.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("KG", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                        color = Teal, letterSpacing = 1.sp, fontFamily = Poppins)
                                }
                            },
                            textStyle       = LocalTextStyle.current.copy(
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Color.White,
                                fontFamily = Poppins
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape           = RoundedCornerShape(12.dp),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = Teal,
                                unfocusedBorderColor    = Color.Transparent,
                                focusedContainerColor   = BgField,
                                unfocusedContainerColor = BgField,
                                focusedTextColor        = Color.White,
                                unfocusedTextColor      = Color.White,
                                cursorColor             = Teal
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
                                                listOf(Teal.copy(alpha = 0.20f), Teal.copy(alpha = 0.05f))
                                            )
                                        )
                                        .border(1.dp, Teal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
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
                                                    color         = TextGray,
                                                    letterSpacing = 0.8.sp,
                                                    fontFamily    = Poppins
                                                )
                                            )
                                            Text(
                                                "30 ml × $beratBadan kg",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color      = Teal.copy(alpha = 0.6f),
                                                    fontFamily = Poppins,
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
                                                    fontFamily = Poppins
                                                )
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                "ml",
                                                style    = MaterialTheme.typography.bodyMedium.copy(
                                                    color      = Teal,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = Poppins
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

                // ── Jenis Kelamin ─────────────────────────────────────────────
                DarkFormLabel("JENIS KELAMIN")
                Spacer(Modifier.height(8.dp))
                GenderToggleRow(selected = gender, onSelect = { gender = it })

                Spacer(Modifier.height(16.dp))

                // ── Checkbox Syarat ───────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BgField)
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked         = setujuTerms,
                        onCheckedChange = { setujuTerms = it },
                        colors          = CheckboxDefaults.colors(
                            checkedColor   = Teal,
                            uncheckedColor = BorderOff
                        )
                    )
                    Text(
                        "Saya menyetujui Syarat & Ketentuan serta Kebijakan Privasi DrinkUp.",
                        style    = MaterialTheme.typography.bodySmall.copy(
                            color      = TextGray,
                            lineHeight = 18.sp,
                            fontFamily = Poppins
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
                            .background(ErrorBg)
                            .border(1.dp, ErrorCol.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("!", style = MaterialTheme.typography.labelLarge.copy(color = ErrorCol, fontWeight = FontWeight.ExtraBold, fontFamily = Poppins))
                            Spacer(Modifier.width(8.dp))
                            Text(errorMsg, color = ErrorCol, fontSize = 13.sp, fontFamily = Poppins)
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
                            Brush.horizontalGradient(listOf(Teal, Color(0xFF00B894)))
                        )
                        .clickable {
                            if (!isLoading) {
                                errorMsg = ""
                                when {
                                    nama.isBlank()         -> errorMsg = "Nama tidak boleh kosong"
                                    email.isBlank()        -> errorMsg = "Email tidak boleh kosong"
                                    password.length < 6    -> errorMsg = "Password minimal 6 karakter"
                                    password != konfirmasi -> errorMsg = "Password tidak cocok"
                                    beratBadan <= 0        -> errorMsg = "Masukkan berat badan kamu"
                                    gender.isEmpty()       -> errorMsg = "Pilih jenis kelamin dulu"
                                    !setujuTerms           -> errorMsg = "Setujui syarat & ketentuan dulu"
                                    else -> {
                                        isLoading = true
                                        authViewModel.register(
                                            namaLengkap        = nama.trim(),
                                            email              = email.trim(),
                                            password           = password,
                                            konfirmasiPassword = konfirmasi,
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
                        CircularProgressIndicator(color = BgDeep, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text(
                            "Daftar Sekarang →",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color      = BgDeep,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Poppins
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
                            color      = TextGray,
                            fontFamily = Poppins
                        )
                    )
                    Text(
                        "Masuk ke sini",
                        style    = MaterialTheme.typography.bodySmall.copy(
                            color      = Teal,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Poppins
                        ),
                        modifier = Modifier.clickable { /* handled by parent tab switcher */ }
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// GENDER TOGGLE ROW
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
            .background(BgField)
            .border(1.dp, BorderOff, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            GenderToggleItem(
                modifier    = Modifier.weight(1f),
                label       = "♂  Laki-laki",
                isSelected  = selected == "L",
                activeColor = Teal,
                onClick     = { onSelect("L") }
            )
            GenderToggleItem(
                modifier    = Modifier.weight(1f),
                label       = "♀  Perempuan",
                isSelected  = selected == "P",
                activeColor = Color(0xFFFF6B9D),
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
    val brd   = if (isSelected) activeColor.copy(alpha = 0.5f) else Color.Transparent
    val color = if (isSelected) activeColor else TextGray
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
                fontSize   = 14.sp,
                fontFamily = Poppins
            )
        )
    }
}

// ════════════════════════════════════════════════════════════════════════════
// GENDER SELECT CARD — untuk kompatibilitas
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
    val borderColor = if (isSelected) activeColor else BorderOff
    val bgColor     = if (isSelected) activeColor.copy(alpha = 0.12f) else BgField

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
                color      = if (isSelected) activeColor else TextGray,
                textAlign  = TextAlign.Center,
                fontFamily = Poppins
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

// ════════════════════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ════════════════════════════════════════════════════════════════════════════
@Composable
private fun DarkFormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelSmall.copy(
            color         = TextGray,
            fontWeight    = FontWeight.SemiBold,
            fontFamily    = Poppins,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun NavyFormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.labelMedium.copy(
            color         = TextGray,
            fontWeight    = FontWeight.SemiBold,
            fontFamily    = Poppins,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = Teal.copy(alpha = 0.8f),
    unfocusedBorderColor      = BorderOff,
    focusedContainerColor     = BgField,
    unfocusedContainerColor   = BgField,
    focusedTextColor          = Color.White,
    unfocusedTextColor        = Color.White,
    cursorColor               = Teal,
    focusedPlaceholderColor   = TextDim,
    unfocusedPlaceholderColor = TextDim
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun navyFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor        = Teal.copy(alpha = 0.8f),
    unfocusedBorderColor      = BorderOff,
    focusedContainerColor     = BgField,
    unfocusedContainerColor   = BgField,
    focusedTextColor          = Color.White,
    unfocusedTextColor        = Color.White,
    cursorColor               = Teal,
    focusedPlaceholderColor   = TextDim,
    unfocusedPlaceholderColor = TextDim
)

// ════════════════════════════════════════════════════════════════════════════
// NAVY WAVE DECORATION — tetap ada untuk kompatibilitas (tidak dipakai di sini)
// ════════════════════════════════════════════════════════════════════════════
@Composable
fun NavyWaveDecoration(fromLeft: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val w = size.width
        val h = size.height
        val path = Path()
        if (fromLeft) {
            path.moveTo(0f, 0f)
            path.lineTo(w * 0.72f, 0f)
            path.cubicTo(w * 0.85f, 0f, w * 0.55f, h * 0.55f, w * 0.30f, h * 0.75f)
            path.cubicTo(w * 0.12f, h * 0.88f, 0f, h * 0.70f, 0f, h * 0.60f)
            path.close()
        } else {
            path.moveTo(w, 0f)
            path.lineTo(w * 0.28f, 0f)
            path.cubicTo(w * 0.15f, 0f, w * 0.45f, h * 0.55f, w * 0.70f, h * 0.75f)
            path.cubicTo(w * 0.88f, h * 0.88f, w, h * 0.70f, w, h * 0.60f)
            path.close()
        }
        drawPath(path = path, color = Teal.copy(alpha = 0.08f), style = Fill)
    }
}
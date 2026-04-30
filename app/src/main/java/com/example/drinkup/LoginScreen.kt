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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

// ── Warna konsisten dengan app ──────────────────────────────────────────────
private val L_Navy      = Color(0xFF0D1B4B)
private val L_NavyMid   = Color(0xFF1A2F6B)
private val L_Teal      = Color(0xFF00BFA5)
private val L_BgLight   = Color(0xFFE8F4FC)
private val L_BgWhite   = Color(0xFFFFFFFF)
private val L_Gray      = Color(0xFF8A94A6)
private val L_BorderOff = Color(0xFFDDE2EC)
private val L_HydroBlue = Color(0xFFD6EEF8)
private val L_MaleBlue  = Color(0xFF1565C0)
private val L_FemPink   = Color(0xFFAD1457)
private val L_Error     = Color(0xFFD32F2F)
private val L_ErrorBg   = Color(0xFFFFEBEE)

// ── Tab enum ────────────────────────────────────────────────────────────────
private enum class AuthTab { SIGN_IN, REGISTER }

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

    // Background gradien seperti referensi (biru muda ke putih)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(L_BgLight, Color(0xFFF0F8FC), L_BgWhite)
                )
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // ── Logo DrinkUp ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💧", fontSize = 22.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "DrinkUp",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = L_Navy,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle  = FontStyle.Italic
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Tab Switcher: Sign In | Register ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFEEF3F8))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    AuthTabButton(
                        text      = "Sign In",
                        selected  = activeTab == AuthTab.SIGN_IN,
                        modifier  = Modifier.weight(1f),
                        onClick   = { activeTab = AuthTab.SIGN_IN }
                    )
                    AuthTabButton(
                        text      = "Register",
                        selected  = activeTab == AuthTab.REGISTER,
                        modifier  = Modifier.weight(1f),
                        onClick   = { activeTab = AuthTab.REGISTER }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Konten tab dengan animasi slide ──────────────────────────────
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
                "Dengan melanjutkan, kamu menyetujui Syarat & Ketentuan\ndan Kebijakan Privasi DrinkUp.",
                style     = MaterialTheme.typography.bodySmall.copy(
                    color     = L_Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Tab Button ───────────────────────────────────────────────────────────────
@Composable
private fun AuthTabButton(
    text     : String,
    selected : Boolean,
    modifier : Modifier,
    onClick  : () -> Unit
) {
    val bg    = if (selected) L_BgWhite else Color.Transparent
    val color = if (selected) L_Navy   else L_Gray
    val fw    = if (selected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier         = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(color = color, fontWeight = fw))
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

    // Google Sign-In launcher
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleGoogleSignInResult(
                data      = result.data,
                onNewUser = { onGoogleNewUser() },   // baru → isi gender dulu
                onOldUser = { onGoogleOldUser() },   // lama → langsung dashboard
                onError   = { err -> errorMsg = err }
            )
        } else {
            errorMsg = "Login Google dibatalkan"
        }
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = L_BgWhite),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // EMAIL ADDRESS
            Text(
                "EMAIL ADDRESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = L_Gray,
                    letterSpacing = 1.sp,
                    fontWeight    = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("hello@drinkup.com", color = L_Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape  = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = L_Teal,
                    unfocusedBorderColor = L_BorderOff
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // PASSWORD
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "PASSWORD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color         = L_Gray,
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.SemiBold
                    )
                )
                Text(
                    "FORGOT?",
                    style    = MaterialTheme.typography.labelSmall.copy(
                        color         = L_Teal,
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { /* TODO: lupa password */ }
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                modifier             = Modifier.fillMaxWidth(),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            null, tint = L_Gray
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape  = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = L_Teal,
                    unfocusedBorderColor = L_BorderOff
                ),
                singleLine = true
            )

            // Error
            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(containerColor = L_ErrorBg)
                ) {
                    Text(errorMsg, color = L_Error, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tombol login
            Button(
                onClick = {
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
                },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(27.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = L_Navy, contentColor = Color.White)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Continue to Hydrate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            // Divider OR JOIN WITH
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = L_BorderOff)
                Text(
                    " OR JOIN WITH ",
                    style = MaterialTheme.typography.labelSmall.copy(color = L_Gray, letterSpacing = 1.sp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = L_BorderOff)
            }

            Spacer(Modifier.height(12.dp))

            // Google button
            OutlinedButton(
                onClick  = {
                    errorMsg        = ""
                    isGoogleLoading = true
                    val intent = authViewModel.getGoogleSignInIntent(context)
                    googleLauncher.launch(intent)
                },
                enabled  = !isLoading && !isGoogleLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(25.dp),
                border   = BorderStroke(1.dp, L_BorderOff),
                colors   = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF5F5F5))
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        color       = Color(0xFF4285F4),
                        modifier    = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Menghubungkan...", fontSize = 14.sp, color = L_Gray)
                } else {
                    Text("G", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                    Spacer(Modifier.width(8.dp))
                    Text("Google", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = L_Navy)
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
    var nama               by remember { mutableStateOf("") }
    var email              by remember { mutableStateOf("") }
    var password           by remember { mutableStateOf("") }
    var konfirmasi         by remember { mutableStateOf("") }
    var beratBadanStr      by remember { mutableStateOf("") }
    var gender             by remember { mutableStateOf("") }
    var showPass           by remember { mutableStateOf(false) }
    var showKonfirmasi     by remember { mutableStateOf(false) }
    var errorMsg           by remember { mutableStateOf("") }
    var isLoading          by remember { mutableStateOf(false) }
    var setujuTerms        by remember { mutableStateOf(false) }

    val beratBadan    = beratBadanStr.toIntOrNull() ?: 0
    val targetHidrasi = beratBadan * 30

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = L_BgWhite),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Judul
            Text(
                "Buat Akun\nBaru",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = L_Navy,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 34.sp
                )
            )
            Text(
                "Mulailah perjalanan hidrasi Anda hari ini.",
                style = MaterialTheme.typography.bodySmall.copy(color = L_Gray),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Nama Lengkap
            FormLabel("Nama Lengkap")
            OutlinedTextField(
                value         = nama,
                onValueChange = { nama = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("John Doe", color = L_Gray) },
                shape         = RoundedCornerShape(14.dp),
                colors        = fieldColors(),
                singleLine    = true
            )

            Spacer(Modifier.height(14.dp))

            // Email
            FormLabel("Email")
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("contoh@email.com", color = L_Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape         = RoundedCornerShape(14.dp),
                colors        = fieldColors(),
                singleLine    = true
            )

            Spacer(Modifier.height(14.dp))

            // Kata Sandi
            FormLabel("Kata Sandi")
            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                modifier             = Modifier.fillMaxWidth(),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = L_Gray)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape           = RoundedCornerShape(14.dp),
                colors          = fieldColors(),
                singleLine      = true
            )

            Spacer(Modifier.height(14.dp))

            // Konfirmasi Kata Sandi
            FormLabel("Konfirmasi Kata Sandi")
            OutlinedTextField(
                value                = konfirmasi,
                onValueChange        = { konfirmasi = it },
                modifier             = Modifier.fillMaxWidth(),
                visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                        Icon(if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = L_Gray)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape           = RoundedCornerShape(14.dp),
                colors          = fieldColors(),
                singleLine      = true
            )

            Spacer(Modifier.height(20.dp))

            // ── Kalkulator Hidrasi ────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = L_HydroBlue),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Kalkulator Hidrasi",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color      = L_NavyMid,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Input berat badan
                    Text(
                        "BERAT BADAN (KG)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = L_Gray,
                            letterSpacing = 0.8.sp,
                            fontWeight    = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = beratBadanStr,
                        onValueChange = { v -> if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("70", color = L_Gray, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold) },
                        trailingIcon  = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "KG",
                                    fontSize      = 12.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = L_Gray,
                                    letterSpacing = 1.sp
                                )
                            }
                        },
                        textStyle       = LocalTextStyle.current.copy(
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = L_Navy
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape  = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = L_Teal,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedContainerColor   = L_BgWhite,
                            unfocusedContainerColor = L_BgWhite
                        ),
                        singleLine = true
                    )

                    // Target harian — muncul saat ada input
                    AnimatedVisibility(visible = beratBadan > 0) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(16.dp),
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
                                        "TARGET HARIAN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color         = L_NavyMid,
                                            letterSpacing = 1.sp,
                                            fontWeight    = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            "%,d".format(targetHidrasi),
                                            style = MaterialTheme.typography.displaySmall.copy(
                                                color      = L_Navy,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "ml",
                                            style    = MaterialTheme.typography.titleLarge.copy(
                                                color      = L_NavyMid,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Text(
                                        "30ml × ${beratBadan}kg",
                                        style = MaterialTheme.typography.bodySmall.copy(color = L_Gray)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Jenis Kelamin ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderSelectCard(
                    modifier    = Modifier.weight(1f),
                    gender      = "L",
                    label       = "Laki-laki",
                    isSelected  = gender == "L",
                    activeColor = L_MaleBlue,
                    onClick     = { gender = "L" }
                )
                GenderSelectCard(
                    modifier    = Modifier.weight(1f),
                    gender      = "P",
                    label       = "Perempuan",
                    isSelected  = gender == "P",
                    activeColor = L_FemPink,
                    onClick     = { gender = "P" }
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Checkbox Syarat & Ketentuan ───────────────────────────────────
            Row(
                verticalAlignment = Alignment.Top,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = setujuTerms,
                    onCheckedChange = { setujuTerms = it },
                    colors          = CheckboxDefaults.colors(checkedColor = L_Teal)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Saya menyetujui ",
                    style    = MaterialTheme.typography.bodySmall.copy(color = L_Gray),
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    "Syarat & Ketentuan",
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = L_Teal,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    " serta Kebijakan Privasi DrinkUp.",
                    style    = MaterialTheme.typography.bodySmall.copy(color = L_Gray),
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            // Error
            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(containerColor = L_ErrorBg)
                ) {
                    Text(errorMsg, color = L_Error, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tombol Daftar
            Button(
                onClick = {
                    errorMsg = ""
                    when {
                        nama.isBlank()        -> errorMsg = "Nama tidak boleh kosong"
                        email.isBlank()       -> errorMsg = "Email tidak boleh kosong"
                        password.length < 6   -> errorMsg = "Password minimal 6 karakter"
                        password != konfirmasi -> errorMsg = "Password tidak cocok"
                        beratBadan <= 0       -> errorMsg = "Masukkan berat badan kamu"
                        gender.isEmpty()      -> errorMsg = "Pilih jenis kelamin dulu"
                        !setujuTerms          -> errorMsg = "Setujui syarat & ketentuan dulu"
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
                },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(27.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = L_Navy, contentColor = Color.White)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            // Link "Sudah punya akun?"
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Sudah punya akun? ", style = MaterialTheme.typography.bodySmall.copy(color = L_Gray))
                Text(
                    "Masuk ke sini",
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = L_Teal,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { /* switching ke tab sign in — dihandle parent */ }
                )
            }
        }
    }
}

// ── Helper composable ────────────────────────────────────────────────────────
@Composable
private fun FormLabel(text: String) {
    Text(
        text,
        style    = MaterialTheme.typography.bodyMedium.copy(
            color      = L_Navy,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = L_Teal,
    unfocusedBorderColor = L_BorderOff
)
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

// ── Warna konsisten dengan tema navy app ─────────────────────────────────────
private val L_Navy      = Color(0xFF0A1F5C)
private val L_NavyMid   = Color(0xFF0D3B8E)
private val L_NavyCard  = Color(0xFF112870)
private val L_NavyBrd   = Color(0xFF1E3FA0)
private val L_Teal      = Color(0xFF4FC3F7)
private val L_TealDark  = Color(0xFF29B6F6)
private val L_BgLight   = Color(0xFF0A1F5C)
private val L_BgWhite   = Color(0xFF112870)
private val L_Gray      = Color(0xFFB0C4E8)
private val L_BorderOff = Color(0xFF1E3FA0)
private val L_HydroBlue = Color(0xFF0D3B8E)
private val L_MaleBlue  = Color(0xFF4FC3F7)
private val L_FemPink   = Color(0xFFFF6D00)
private val L_Error     = Color(0xFFF87171)
private val L_ErrorBg   = Color(0xFF7F1D1D)

// ── Tab enum ─────────────────────────────────────────────────────────────────
private enum class AuthTab { SIGN_IN, REGISTER }

// ════════════════════════════════════════════════════════════════════════════
// LOGIN SCREEN (root)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1F5C), Color(0xFF0D3B8E), Color(0xFF0A1F5C))
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

            // ── Logo DrinkUp — teks saja, tengah ─────────────────────────────
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

            // ── Tab Switcher ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0D2B6B))
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

            // ── Konten tab dengan animasi slide ───────────────────────────────
            AnimatedContent(
                targetState  = activeTab,
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
// TAB BUTTON
// ════════════════════════════════════════════════════════════════════════════
@Composable
private fun AuthTabButton(
    text     : String,
    selected : Boolean,
    modifier : Modifier,
    onClick  : () -> Unit
) {
    val bg    = if (selected) Color(0xFF1E3FA0) else Color.Transparent
    val color = if (selected) Color.White       else Color(0xFFB0C4E8)
    val fw    = if (selected) FontWeight.Bold   else FontWeight.Normal

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

    // Google Sign-In launcher — tetap ada meski tombol disembunyikan
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

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF112870)),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, Color(0xFF1E3FA0))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // EMAIL ADDRESS
            Text(
                "EMAIL ADDRESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = Color(0xFF7A9CC5),
                    letterSpacing = 1.sp,
                    fontWeight    = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it },
                modifier        = Modifier.fillMaxWidth(),
                placeholder     = { Text("hello@drinkup.com", color = Color(0xFF4A6B9A)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape           = RoundedCornerShape(14.dp),
                colors          = navyFieldColors(),
                singleLine      = true
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
                        color         = Color(0xFF7A9CC5),
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.SemiBold
                    )
                )
                Text(
                    "LUPA?",
                    style    = MaterialTheme.typography.labelSmall.copy(
                        color         = Color(0xFF4FC3F7),
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
                trailingIcon         = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF7A9CC5)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape           = RoundedCornerShape(14.dp),
                colors          = navyFieldColors(),
                singleLine      = true
            )

            // Error
            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.6f))
                ) {
                    Text(
                        errorMsg,
                        color    = Color(0xFFF87171),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tombol Masuk
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape    = RoundedCornerShape(27.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Color(0xFF0A1F5C)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color(0xFF0A1F5C),
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Masuk ke DrinkUp", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
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

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF112870)),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, Color(0xFF1E3FA0))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

            // Judul — satu baris, lebih kecil
            Text(
                "Buat Akun Baru",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                "Mulailah perjalanan hidrasi Anda hari ini.",
                style    = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7A9CC5)),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Nama Lengkap
            NavyFormLabel("Nama Lengkap")
            OutlinedTextField(
                value         = nama,
                onValueChange = { nama = it },
                modifier      = Modifier.fillMaxWidth().height(52.dp),
                placeholder   = { Text("John Doe", color = Color(0xFF4A6B9A), fontSize = 14.sp) },
                shape         = RoundedCornerShape(12.dp),
                colors        = navyFieldColors(),
                singleLine    = true,
                textStyle     = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(Modifier.height(10.dp))

            // Email
            NavyFormLabel("Email")
            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it },
                modifier        = Modifier.fillMaxWidth().height(52.dp),
                placeholder     = { Text("contoh@email.com", color = Color(0xFF4A6B9A), fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape           = RoundedCornerShape(12.dp),
                colors          = navyFieldColors(),
                singleLine      = true,
                textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(Modifier.height(10.dp))

            // Kata Sandi
            NavyFormLabel("Kata Sandi")
            OutlinedTextField(
                value                = password,
                onValueChange        = { password = it },
                modifier             = Modifier.fillMaxWidth().height(52.dp),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon         = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF7A9CC5)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape           = RoundedCornerShape(12.dp),
                colors          = navyFieldColors(),
                singleLine      = true,
                textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(Modifier.height(10.dp))

            // Konfirmasi Kata Sandi
            NavyFormLabel("Konfirmasi Kata Sandi")
            OutlinedTextField(
                value                = konfirmasi,
                onValueChange        = { konfirmasi = it },
                modifier             = Modifier.fillMaxWidth().height(52.dp),
                visualTransformation = if (showKonfirmasi) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon         = {
                    IconButton(onClick = { showKonfirmasi = !showKonfirmasi }) {
                        Icon(
                            if (showKonfirmasi) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF7A9CC5)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape           = RoundedCornerShape(12.dp),
                colors          = navyFieldColors(),
                singleLine      = true,
                textStyle       = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Spacer(Modifier.height(14.dp))

            // ── Kalkulator Hidrasi ────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFF0D2B6B)),
                elevation = CardDefaults.cardElevation(0.dp),
                border    = BorderStroke(1.dp, Color(0xFF1E3FA0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", fontSize = 14.sp)
                        Spacer(Modifier.width(5.dp))
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
                            color         = Color(0xFF7A9CC5),
                            letterSpacing = 0.8.sp,
                            fontWeight    = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value         = beratBadanStr,
                        onValueChange = { v -> if (v.length <= 3 && v.all { it.isDigit() }) beratBadanStr = v },
                        modifier      = Modifier.fillMaxWidth().height(52.dp),
                        placeholder   = {
                            Text("70", color = Color(0xFF4A6B9A), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        },
                        trailingIcon  = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("KG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A9CC5), letterSpacing = 1.sp)
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
                            focusedBorderColor      = Color(0xFF4FC3F7),
                            unfocusedBorderColor    = Color.Transparent,
                            focusedContainerColor   = Color(0xFF0A1F5C),
                            unfocusedContainerColor = Color(0xFF0A1F5C),
                            focusedTextColor        = Color.White,
                            unfocusedTextColor      = Color.White,
                            cursorColor             = Color(0xFF4FC3F7)
                        ),
                        singleLine = true
                    )

                    // Target harian — muncul saat ada input
                    AnimatedVisibility(visible = beratBadan > 0) {
                        Column {
                            Spacer(Modifier.height(10.dp))
                            Card(
                                modifier  = Modifier.fillMaxWidth(),
                                shape     = RoundedCornerShape(12.dp),
                                colors    = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier          = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "TARGET HARIAN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF7A9CC5), letterSpacing = 0.8.sp, fontWeight = FontWeight.SemiBold
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
                                            style    = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
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
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = setujuTerms,
                    onCheckedChange = { setujuTerms = it },
                    colors          = CheckboxDefaults.colors(
                        checkedColor   = Color(0xFF4FC3F7),
                        uncheckedColor = Color(0xFF1E3FA0)
                    )
                )
                Text(
                    "Saya menyetujui Syarat & Ketentuan serta Kebijakan Privasi DrinkUp.",
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = Color(0xFF7A9CC5),
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Error
            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color(0xFF7F1D1D).copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        errorMsg,
                        color    = Color(0xFFF87171),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Tombol Daftar
            Button(
                onClick = {
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
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape    = RoundedCornerShape(27.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Color(0xFF0A1F5C)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color(0xFF0A1F5C),
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Link "Sudah punya akun?"
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Sudah punya akun? ",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7A9CC5))
                )
                Text(
                    "Masuk ke sini",
                    style    = MaterialTheme.typography.bodySmall.copy(
                        color      = Color(0xFF4FC3F7),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { /* handled by parent tab switcher */ }
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// HELPER COMPOSABLES — top-level agar bisa diakses semua fungsi di atas
// ════════════════════════════════════════════════════════════════════════════

@Composable
private fun NavyFormLabel(text: String) {
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
private fun navyFieldColors() = OutlinedTextFieldDefaults.colors(
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
package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Poppins ───────────────────────────────────────────────────────────────────
private val EPoppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ── Palette ───────────────────────────────────────────────────────────────────
private val EBgDeep       = Color(0xFF060D1F)
private val EBgMid        = Color(0xFF0A1428)
private val ENavyCard     = Color(0xFF0D1E38)
private val ENavyCardAlt  = Color(0xFF112244)
private val ETealPrimary  = Color(0xFF00D4AA)
private val ECyanAccent   = Color(0xFF00BFFF)
private val ETextWhite    = Color(0xFFFFFFFF)
private val ETextSub      = Color(0xFF7A9BBF)
private val ETextMuted    = Color(0xFF3D5A7A)
private val EBorderSubtle = Color(0xFF162845)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {}
) {
    val auth      = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // FIX 1: Ambil uid sekali, jika null tampilkan pesan dan keluar lebih awal
    val uid = auth.currentUser?.uid

    // FIX 2: Tampilkan layar fallback jika user tidak login, hindari crash
    if (uid == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EBgDeep),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "Sesi tidak ditemukan.\nSilakan login ulang.",
                color      = ETextSub,
                fontFamily = EPoppins,
                textAlign  = TextAlign.Center,
                fontSize   = 15.sp
            )
        }
        return
    }

    var namaLengkap   by remember { mutableStateOf("") }
    var emailDisplay  by remember { mutableStateOf("") }
    var beratBadanStr by remember { mutableStateOf("") }
    var gender        by remember { mutableStateOf("") }
    var isLoading     by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var snackMsg      by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // FIX 3: Gunakan uid (String) bukan nullable uid? sebagai key,
    // sehingga LaunchedEffect stabil dan tidak re-trigger tanpa sebab
    LaunchedEffect(uid) {
        if (!isInitialized) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    // FIX 4: Pastikan doc valid sebelum akses field-nya
                    if (doc != null && doc.exists()) {
                        namaLengkap  = doc.getString("namaLengkap") ?: ""
                        emailDisplay = doc.getString("email")
                            ?: auth.currentUser?.email
                                    ?: ""
                        val bb = doc.get("beratBadan")
                        beratBadanStr = when (bb) {
                            is Long   -> bb.toString()
                            is Double -> bb.toInt().toString()
                            is String -> bb
                            else      -> ""
                        }
                        gender        = doc.getString("gender") ?: ""
                        isInitialized = true
                    } else {
                        // Dokumen tidak ada — tetap tandai sebagai initialized
                        // agar tidak loop terus, dan isi email dari auth
                        emailDisplay  = auth.currentUser?.email ?: ""
                        isInitialized = true
                    }
                }
                .addOnFailureListener { e ->
                    // FIX 5: Handle Firestore error dengan baik, jangan biarkan crash
                    snackMsg      = "Gagal memuat profil: ${e.message}"
                    isInitialized = true
                }
        }
    }

    // FIX 6: LaunchedEffect untuk snackbar — gunakan key yang stabil
    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            snackMsg = null
        }
    }

    val beratBadan     = beratBadanStr.toIntOrNull() ?: 0
    val targetOtomatis = beratBadan * 35

    // FIX 7: Pindahkan saveProfile ke luar sebagai lambda yang capture state via snapshot,
    // hindari stale closure yang bisa menyebabkan crash
    val saveProfile: () -> Unit = {
        when {
            namaLengkap.isBlank() -> {
                snackMsg = "Nama tidak boleh kosong"
            }
            else -> {
                isLoading = true
                val kebutuhanAir = beratBadan * 35
                firestore.collection("users").document(uid)
                    .update(
                        mapOf(
                            "namaLengkap"  to namaLengkap.trim(),
                            "beratBadan"   to beratBadan,
                            "kebutuhanAir" to kebutuhanAir
                        )
                    )
                    .addOnSuccessListener {
                        isLoading = false
                        onNavigateBack()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        snackMsg  = "Gagal menyimpan: ${e.message}"
                    }
            }
        }
    }

    Scaffold(
        containerColor = EBgDeep,
        snackbarHost   = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF060D1F), Color(0xFF091428), Color(0xFF0B1A35))
                    )
                )
        ) {
            // Decorative blob
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-80).dp)
                    .background(
                        Brush.radialGradient(listOf(ETealPrimary.copy(0.08f), Color.Transparent)),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Top Bar ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ENavyCard)
                            .border(1.dp, EBorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint     = ETealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text       = "Edit Profil",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ETextWhite,
                        fontFamily = EPoppins
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text       = "DrinkUp",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = ETealPrimary,
                            fontFamily = EPoppins
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Avatar ───────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.fillMaxWidth()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(
                                    Brush.radialGradient(listOf(ETealPrimary.copy(0.20f), Color.Transparent)),
                                    CircleShape
                                )
                        )
                        // GenderAvatar sudah punya border sendiri — tidak perlu wrapper tambahan
                        GenderAvatar(gender = gender, nama = namaLengkap, size = 110.dp)
                    }

                    Spacer(Modifier.height(14.dp))

                    val genderColor = when (gender.uppercase()) {
                        "L"  -> ECyanAccent
                        "P"  -> Color(0xFFFF6B9D)
                        else -> ETextSub
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(genderColor.copy(0.12f))
                            .border(1.dp, genderColor.copy(0.30f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (gender.uppercase()) {
                                "L"  -> "♂  Laki-laki"
                                "P"  -> "♀  Perempuan"
                                else -> "—"
                            },
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = genderColor,
                            textAlign  = TextAlign.Center,
                            fontFamily = EPoppins
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Form ─────────────────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                    // Nama Lengkap
                    ELabel(text = "Nama Lengkap", required = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = namaLengkap,
                        onValueChange = { namaLengkap = it },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(16.dp),
                        placeholder   = {
                            Text("Nama kamu", color = ETextMuted, fontFamily = EPoppins, fontSize = 14.sp)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = ETextWhite, fontFamily = EPoppins, fontSize = 15.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = ETealPrimary,
                            unfocusedBorderColor    = EBorderSubtle,
                            focusedContainerColor   = ENavyCard,
                            unfocusedContainerColor = ENavyCard,
                            cursorColor             = ETealPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(22.dp))

                    // Email (read-only)
                    ELabel(text = "Alamat Email", required = false)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = emailDisplay,
                        onValueChange = {},
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(16.dp),
                        enabled       = false,
                        trailingIcon  = {
                            Icon(Icons.Filled.Lock, null, tint = ETextMuted, modifier = Modifier.size(18.dp))
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = ETextSub, fontFamily = EPoppins, fontSize = 15.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor    = EBorderSubtle.copy(0.35f),
                            disabledContainerColor = ENavyCard.copy(0.55f),
                            disabledTextColor      = ETextSub
                        )
                    )

                    Spacer(Modifier.height(22.dp))

                    // Berat Badan
                    ELabel(text = "Berat Badan (kg)", required = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = beratBadanStr,
                        onValueChange = { v ->
                            // FIX 8: Guard filter lebih ketat, tolak input selain digit
                            if (v.isEmpty() || (v.length <= 3 && v.all { it.isDigit() })) {
                                beratBadanStr = v
                            }
                        },
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder   = {
                            Text("Contoh: 65", color = ETextMuted, fontFamily = EPoppins, fontSize = 14.sp)
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = ETextWhite, fontFamily = EPoppins, fontSize = 15.sp
                        ),
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ETealPrimary.copy(alpha = 0.12f))
                                    .border(1.dp, ETealPrimary.copy(0.28f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "kg",
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = ETealPrimary,
                                    fontFamily = EPoppins
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = ETealPrimary,
                            unfocusedBorderColor    = EBorderSubtle,
                            focusedContainerColor   = ENavyCard,
                            unfocusedContainerColor = ENavyCard,
                            cursorColor             = ETealPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(22.dp))

                    // Kalkulasi Target Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ETealPrimary.copy(0.14f), ECyanAccent.copy(0.05f))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(ETealPrimary.copy(0.40f), ECyanAccent.copy(0.12f))
                                ),
                                RoundedCornerShape(22.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ETealPrimary.copy(0.16f))
                                        .border(1.dp, ETealPrimary.copy(0.30f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✦", fontSize = 14.sp, color = ETealPrimary)
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text       = "Kalkulasi Target Otomatis",
                                    fontSize   = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = ETextWhite,
                                    fontFamily = EPoppins
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text       = "Target harian disesuaikan secara dinamis berdasarkan berat badan saat ini.",
                                fontSize   = 12.sp,
                                color      = ETextSub,
                                lineHeight = 18.sp,
                                fontFamily = EPoppins
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                verticalAlignment     = Alignment.Bottom,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text       = if (beratBadan > 0) "%,d".format(targetOtomatis) else "—",
                                    fontSize   = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = ETealPrimary,
                                    fontFamily = EPoppins
                                )
                                if (beratBadan > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Column {
                                        Spacer(Modifier.height(18.dp))
                                        Text(
                                            text          = "MILILITER / HARI",
                                            fontSize      = 10.sp,
                                            fontWeight    = FontWeight.SemiBold,
                                            color         = ETextSub,
                                            letterSpacing = 0.5.sp,
                                            fontFamily    = EPoppins
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Tombol Simpan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (!isLoading)
                                    Brush.horizontalGradient(
                                        listOf(ETealPrimary, Color(0xFF00B894))
                                    )
                                else Brush.horizontalGradient(
                                    listOf(ETealPrimary.copy(0.40f), ETealPrimary.copy(0.40f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick  = { saveProfile() },
                            enabled  = !isLoading,
                            modifier = Modifier.fillMaxSize(),
                            shape    = RoundedCornerShape(28.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor         = Color.Transparent,
                                contentColor           = EBgDeep,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor   = EBgDeep.copy(0.60f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color       = EBgDeep,
                                    modifier    = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    "✓  Simpan Perubahan",
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = EPoppins
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

// ── Field Label ───────────────────────────────────────────────────────────────
@Composable
private fun ELabel(text: String, required: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(3.dp, 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (required) ETealPrimary else ETextMuted)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = text,
            fontSize   = 13.sp,
            fontWeight = if (required) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (required) ETextWhite else ETextSub,
            fontFamily = EPoppins
        )
    }
}
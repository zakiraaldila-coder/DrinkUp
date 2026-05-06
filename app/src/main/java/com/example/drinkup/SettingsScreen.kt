package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Poppins ───────────────────────────────────────────────────────────────────
private val Poppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ── Palette ───────────────────────────────────────────────────────────────────
private val SBgDeep       = Color(0xFF060D1F)
private val SBgMid        = Color(0xFF0A1428)
private val SNavyCard     = Color(0xFF0D1E38)
private val SNavyCardAlt  = Color(0xFF112244)
private val STealPrimary  = Color(0xFF00D4AA)
private val SCyanAccent   = Color(0xFF00BFFF)
private val SGreenOnline  = Color(0xFF1DDB8B)
private val STextWhite    = Color(0xFFFFFFFF)
private val STextSub      = Color(0xFF7A9BBF)
private val STextMuted    = Color(0xFF3D5A7A)
private val SDangerRed    = Color(0xFFFF4D6A)
private val SBorderSubtle = Color(0xFF162845)

@Composable
fun SettingsScreen(
    themeViewModel         : ThemeViewModel,
    onLogout               : () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    var nama       by remember { mutableStateOf("Pengguna") }
    var email      by remember { mutableStateOf("") }
    var beratBadan by remember { mutableStateOf(0) }
    var gender     by remember { mutableStateOf("") }

    val targetAir = beratBadan * 35

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    nama       = doc.getString("namaLengkap") ?: "Pengguna"
                    email      = doc.getString("email") ?: ""
                    beratBadan = doc.getLong("beratBadan")?.toInt() ?: 0
                    gender     = doc.getString("gender") ?: ""
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF060D1F), Color(0xFF091428), Color(0xFF0B1A35))
                )
            )
    ) {
        // Decorative glow blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(listOf(STealPrimary.copy(0.06f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 120.dp)
                .background(
                    Brush.radialGradient(listOf(SCyanAccent.copy(0.04f), Color.Transparent)),
                    CircleShape
                )
        )

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {

            // ── Header ───────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp)) {
                    Text(
                        text          = "Pengaturan",
                        fontSize      = 32.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = STextWhite,
                        fontFamily    = Poppins,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Kelola akun & preferensimu",
                        fontSize   = 13.sp,
                        color      = STextSub,
                        fontFamily = Poppins
                    )
                }
            }

            // ── Profil ───────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                    SLabel(title = "PROFIL", actionText = "Edit", onAction = onNavigateToEditProfile)
                    Spacer(Modifier.height(12.dp))

                    // Profile Card — glass
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.06f),
                                        Color.White.copy(alpha = 0.02f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end   = Offset(1000f, 1000f) // FIX: ganti dari Float.MAX_VALUE
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(Color.White.copy(0.16f), Color.White.copy(0.04f))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { onNavigateToEditProfile() }
                    ) {
                        Row(
                            modifier          = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(STealPrimary.copy(0.30f), Color.Transparent)
                                            ),
                                            CircleShape
                                        )
                                )
                                GenderAvatar(gender = gender, nama = nama, size = 64.dp) // inisial avatar
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(SGreenOnline)
                                        .border(2.dp, SBgDeep, CircleShape)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nama, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = STextWhite, fontFamily = Poppins)
                                Spacer(Modifier.height(2.dp))
                                Text(email, fontSize = 12.sp, color = STextSub, fontFamily = Poppins)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = STealPrimary.copy(0.65f), modifier = Modifier.size(22.dp))
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                    SLabel(title = "HEALTH DATA")
                    Spacer(Modifier.height(12.dp))

                    // Berat Badan Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SNavyCard)
                            .border(1.dp, SBorderSubtle, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text          = "BERAT BADAN",
                                fontSize      = 10.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = STextMuted,
                                letterSpacing = 1.8.sp,
                                fontFamily    = Poppins
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text       = if (beratBadan > 0) "$beratBadan" else "—",
                                    fontSize   = 44.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = STextWhite,
                                    fontFamily = Poppins
                                )
                                if (beratBadan > 0) {
                                    Text(
                                        text     = " kg",
                                        fontSize = 16.sp,
                                        color    = STextSub,
                                        fontFamily = Poppins,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            if (beratBadan > 0) {
                                Spacer(Modifier.height(12.dp))
                                SSegmentedProgressBar(
                                    progress = (beratBadan / 150f).coerceIn(0f, 1f),
                                    color    = STealPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier          = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToEditProfile() },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(STealPrimary))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text       = "Edit di profil →",
                                    fontSize   = 12.sp,
                                    color      = STealPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = Poppins
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Daily Target Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF005FA3), Color(0xFF0099CC), Color(0xFF00C4E0)),
                                    start = Offset(0f, 0f),
                                    end   = Offset(1000f, 1000f) // FIX: ganti dari Float.MAX_VALUE
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 40.dp, y = (-40).dp)
                                .background(
                                    Brush.radialGradient(listOf(Color.White.copy(0.10f), Color.Transparent)),
                                    CircleShape
                                )
                        )
                        Row(
                            modifier          = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .align(Alignment.Top),
                                contentAlignment = Alignment.Center
                            ) { Text("💧", fontSize = 18.sp) }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text          = "DAILY TARGET",
                                    fontSize      = 10.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = Color.White.copy(0.70f),
                                    letterSpacing = 1.8.sp,
                                    fontFamily    = Poppins
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text       = if (targetAir > 0)
                                            String.format("%.1f", targetAir / 1000.0).replace(".", ",")
                                        else "—",
                                        fontSize   = 52.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = Color.White,
                                        fontFamily = Poppins
                                    )
                                    if (targetAir > 0) {
                                        Text(
                                            text     = " L",
                                            fontSize = 24.sp,
                                            color    = Color.White.copy(0.80f),
                                            fontFamily = Poppins,
                                            modifier = Modifier.padding(bottom = 9.dp)
                                        )
                                    }
                                }
                                Text(
                                    text       = if (beratBadan > 0) "Berdasarkan berat badan $beratBadan kg"
                                    else "Atur berat badan di Edit Profil",
                                    fontSize   = 12.sp,
                                    color      = Color.White.copy(0.75f),
                                    fontFamily = Poppins
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                }
            }

            // ── Akun ─────────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SLabel(title = "AKUN")
                    Spacer(Modifier.height(12.dp))

                    SActionRow(
                        iconEmoji  = "↪",
                        iconBg     = Color(0xFF152A50),
                        label      = "Logout",
                        labelColor = STextWhite,
                        onClick    = onLogout
                    )
                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SDangerRed.copy(alpha = 0.07f))
                            .border(1.dp, SDangerRed.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                            .clickable { }
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(SDangerRed.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) { Text("🗑️", fontSize = 18.sp) }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = "Delete Account",
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = SDangerRed,
                                    fontFamily = Poppins
                                )
                                Text(
                                    text       = "Permanent Action",
                                    fontSize   = 11.sp,
                                    color      = SDangerRed.copy(0.45f),
                                    fontFamily = Poppins
                                )
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = SDangerRed.copy(0.45f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Reusable ──────────────────────────────────────────────────────────────────

@Composable
private fun SLabel(
    title      : String,
    actionText : String? = null,
    onAction   : (() -> Unit)? = null
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = title,
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = STealPrimary,
            letterSpacing = 1.4.sp,
            fontFamily    = Poppins
        )
        if (actionText != null && onAction != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(STealPrimary.copy(alpha = 0.12f))
                    .border(1.dp, STealPrimary.copy(0.28f), RoundedCornerShape(20.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = actionText,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = STealPrimary,
                    fontFamily = Poppins
                )
            }
        }
    }
}

@Composable
private fun SActionRow(
    iconEmoji  : String,
    iconBg     : Color,
    label      : String,
    labelColor : Color,
    onClick    : () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SNavyCard)
            .border(1.dp, SBorderSubtle, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(iconEmoji, fontSize = 18.sp, color = STextWhite, fontFamily = Poppins)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text       = label,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = labelColor,
                fontFamily = Poppins,
                modifier   = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ChevronRight, null, tint = STextSub, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SPreferenceToggleRow(
    emoji       : String,
    emojiBg     : Color,
    title       : String,
    subtitle    : String,
    checked     : Boolean,
    onChecked   : (Boolean) -> Unit,
    accentColor : Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SNavyCard)
            .border(1.dp, if (checked) accentColor.copy(0.28f) else SBorderSubtle, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(emojiBg),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 20.sp) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = STextWhite, fontFamily = Poppins)
                Text(text = subtitle, fontSize = 12.sp, color = STextSub, fontFamily = Poppins)
            }
            Switch(
                checked         = checked,
                onCheckedChange = onChecked,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = accentColor,
                    uncheckedThumbColor = STextSub,
                    uncheckedTrackColor = Color(0xFF1A2E48)
                )
            )
        }
    }
}

@Composable
private fun SSegmentedProgressBar(
    progress : Float,
    color    : Color,
    modifier : Modifier = Modifier
) {
    val segments = 20
    val filled   = (progress * segments).toInt()
    Row(modifier = modifier.height(5.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(segments) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < filled) color else Color(0xFF182C48))
            )
        }
    }
}

@Composable
private fun SArcGauge(progress: Float, size: Dp, color: Color) {
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label         = "arc"
    )
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxSize().drawBehind {
                val sw = 8f
                drawArc(color.copy(0.15f), 135f, 270f, false, style = Stroke(sw, cap = StrokeCap.Round))
                if (animProgress > 0f)
                    drawArc(color, 135f, 270f * animProgress, false, style = Stroke(sw, cap = StrokeCap.Round))
            }
        )
        Text("💧", fontSize = 18.sp)
    }
}
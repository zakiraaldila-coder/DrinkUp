package com.example.drinkup

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Palette (senada Tujuan Mingguan) ──────────────────────────────────────────
private val BgDeep       = Color(0xFF0A1535)
private val NavyCard     = Color(0xFF0F1F48)
private val NavyCardAlt  = Color(0xFF132257)
private val TealPrimary  = Color(0xFF00D4AA)
private val TealLight    = Color(0xFF26E5BC)
private val CyanAccent   = Color(0xFF00BFFF)
private val GreenSuccess = Color(0xFF1DDB8B)
private val TextWhite    = Color(0xFFFFFFFF)
private val TextSub      = Color(0xFF8AAAC8)
private val TextMuted    = Color(0xFF4A6A90)
private val DangerRed    = Color(0xFFFF4D6A)
private val GoldStar     = Color(0xFFFFD700)

@Composable
fun SettingsScreen(
    themeViewModel         : ThemeViewModel,
    onLogout               : () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    var notifikasi by remember { mutableStateOf(true) }
    var nama       by remember { mutableStateOf("Pengguna") }
    var email      by remember { mutableStateOf("") }
    var beratBadan by remember { mutableStateOf(0) }
    var gender     by remember { mutableStateOf("") }

    val targetAir = beratBadan * 35

    // Fetch dari Firestore — realtime
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
                    colors = listOf(Color(0xFF08122E), Color(0xFF0A1535), Color(0xFF0D1A3E))
                )
            )
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Page Header ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp, 28.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(TealPrimary)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text       = "Pengaturan",
                                fontSize   = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = TextWhite
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text     = "Customize your sanctuary experience",
                            fontSize = 13.sp,
                            color    = TextSub,
                            modifier = Modifier.padding(start = 14.dp)
                        )
                    }
                }
            }

            // ── Profile Section ──────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel(title = "Profil", actionText = "Edit", onAction = onNavigateToEditProfile)
                    Spacer(Modifier.height(12.dp))

                    // Profile Card — gradient border glow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        TealPrimary.copy(alpha = 0.25f),
                                        CyanAccent.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(TealPrimary.copy(0.5f), CyanAccent.copy(0.2f))),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { onNavigateToEditProfile() }
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with teal ring
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(TealPrimary.copy(0.3f), NavyCardAlt)
                                            )
                                        )
                                        .border(2.dp, TealPrimary.copy(0.7f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GenderAvatar(gender = gender, size = 68.dp)
                                }
                                // Online dot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(GreenSuccess)
                                        .border(2.dp, BgDeep, CircleShape)
                                )
                            }

                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    nama,
                                    fontSize   = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = TextWhite
                                )
                                Text(
                                    email,
                                    fontSize = 12.sp,
                                    color    = TextSub
                                )
                                Spacer(Modifier.height(8.dp))
                                // PRO badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(TealPrimary, CyanAccent)
                                            )
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⭐", fontSize = 9.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text       = "PRO MEMBER",
                                            fontSize   = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color      = BgDeep
                                        )
                                    }
                                }
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint     = TealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // ── Health Data ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel(title = "Health Data")
                    Spacer(Modifier.height(12.dp))

                    // Weight Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(NavyCard)
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(TealPrimary.copy(0.3f), Color.Transparent)
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { onNavigateToEditProfile() }
                    ) {
                        // Decorative circle top-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(TealPrimary.copy(alpha = 0.07f))
                                .border(1.dp, TealPrimary.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚖️", fontSize = 22.sp)
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text          = "BERAT BADAN",
                                fontSize      = 10.sp,
                                fontWeight    = FontWeight.Bold,
                                color         = TextMuted,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text       = if (beratBadan > 0) "$beratBadan" else "—",
                                    fontSize   = 42.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = TextWhite
                                )
                                Text(
                                    text     = " kg",
                                    fontSize = 18.sp,
                                    color    = TextSub,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            if (beratBadan > 0) {
                                Spacer(Modifier.height(10.dp))
                                // Segmented progress bar
                                SegmentedProgressBar(
                                    progress = (beratBadan / 150f).coerceIn(0f, 1f),
                                    color    = TealPrimary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text       = "Edit di profil →",
                                        fontSize   = 12.sp,
                                        color      = TealPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text       = "Atur berat badan di Edit Profil →",
                                    fontSize   = 12.sp,
                                    color      = TealPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Daily Target Card — gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    0f to Color(0xFF0E2A5C),
                                    0.5f to Color(0xFF0C3060),
                                    1f to Color(0xFF083555)
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(CyanAccent.copy(0.4f), TealPrimary.copy(0.2f))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        Row(
                            modifier          = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text          = "TARGET HARIAN",
                                    fontSize      = 10.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = CyanAccent.copy(0.7f),
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text       = if (targetAir > 0) String.format("%.1f", targetAir / 1000.0) else "—",
                                        fontSize   = 44.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = TextWhite
                                    )
                                    if (targetAir > 0) {
                                        Text(
                                            text     = "L",
                                            fontSize = 20.sp,
                                            color    = CyanAccent,
                                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                        )
                                    }
                                }
                                Text(
                                    text     = if (beratBadan > 0) "Berdasarkan berat badan $beratBadan kg" else "Atur berat badan di Edit Profil",
                                    fontSize = 12.sp,
                                    color    = TextSub
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // Mini arc gauge
                            ArcGauge(
                                progress = if (targetAir > 0) (targetAir / 1000f / 4f).coerceIn(0f, 1f) else 0f,
                                size     = 72.dp,
                                color    = CyanAccent
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // ── Preferences ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel(title = "Preferensi")
                    Spacer(Modifier.height(12.dp))

                    // Smart Reminders — only item after removing Dark Mode
                    PreferenceToggleRow(
                        emoji       = "🔔",
                        emojiBg     = TealPrimary.copy(0.15f),
                        title       = "Smart Reminders",
                        subtitle    = "Adaptive hydration alerts",
                        checked     = notifikasi,
                        onChecked   = { notifikasi = it },
                        accentColor = TealPrimary
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            // ── Account ──────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionLabel(title = "Akun")
                    Spacer(Modifier.height(12.dp))

                    // Logout Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(NavyCard)
                            .border(1.dp, NavyCardAlt, RoundedCornerShape(20.dp))
                            .clickable { onLogout() }
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A6E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("↪", fontSize = 18.sp, color = TextWhite)
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text       = "Logout",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = TextWhite,
                                modifier   = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint     = TextSub,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Delete Account Row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DangerRed.copy(alpha = 0.08f))
                            .border(1.dp, DangerRed.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                            .clickable { }
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🗑️", fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = "Delete Account",
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = DangerRed
                                )
                                Text(
                                    text     = "Permanent Action",
                                    fontSize = 11.sp,
                                    color    = DangerRed.copy(0.5f)
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint     = DangerRed.copy(0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Reusable Components ───────────────────────────────────────────────────────

@Composable
private fun SectionLabel(
    title      : String,
    actionText : String? = null,
    onAction   : (() -> Unit)? = null
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(3.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TealPrimary)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = title,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = TextWhite
            )
        }
        if (actionText != null && onAction != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealPrimary.copy(alpha = 0.15f))
                    .clickable { onAction() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text       = actionText,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TealPrimary
                )
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    emoji       : String,
    emojiBg     : Color,
    title       : String,
    subtitle    : String,
    checked     : Boolean,
    onChecked   : (Boolean) -> Unit,
    accentColor : Color
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color(0xFF2A3F6A),
        label       = "track"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NavyCard)
            .border(
                1.dp,
                if (checked) accentColor.copy(0.3f) else NavyCardAlt,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(emojiBg),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = title,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextWhite
                )
                Text(
                    text     = subtitle,
                    fontSize = 12.sp,
                    color    = TextSub
                )
            }
            Switch(
                checked         = checked,
                onCheckedChange = onChecked,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = Color.White,
                    checkedTrackColor   = accentColor,
                    uncheckedThumbColor = TextSub,
                    uncheckedTrackColor = Color(0xFF1C2E50)
                )
            )
        }
    }
}

@Composable
private fun SegmentedProgressBar(
    progress : Float,
    color    : Color,
    modifier : Modifier = Modifier
) {
    val segments = 20
    val filled   = (progress * segments).toInt()
    Row(
        modifier            = modifier.height(6.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(segments) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (i < filled) color
                        else Color(0xFF1E3560)
                    )
            )
        }
    }
}

@Composable
private fun ArcGauge(
    progress : Float,
    size     : Dp,
    color    : Color
) {
    val animProgress by animateFloatAsState(
        targetValue    = progress,
        animationSpec  = tween(1000, easing = FastOutSlowInEasing),
        label          = "arc"
    )

    Box(
        modifier         = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val strokeWidth = 8f
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val sweepAngle = 270f * animProgress
                    val startAngle = 135f
                    // Track
                    drawArc(
                        color      = color.copy(alpha = 0.15f),
                        startAngle = startAngle,
                        sweepAngle = 270f,
                        useCenter  = false,
                        style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    // Fill
                    if (animProgress > 0f) {
                        drawArc(
                            color      = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter  = false,
                            style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
        )
        Text("💧", fontSize = 18.sp)
    }
}
package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill

// ── Warna tema senada halaman Statistik (navy biru medium + aksen teal/cyan) ──
private val WgNavyDark   = Color(0xFF0B1A35)   // bg utama navy — sama seperti statistik
private val WgNavyMid    = Color(0xFF0E2040)   // navy layer 2
private val WgNavyCard   = Color(0xFF112545)   // card surface
private val WgNavyDeep   = Color(0xFF0D1E38)   // elemen bar kosong
private val WgNavyBrd    = Color(0xFF1B3560)   // border kartu
private val WgWater1     = Color(0xFF00BFA5)   // teal utama (aksen statistik)
private val WgTeal       = Color(0xFF26C6DA)   // cyan terang (bar chart statistik)
private val WgGray       = Color(0xFF8FA8C8)   // secondary text
private val WgGrayDim    = Color(0xFF4A6880)   // muted text
private val WgGreen      = Color(0xFF43E97B)   // hijau tetap sama (target tercapai)
private val WgAmber      = Color(0xFFFFB300)   // amber tetap sama (garis target)

// Label hari dalam seminggu (Senin=0 ... Minggu=6)
private val DAY_LABELS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
fun WeeklyGoalScreen(
    intakeViewModel : IntakeViewModel,
    onBack          : () -> Unit = {}
) {
    val intakeState by intakeViewModel.state.collectAsState()

    // ── Bangun data 7 hari dari weeklyHistory (Firestore) ────────────────────
    val sdf       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDow  = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val mondayOffset = -todayDow
    val weekDays  = (0..6).map { i ->
        val c       = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, mondayOffset + i) }
        val dateKey = sdf.format(c.time)
        val intake  = intakeState.weeklyHistory[dateKey] ?: 0
        Triple(DAY_LABELS[i], dateKey, intake)
    }

    val targetIntake  = intakeState.userTarget
    val weeklyTarget  = targetIntake * 7
    val weeklyTotal   = weekDays.sumOf { it.third }
    val weeklyPct     = (weeklyTotal.toFloat() / weeklyTarget).coerceIn(0f, 1f)
    val weeklyPctInt  = (weeklyPct * 100).toInt()

    val daysHit   = weekDays.count { it.third >= targetIntake && it.third > 0 }
    val avgIntake = weeklyTotal / 7
    val bestLabel = weekDays.maxByOrNull { it.third }?.first ?: "-"

    val animArc by animateFloatAsState(
        targetValue   = weeklyPct,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label         = "arc"
    )

    // Animasi infinite pulse untuk icon drop di tengah arc
    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infinitePulse.animateFloat(
        initialValue = 0.92f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Warna & pesan motivasi berdasarkan progress
    val (motivasiText, motivasiIcon, accentColor) = when {
        weeklyPctInt >= 100 -> Triple("Luar biasa! Target minggu ini tercapai!", Icons.Rounded.EmojiEvents, WgGreen)
        weeklyPctInt >= 75  -> Triple("Hampir sampai! Terus semangat!", Icons.Rounded.TrendingUp, WgWater1)
        weeklyPctInt >= 50  -> Triple("Setengah jalan, jangan berhenti!", Icons.Rounded.DirectionsRun, WgAmber)
        weeklyPctInt >= 25  -> Triple("Awal yang baik, pertahankan ritme ini!", Icons.Rounded.FitnessCenter, WgWater1)
        else                -> Triple("Mulai hari ini, tubuhmu butuh air!", Icons.Rounded.WaterDrop, WgWater1)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WgNavyDark, WgNavyMid, WgNavyDark)
                )
            )
    ) {
        // Dekorasi latar belakang — lingkaran blur besar
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color  = WgNavyMid.copy(alpha = 0.45f),
                radius = size.width * 0.75f,
                center = Offset(size.width * 1.1f, size.height * 0.08f)
            )
            drawCircle(
                color  = WgWater1.copy(alpha = 0.04f),
                radius = size.width * 0.55f,
                center = Offset(-size.width * 0.15f, size.height * 0.55f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── HEADER ────────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(WgNavyDark, WgNavyMid))
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WgNavyCard)
                        .border(1.dp, WgNavyBrd, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Tujuan Mingguan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        "Pantau progres hidrasi minggu ini",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WgGray
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // ── MOTIVASI BANNER ───────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(motivasiIcon, null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        motivasiText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── CARD UTAMA — ARC PROGRESS ─────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(28.dp),
                    colors    = CardDefaults.cardColors(containerColor = WgNavyCard),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border    = BorderStroke(1.dp, WgNavyBrd)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        // Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.TrackChanges,
                                null,
                                tint     = WgWater1,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "TARGET MINGGUAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = WgWater1,
                                    letterSpacing = 1.sp,
                                    fontWeight    = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            // Persentase besar
                            Column {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "$weeklyPctInt",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            color      = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize   = 72.sp
                                        )
                                    )
                                    Text(
                                        "%",
                                        style    = MaterialTheme.typography.headlineMedium.copy(
                                            color      = WgWater1,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                                    )
                                }
                                val weeklyTotalL  = String.format("%.2f", weeklyTotal / 1000f)
                                val weeklyTargetL = String.format("%.1f", weeklyTarget / 1000f)
                                Text(
                                    "${weeklyTotalL}L dari ${weeklyTargetL}L",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = WgGray,
                                        lineHeight = 18.sp
                                    )
                                )
                            }

                            // Arc lingkaran progress
                            Box(
                                modifier         = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val stroke  = 14.dp.toPx()
                                    val padding = stroke / 2f
                                    val arcRect = androidx.compose.ui.geometry.Rect(
                                        left   = padding,
                                        top    = padding,
                                        right  = size.width  - padding,
                                        bottom = size.height - padding
                                    )
                                    // Track
                                    drawArc(
                                        color      = Color.White.copy(alpha = 0.10f),
                                        startAngle = 135f,
                                        sweepAngle = 270f,
                                        useCenter  = false,
                                        style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                        topLeft    = arcRect.topLeft,
                                        size       = arcRect.size
                                    )
                                    // Progress gradient
                                    if (animArc > 0f) {
                                        drawArc(
                                            brush      = Brush.sweepGradient(listOf(WgTeal, WgWater1, WgTeal)),
                                            startAngle = 135f,
                                            sweepAngle = 270f * animArc,
                                            useCenter  = false,
                                            style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                            topLeft    = arcRect.topLeft,
                                            size       = arcRect.size
                                        )
                                    }
                                }
                                // Icon tetes air di tengah arc dengan efek pulse
                                Box(
                                    modifier = Modifier
                                        .size((44 * pulseScale).dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(listOf(WgTeal.copy(alpha = 0.3f), Color.Transparent))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.WaterDrop,
                                        null,
                                        tint     = WgWater1,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Progress bar linear
                        Column {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Progress minggu ini",
                                    style = MaterialTheme.typography.labelSmall.copy(color = WgGray)
                                )
                                Text(
                                    "$weeklyPctInt%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color      = WgWater1,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.08f))
                            ) {
                                val animLinear by animateFloatAsState(
                                    targetValue   = weeklyPct,
                                    animationSpec = tween(1600, easing = FastOutSlowInEasing),
                                    label         = "linear"
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animLinear)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(listOf(WgTeal, WgWater1))
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── HYDRATION WAVES BAR CHART ─────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(24.dp),
                    colors    = CardDefaults.cardColors(containerColor = WgNavyCard),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border    = BorderStroke(1.dp, WgNavyBrd)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WgWater1.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.BarChart,
                                        null,
                                        tint     = WgWater1,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Hydration Waves",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color      = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "HARI TERBAIK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color         = WgGray,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Star,
                                        null,
                                        tint     = WgAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        bestLabel,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color      = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        val maxBar = maxOf(weekDays.maxOf { it.third }, targetIntake).toFloat()
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment     = Alignment.Bottom
                        ) {
                            weekDays.forEachIndexed { idx, (label, _, intake) ->
                                val barFrac  = (intake / maxBar).coerceIn(0f, 1f)
                                val isToday  = idx == todayDow
                                val hitTarget = intake >= targetIntake && intake > 0

                                val barColors = when {
                                    isToday     -> listOf(WgTeal, WgWater1)
                                    hitTarget   -> listOf(WgGreen.copy(alpha = 0.8f), WgGreen)
                                    intake > 0  -> listOf(WgWater1.copy(alpha = 0.6f), WgWater1)
                                    else        -> listOf(WgNavyDeep, WgNavyBrd)
                                }

                                val animBar by animateFloatAsState(
                                    targetValue   = barFrac,
                                    animationSpec = tween(900 + idx * 100, easing = FastOutSlowInEasing),
                                    label         = "bar$idx"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier            = Modifier.fillMaxHeight()
                                ) {
                                    // Dot target tercapai
                                    if (hitTarget) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            null,
                                            tint     = WgGreen,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .padding(bottom = 2.dp)
                                        )
                                    } else {
                                        Spacer(Modifier.height(12.dp))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier         = Modifier
                                            .width(30.dp)
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50))
                                            .background(WgNavyDeep.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(animBar)
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    Brush.verticalGradient(barColors)
                                                )
                                        )
                                        // Garis target
                                        if (targetIntake > 0 && maxBar > 0) {
                                            val targetFrac = (targetIntake / maxBar).coerceIn(0f, 1f)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(WgAmber.copy(alpha = 0.35f))
                                                    .align(Alignment.BottomCenter)
                                                    .offset(y = (-((targetFrac) * 130)).dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color      = if (isToday) WgWater1 else WgGray,
                                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                            fontSize   = 11.sp
                                        )
                                    )
                                    // Dot hari ini
                                    if (isToday) {
                                        Spacer(Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(WgWater1)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Legend
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LegendDot(color = WgWater1, label = "Progres")
                            LegendDot(color = WgGreen, label = "Target tercapai")
                            LegendDot(color = WgAmber, label = "Garis target")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── STATISTIK CARDS 2x1 ───────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatIconCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Rounded.CheckCircle,
                        iconColor   = WgGreen,
                        label       = "HARI TERCAPAI",
                        value       = "$daysHit / 7",
                        subLabel    = "dari 7 hari"
                    )
                    StatIconCard(
                        modifier    = Modifier.weight(1f),
                        icon        = Icons.Rounded.LocalDrink,
                        iconColor   = WgWater1,
                        label       = "RATA-RATA/HARI",
                        value       = "${avgIntake}ml",
                        subLabel    = "per hari"
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── STREAK CARD ───────────────────────────────────────────────
                // Gunakan streak dari ViewModel (Firestore) agar konsisten dengan Dashboard
                val streakDays = intakeState.streak

                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = if (streakDays > 0) WgGreen.copy(alpha = 0.10f) else WgNavyCard
                    ),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border    = BorderStroke(
                        1.dp,
                        if (streakDays > 0) WgGreen.copy(alpha = 0.4f) else WgNavyBrd
                    )
                ) {
                    Row(
                        modifier          = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (streakDays > 0) WgGreen.copy(alpha = 0.18f)
                                    else WgNavyDeep
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                null,
                                tint     = if (streakDays > 0) WgGreen else WgGray,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Streak Minggu Ini",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (streakDays > 0)
                                    "$streakDays hari berturut-turut mencapai target!"
                                else
                                    "Belum ada streak minggu ini. Ayo mulai!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = if (streakDays > 0) WgGreen else WgGray,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── CONSISTENCY INSIGHT ───────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = WgNavyCard),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border    = BorderStroke(1.dp, WgNavyBrd)
                ) {
                    Row(
                        modifier          = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WgWater1.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Insights,
                                null,
                                tint     = WgWater1,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Consistency Insight",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                "Kamu paling aktif minum antara pukul 08.00–11.00. " +
                                        "Pertahankan ritme ini untuk menjaga hidrasi optimal sepanjang hari!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = WgGray,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// STAT ICON CARD
// ════════════════════════════════════════════════════════════════════════════
@Composable
private fun StatIconCard(
    modifier  : Modifier,
    icon      : androidx.compose.ui.graphics.vector.ImageVector,
    iconColor : Color,
    label     : String,
    value     : String,
    subLabel  : String
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = WgNavyCard),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, WgNavyBrd)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = WgGray,
                    letterSpacing = 0.5.sp,
                    fontWeight    = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                subLabel,
                style = MaterialTheme.typography.labelSmall.copy(color = WgGrayDim)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════════════════
// LEGEND DOT
// ════════════════════════════════════════════════════════════════════════════
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = WgGray,
                fontSize = 10.sp
            )
        )
    }
}
package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// ─── Design tokens ────────────────────────────────────────────────────────────
private val BgDeep      = Color(0xFF0B1629)
private val BgCard      = Color(0xFF112240)
private val BgCardAlt   = Color(0xFF0D1B36)
private val AccentCyan  = Color(0xFF00E5FF)
private val AccentBlue  = Color(0xFF2979FF)
private val AccentTeal  = Color(0xFF00BFA5)
private val AccentOrange= Color(0xFFFF6D00)
private val AccentRed   = Color(0xFFFF1744)
private val TextPrimary = Color(0xFFE8F0FE)
private val TextSec     = Color(0xFF7B93B8)
private val TextMuted   = Color(0xFF3D5A80)

// ─── Canvas icons ─────────────────────────────────────────────────────────────

@Composable
fun DropIconStat(modifier: Modifier = Modifier, color: Color = AccentCyan) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val path = Path().apply {
            moveTo(cx, 0f)
            cubicTo(cx + size.width * .5f, size.height * .4f,
                cx + size.width * .5f, size.height * .75f, cx, size.height)
            cubicTo(cx - size.width * .5f, size.height * .75f,
                cx - size.width * .5f, size.height * .4f, cx, 0f)
            close()
        }
        drawPath(path, color)
        val shine = Path().apply {
            moveTo(cx - size.width * .09f, size.height * .26f)
            cubicTo(cx - size.width * .19f, size.height * .43f,
                cx - size.width * .16f, size.height * .57f,
                cx - size.width * .07f, size.height * .57f)
            cubicTo(cx - size.width * .02f, size.height * .57f,
                cx + size.width * .02f, size.height * .49f,
                cx - size.width * .09f, size.height * .26f)
            close()
        }
        drawPath(shine, Color.White.copy(alpha = .4f))
    }
}

@Composable
fun ChartBarIcon(modifier: Modifier = Modifier, color: Color = AccentCyan) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val fracs = listOf(.5f, .75f, 1f)
        val bw = w * .22f; val gap = w * .10f
        val sx = (w - (3 * bw + 2 * gap)) / 2f
        fracs.forEachIndexed { i, frac ->
            val bh = h * frac * .85f
            drawRoundRect(color.copy(alpha = .4f + .3f * i),
                topLeft = Offset(sx + i * (bw + gap), h - bh),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(3f))
        }
    }
}

@Composable
fun FlameIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f; val cy = size.height * .88f; val s = size.width
        fun layer(w: Float, h: Float, col: Color) {
            val p = Path()
            p.moveTo(cx, cy); p.cubicTo(cx - w*.5f, cy - h*.2f, cx - w*.22f, cy - h*.65f, cx, cy - h)
            p.cubicTo(cx + w*.22f, cy - h*.65f, cx + w*.5f, cy - h*.2f, cx, cy); p.close()
            drawPath(p, col)
        }
        layer(s*.75f, s*.92f, Color(0xFFFF5722))
        layer(s*.52f, s*.72f, Color(0xFFFF9800))
        layer(s*.30f, s*.50f, Color(0xFFFFCC02))
    }
}

@Composable
fun TargetCircleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = minOf(size.width, size.height) / 2f
        drawCircle(AccentRed, r, c)
        drawCircle(Color.White, r * .68f, c)
        drawCircle(AccentRed, r * .38f, c)
    }
}

@Composable
fun ClockIconStat(modifier: Modifier = Modifier, color: Color = AccentCyan) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f; val cy = size.height / 2f; val r = size.width / 2f
        drawCircle(color, r, Offset(cx, cy))
        drawCircle(Color.White.copy(.15f), r * .82f, Offset(cx, cy), style = Stroke(r * .06f))
        drawLine(Color.White, Offset(cx, cy), Offset(cx, cy - r * .52f), r * .08f, StrokeCap.Round)
        drawLine(Color.White, Offset(cx, cy), Offset(cx + r * .38f, cy), r * .07f, StrokeCap.Round)
        drawCircle(Color.White, r * .08f, Offset(cx, cy))
    }
}

// ─── StatistikScreen ──────────────────────────────────────────────────────────

@Composable
fun StatistikScreen(
    intakeViewModel: IntakeViewModel,
    onReadMore: () -> Unit = {}
) {
    val intakeState  by intakeViewModel.state.collectAsState()
    val weeklyHistory = intakeState.weeklyHistory
    val targetIntake  = intakeState.userTarget
    val todayEntries  = intakeState.todayEntries

    val sdf          = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDow     = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val mondayOffset = -todayDow
    val weekDays = (0..6).map { i ->
        val c = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, mondayOffset + i) }
        Pair(sdf.format(c.time), weeklyHistory[sdf.format(c.time)] ?: 0)
    }
    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    val weeklyTotal = weekDays.sumOf { it.second }
    val recordMl    = weeklyHistory.values.maxOrNull() ?: 0
    val avgMl       = weeklyTotal / 7
    val totalL      = weeklyTotal / 1000.0
    val avgL        = avgMl / 1000.0
    val recordL     = recordMl / 1000.0

    val hourlyBuckets = IntArray(8) { 0 }
    todayEntries.forEach { entry ->
        val hour = Calendar.getInstance()
            .also { it.timeInMillis = entry.timestamp }
            .get(Calendar.HOUR_OF_DAY)
        hourlyBuckets[(hour / 3).coerceIn(0, 7)] += entry.amount
    }
    val hourLabels = listOf("00", "03", "06", "09", "12", "15", "18", "21")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Hero Header ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0B1D3A), Color(0xFF0D2D50), Color(0xFF0E3A5F)),
                                Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(AccentCyan.copy(.06f), size.width * .55f,
                            Offset(size.width * .9f, size.height * .15f))
                        drawCircle(AccentBlue.copy(.08f), size.width * .32f,
                            Offset(size.width * .05f, size.height * .9f))
                    }
                    // Chip total minggu ini — kanan atas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 20.dp, end = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(.08f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "MINGGU INI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSec,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${String.format("%.1f", totalL)} L",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 22.dp, bottom = 24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropIconStat(Modifier.size(12.dp), AccentCyan)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "HIDRASI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentCyan,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Statistik\nMingguan",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            lineHeight = 36.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Lacak hidrasi Anda sepanjang minggu",
                            fontSize = 13.sp,
                            color = TextSec
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Mini Stat Row ────────────────────────────────────────────────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = { ChartBarIcon(Modifier.size(20.dp), AccentCyan) },
                        iconBg = AccentCyan.copy(.12f),
                        label = "RATA-RATA/HARI",
                        value = "${String.format("%.1f", avgL)} L"
                    )
                    StatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = { FlameIcon(Modifier.size(20.dp)) },
                        iconBg = AccentOrange.copy(.12f),
                        label = "REKOR TERBAIK",
                        value = "${String.format("%.1f", recordL)} L"
                    )
                    StatMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = { TargetCircleIcon(Modifier.size(20.dp)) },
                        iconBg = AccentRed.copy(.12f),
                        label = "TARGET HARIAN",
                        value = "${String.format("%.1f", targetIntake / 1000.0)} L"
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Health Tips Card ─────────────────────────────────────────────
            item {
                HealthTipsCard(onReadMore = onReadMore)
                Spacer(Modifier.height(20.dp))
            }

            // ── Hydration Chart ──────────────────────────────────────────────
            item {
                HydrationChartCard(weekDays, dayLabels, targetIntake, totalL, todayDow)
                Spacer(Modifier.height(20.dp))
            }

            // ── Progress vs Target ───────────────────────────────────────────
            item {
                ProgressVsTargetCard(weekDays, dayLabels, targetIntake)
                Spacer(Modifier.height(20.dp))
            }

            // ── Pola Jam Minum ───────────────────────────────────────────────
            item {
                HourlyPatternCard(hourlyBuckets, hourLabels)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ─── StatMiniCard ─────────────────────────────────────────────────────────────

@Composable
fun StatMiniCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    iconBg: Color,
    label: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.height(10.dp))
            Text(
                label,
                fontSize = 8.sp,
                color = TextSec,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = .4.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
    }
}

// ─── HealthTipsCard ───────────────────────────────────────────────────────────

@Composable
fun HealthTipsCard(onReadMore: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
    ) {
        Column {
            // top teal accent row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Drop icon circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF004D6B), Color(0xFF006A80)),
                                Offset.Zero,
                                Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    DropIconStat(Modifier.size(26.dp), AccentCyan)
                }
                Column {
                    Text(
                        "HEALTH TIPS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentTeal,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Pentingnya Air Saat Pagi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color.White.copy(.06f)
            )
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Memulai hari dengan segelas air hangat dapat membantu mengaktifkan organ internal dan membuang racun sebelum mengonsumsi makanan pertama.",
                    fontSize = 13.sp,
                    color = TextSec,
                    lineHeight = 21.sp
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.clickable { onReadMore() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Baca Selengkapnya",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                    Text("›", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                }
            }
        }
    }
}

// ─── HydrationChartCard ───────────────────────────────────────────────────────

@Composable
fun HydrationChartCard(
    weekDays: List<Pair<String, Int>>,
    dayLabels: List<String>,
    targetIntake: Int,
    totalL: Double,
    todayDow: Int
) {
    val max            = maxOf(weekDays.maxOf { it.second }, targetIntake).toFloat()
    val targetFraction = if (max > 0) targetIntake / max else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
    ) {
        Column(Modifier.padding(20.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(.12f)),
                        contentAlignment = Alignment.Center
                    ) { ChartBarIcon(Modifier.size(20.dp), AccentBlue) }
                    Column {
                        Text(
                            "Aktivitas Hidrasi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "7 HARI TERAKHIR",
                            fontSize = 9.sp,
                            color = TextSec,
                            letterSpacing = .5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TOTAL MINGGU INI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSec,
                        letterSpacing = .5.sp
                    )
                    Text(
                        "${String.format("%.1f", totalL)} L",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            val hasData = weekDays.any { it.second > 0 }
            if (!hasData) {
                Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DropIconStat(Modifier.size(36.dp), TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada data minggu ini", color = TextSec, fontSize = 14.sp)
                    }
                }
            } else {
                // Target dashed line + bars
                Box(Modifier.fillMaxWidth().height(160.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val yTarget = size.height * (1f - targetFraction)
                        drawLine(
                            AccentRed.copy(.4f), Offset(0f, yTarget), Offset(size.width, yTarget),
                            1.5.dp.toPx(), StrokeCap.Round,
                            PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weekDays.forEachIndexed { idx, (_, value) ->
                            val isToday = idx == todayDow
                            val animFrac by animateFloatAsState(
                                if (max > 0) value / max else 0f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                                label = "b$idx"
                            )
                            val barBrush = when {
                                value == 0       -> Brush.verticalGradient(listOf(BgCardAlt, BgCardAlt))
                                value >= targetIntake -> Brush.verticalGradient(listOf(AccentCyan.copy(.5f), AccentCyan))
                                isToday          -> Brush.verticalGradient(listOf(AccentBlue.copy(.5f), AccentBlue))
                                else             -> Brush.verticalGradient(listOf(AccentBlue.copy(.2f), AccentBlue.copy(.45f)))
                            }
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animFrac.coerceAtLeast(.04f))
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(barBrush)
                                ) {
                                    if (value > 0)
                                        Box(
                                            Modifier.fillMaxWidth().height(3.dp)
                                                .align(Alignment.TopCenter)
                                                .background(Color.White.copy(.25f))
                                        )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    dayLabels.forEachIndexed { idx, label ->
                        val isToday = idx == todayDow
                        Text(
                            label,
                            fontSize = 11.sp,
                            color = if (isToday) AccentCyan else TextSec,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── ProgressVsTargetCard ─────────────────────────────────────────────────────

@Composable
fun ProgressVsTargetCard(
    weekDays: List<Pair<String, Int>>,
    dayLabels: List<String>,
    targetIntake: Int
) {
    val todayDow = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
    ) {
        Column(Modifier.padding(20.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(.12f)),
                    contentAlignment = Alignment.Center
                ) { TargetCircleIcon(Modifier.size(18.dp)) }
                Column {
                    Text(
                        "Progress vs Target",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Seberapa dekat tiap hari ke target",
                        fontSize = 12.sp,
                        color = TextSec
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            weekDays.forEachIndexed { idx, (_, intake) ->
                val isToday  = idx == todayDow
                val pct      = if (targetIntake > 0) (intake.toFloat() / targetIntake).coerceIn(0f, 1f) else 0f
                val exceeded = intake >= targetIntake
                val hasData  = intake > 0

                val animPct by animateFloatAsState(
                    pct,
                    tween(800 + idx * 60, easing = FastOutSlowInEasing),
                    label = "p$idx"
                )

                val barBrush: Brush? = when {
                    !hasData -> null
                    exceeded -> Brush.horizontalGradient(listOf(AccentTeal, AccentCyan))
                    isToday  -> Brush.horizontalGradient(listOf(AccentBlue, AccentCyan.copy(.8f)))
                    else     -> Brush.horizontalGradient(listOf(AccentBlue.copy(.4f), AccentBlue.copy(.65f)))
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        dayLabels[idx],
                        fontSize = 13.sp,
                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isToday) AccentCyan else TextSec,
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .height(if (isToday) 14.dp else 10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(BgCardAlt)
                    ) {
                        if (hasData && barBrush != null)
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animPct)
                                    .clip(RoundedCornerShape(50))
                                    .background(barBrush)
                            )
                    }
                    Text(
                        if (!hasData) "–" else "${(pct * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            exceeded -> AccentCyan
                            isToday  -> AccentBlue
                            else     -> TextSec
                        },
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(.06f))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(AccentCyan))
                    Spacer(Modifier.width(5.dp))
                    Text("TARGET TERCAPAI", fontSize = 10.sp, color = TextSec, letterSpacing = .3.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(AccentBlue.copy(.6f)))
                    Spacer(Modifier.width(5.dp))
                    Text("SEBAGIAN", fontSize = 10.sp, color = TextSec, letterSpacing = .3.sp)
                }
            }
        }
    }
}

// ─── HourlyPatternCard ────────────────────────────────────────────────────────

@Composable
fun HourlyPatternCard(hourlyBuckets: IntArray, hourLabels: List<String>) {
    val maxBucket  = hourlyBuckets.max().takeIf { it > 0 } ?: 1
    val peakBucket = hourlyBuckets.indexOfFirst { it == hourlyBuckets.max() }
    val peakLabel  = if (hourlyBuckets.max() > 0) {
        "${String.format("%02d", peakBucket * 3)}.00 - ${String.format("%02d", peakBucket * 3 + 3)}.00"
    } else "--"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                AccentCyan.copy(.04f), size.width * .48f,
                Offset(size.width * .92f, size.height * .12f)
            )
        }
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(.12f)),
                        contentAlignment = Alignment.Center
                    ) { ClockIconStat(Modifier.size(18.dp), AccentCyan) }
                    Column {
                        Text(
                            "Pola Jam Minum",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Distribusi minum hari ini",
                            fontSize = 12.sp,
                            color = TextSec
                        )
                    }
                }
                if (hourlyBuckets.max() > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "PALING AKTIF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentCyan,
                            letterSpacing = .5.sp
                        )
                        Text(
                            peakLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            if (hourlyBuckets.max() == 0) {
                Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DropIconStat(Modifier.size(30.dp), TextMuted)
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada data minum hari ini", color = TextSec, fontSize = 13.sp)
                    }
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    hourlyBuckets.forEachIndexed { idx, amount ->
                        val isPeak  = idx == peakBucket && amount > 0
                        val animFrac by animateFloatAsState(
                            amount.toFloat() / maxBucket,
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                            label = "h$idx"
                        )
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .width(24.dp)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(.05f)),
                                Alignment.BottomCenter
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animFrac)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isPeak)
                                                Brush.verticalGradient(listOf(AccentCyan.copy(.5f), AccentCyan))
                                            else
                                                Brush.verticalGradient(listOf(AccentBlue.copy(.2f), AccentBlue.copy(.45f)))
                                        )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    hourLabels.forEach { label ->
                        Text(
                            label,
                            fontSize = 9.sp,
                            color = TextSec,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ─── SmallStatCard (legacy compat) ────────────────────────────────────────────
@Composable
fun SmallStatCard(modifier: Modifier = Modifier, icon: String, label: String, value: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BgCard)
            .padding(16.dp)
    ) {
        Column {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSec, letterSpacing = .5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
    }
}

// ─── MiniStatCard (legacy compat) ─────────────────────────────────────────────
@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    tint: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(.12f)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 9.sp, color = TextSec, fontWeight = FontWeight.SemiBold, letterSpacing = .3.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
    }
}
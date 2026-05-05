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

// ─── Palet warna konsisten ────────────────────────────────────────────────────
private val C_DeepBlue   = Color(0xFF0D3B6E)
private val C_MidBlue    = Color(0xFF1565C0)
private val C_Light      = Color(0xFF42A5F5)
private val C_Cyan       = Color(0xFF26C6DA)
private val C_Red        = Color(0xFFEF5350)
private val C_Orange     = Color(0xFFFF7043)

// ─── Canvas Icons ─────────────────────────────────────────────────────────────

@Composable
fun DropIconStat(modifier: Modifier = Modifier, color: Color = C_Light) {
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
fun ChartBarIcon(modifier: Modifier = Modifier, color: Color = C_Light) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val fracs  = listOf(.5f, .75f, 1f)
        val alphas = listOf(.5f, .75f, 1f)
        val bw = w * .22f; val gap = w * .10f
        val sx = (w - (3 * bw + 2 * gap)) / 2f
        fracs.forEachIndexed { i, frac ->
            val bh = h * frac * .85f
            drawRoundRect(color.copy(alpha = alphas[i]),
                topLeft = Offset(sx + i * (bw + gap), h - bh),
                size    = Size(bw, bh),
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
            p.moveTo(cx, cy)
            p.cubicTo(cx - w*.5f, cy - h*.2f, cx - w*.22f, cy - h*.65f, cx, cy - h)
            p.cubicTo(cx + w*.22f, cy - h*.65f, cx + w*.5f, cy - h*.2f, cx, cy)
            p.close()
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
        drawCircle(C_Red, r, c)
        drawCircle(Color.White, r * .68f, c)
        drawCircle(C_Red, r * .38f, c)
    }
}

@Composable
fun ClockIconStat(modifier: Modifier = Modifier, color: Color = C_Cyan) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f; val cy = size.height / 2f; val r = size.width / 2f
        drawCircle(color, r, Offset(cx, cy))
        drawCircle(Color.White.copy(.15f), r * .82f, Offset(cx, cy),
            style = Stroke(r * .06f))
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
    val colorScheme = MaterialTheme.colorScheme
    val intakeState by intakeViewModel.state.collectAsState()

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
        val hour   = Calendar.getInstance().also { it.timeInMillis = entry.timestamp }.get(Calendar.HOUR_OF_DAY)
        hourlyBuckets[(hour / 3).coerceIn(0, 7)] += entry.amount
    }
    val hourLabels = listOf("00", "03", "06", "09", "12", "15", "18", "21")

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Hero header ──────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0D3B6E), Color(0xFF1565C0), Color(0xFF00838F)),
                                Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(.06f), size.width * .55f,
                            Offset(size.width * .88f, size.height * .2f))
                        drawCircle(C_Cyan.copy(.10f), size.width * .32f,
                            Offset(size.width * .08f, size.height * .92f))
                    }
                    // Chip total di kanan atas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 18.dp, end = 18.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(.14f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Minggu ini", fontSize = 10.sp, color = Color.White.copy(.65f))
                            Text("${String.format("%.1f", totalL)} L",
                                fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                color = Color.White)
                        }
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .padding(start = 20.dp, bottom = 20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropIconStat(Modifier.size(14.dp), C_Cyan)
                            Spacer(Modifier.width(5.dp))
                            Text("HIDRASI", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                color = C_Cyan, letterSpacing = 2.sp)
                        }
                        Spacer(Modifier.height(3.dp))
                        Text("Statistik Mingguan", fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Lacak hidrasi Anda sepanjang minggu",
                            fontSize = 13.sp, color = Color.White.copy(.65f))
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Mini stat row ────────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniStatCard(Modifier.weight(1f),
                        icon  = { ChartBarIcon(Modifier.size(20.dp), C_Cyan) },
                        label = "Rata-rata/hari",
                        value = "${String.format("%.1f", avgL)} L",
                        tint  = C_Cyan)
                    MiniStatCard(Modifier.weight(1f),
                        icon  = { FlameIcon(Modifier.size(20.dp)) },
                        label = "Rekor Terbaik",
                        value = "${String.format("%.1f", recordL)} L",
                        tint  = C_Orange)
                    MiniStatCard(Modifier.weight(1f),
                        icon  = { TargetCircleIcon(Modifier.size(20.dp)) },
                        label = "Target Harian",
                        value = "${String.format("%.1f", targetIntake / 1000.0)} L",
                        tint  = C_Red)
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Health Tips ──────────────────────────────────────────────────
            item {
                HealthTipsCard(onReadMore = onReadMore)
                Spacer(Modifier.height(16.dp))
            }

            // ── Bar chart ────────────────────────────────────────────────────
            item {
                HydrationChartCard(weekDays, dayLabels, targetIntake, totalL)
                Spacer(Modifier.height(16.dp))
            }

            // ── Progress ─────────────────────────────────────────────────────
            item {
                ProgressVsTargetCard(weekDays, dayLabels, targetIntake)
                Spacer(Modifier.height(16.dp))
            }

            // ── Pola jam ─────────────────────────────────────────────────────
            item {
                HourlyPatternCard(hourlyBuckets, hourLabels)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── MiniStatCard ─────────────────────────────────────────────────────────────
@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    tint: Color
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surface)
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(.12f)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 9.sp, color = cs.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold, letterSpacing = .3.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
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
    val cs       = MaterialTheme.colorScheme
    val todayDow = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp)).background(cs.surface)
    ) {
        // Accent bar kiri
        Box(
            modifier = Modifier.width(4.dp).height(56.dp).align(Alignment.TopStart)
                .clip(RoundedCornerShape(bottomEnd = 4.dp, topEnd = 4.dp))
                .background(Brush.verticalGradient(listOf(C_Cyan, C_MidBlue)))
        )
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(C_Red.copy(.12f)),
                    Alignment.Center) { TargetCircleIcon(Modifier.size(18.dp)) }
                Column {
                    Text("Progress vs Target", fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Text("Seberapa dekat tiap hari ke target", fontSize = 12.sp,
                        color = cs.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(18.dp))

            weekDays.forEachIndexed { idx, (_, intake) ->
                val pct      = (intake.toFloat() / targetIntake).coerceIn(0f, 1f)
                val isToday  = idx == todayDow
                val exceeded = intake >= targetIntake
                val hasData  = intake > 0

                val animPct by animateFloatAsState(pct,
                    tween(800 + idx * 60, easing = FastOutSlowInEasing), label = "p$idx")

                val barBrush: Brush? = when {
                    !hasData -> null
                    exceeded -> Brush.horizontalGradient(listOf(Color(0xFF00BCD4), C_Cyan))
                    isToday  -> Brush.horizontalGradient(listOf(C_MidBlue, C_Light))
                    else     -> Brush.horizontalGradient(listOf(C_Light.copy(.45f), C_Light.copy(.7f)))
                }

                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(dayLabels[idx], fontSize = 12.sp,
                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isToday) C_Cyan else cs.onSurfaceVariant,
                        modifier = Modifier.width(34.dp))
                    Box(
                        Modifier.weight(1f).height(if (isToday) 14.dp else 10.dp)
                            .clip(RoundedCornerShape(50)).background(cs.surfaceVariant)
                    ) {
                        if (hasData && barBrush != null)
                            Box(Modifier.fillMaxHeight().fillMaxWidth(animPct)
                                .clip(RoundedCornerShape(50)).background(barBrush))
                    }
                    Text(if (!hasData) "–" else "${(pct * 100).toInt()}%",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (exceeded) C_Cyan else cs.onSurfaceVariant,
                        modifier = Modifier.width(38.dp), textAlign = TextAlign.End)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(C_Cyan))
                    Spacer(Modifier.width(4.dp))
                    Text("Target tercapai", fontSize = 10.sp, color = cs.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(C_Light.copy(.65f)))
                    Spacer(Modifier.width(4.dp))
                    Text("Sebagian", fontSize = 10.sp, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

// ─── HourlyPatternCard ────────────────────────────────────────────────────────
@Composable
fun HourlyPatternCard(hourlyBuckets: IntArray, hourLabels: List<String>) {
    val cs         = MaterialTheme.colorScheme
    val maxBucket  = hourlyBuckets.max().takeIf { it > 0 } ?: 1
    val peakBucket = hourlyBuckets.indexOfFirst { it == hourlyBuckets.max() }
    val peakLabel  = if (hourlyBuckets.max() > 0) {
        "${String.format("%02d", peakBucket * 3)}.00–${String.format("%02d", peakBucket * 3 + 3)}.00"
    } else "--"

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(
                listOf(Color(0xFF0D2B5E), Color(0xFF0A1A3E)),
                Offset.Zero, Offset(0f, Float.POSITIVE_INFINITY)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(.04f), size.width * .45f,
                Offset(size.width * .9f, size.height * .12f))
        }
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(C_Cyan.copy(.18f)),
                        Alignment.Center) { ClockIconStat(Modifier.size(18.dp)) }
                    Column {
                        Text("Pola Jam Minum", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Distribusi minum hari ini", fontSize = 12.sp,
                            color = Color.White.copy(.5f))
                    }
                }
                if (hourlyBuckets.max() > 0) Column(horizontalAlignment = Alignment.End) {
                    Text("PALING AKTIF", fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = C_Cyan, letterSpacing = .5.sp)
                    Text(peakLabel, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White)
                }
            }
            Spacer(Modifier.height(20.dp))

            if (hourlyBuckets.max() == 0) {
                Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DropIconStat(Modifier.size(30.dp), Color.White.copy(.2f))
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada data minum hari ini",
                            color = Color.White.copy(.45f), fontSize = 13.sp)
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth().height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.Bottom) {
                    hourlyBuckets.forEachIndexed { idx, amount ->
                        val isPeak  = idx == peakBucket && amount > 0
                        val animFrac by animateFloatAsState(
                            amount.toFloat() / maxBucket,
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                            label = "h$idx")
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.width(22.dp).weight(1f).clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(.08f)),
                                Alignment.BottomCenter
                            ) {
                                Box(Modifier.fillMaxWidth().fillMaxHeight(animFrac)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isPeak) Brush.verticalGradient(listOf(C_Cyan.copy(.65f), C_Cyan))
                                        else Brush.verticalGradient(listOf(C_Light.copy(.3f), C_Light.copy(.55f)))
                                    ))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    hourLabels.forEach { label ->
                        Text(label, fontSize = 9.sp, color = Color.White.copy(.4f),
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
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
    totalL: Double
) {
    val cs             = MaterialTheme.colorScheme
    val todayDow       = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val max            = maxOf(weekDays.maxOf { it.second }, targetIntake).toFloat()
    val targetFraction = targetIntake / max

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp)).background(cs.surface)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).clip(CircleShape).background(C_MidBlue.copy(.12f)),
                        Alignment.Center) { ChartBarIcon(Modifier.size(18.dp), C_MidBlue) }
                    Column {
                        Text("Aktivitas Hidrasi", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Text("7 hari terakhir", fontSize = 12.sp, color = cs.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL MINGGU INI", fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        color = cs.onSurfaceVariant, letterSpacing = .5.sp)
                    Text("${String.format("%.1f", totalL)} L", fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(20.dp))

            val hasData = weekDays.any { it.second > 0 }
            if (!hasData) {
                Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DropIconStat(Modifier.size(36.dp), cs.onSurfaceVariant.copy(.25f))
                        Spacer(Modifier.height(8.dp))
                        Text("Belum ada data minggu ini", color = cs.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().height(170.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val yTarget = size.height * (1f - targetFraction)
                        drawLine(C_Red.copy(.45f), Offset(0f, yTarget), Offset(size.width, yTarget),
                            1.5.dp.toPx(), StrokeCap.Round,
                            PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                    }
                    Text("TARGET (${String.format("%.1f", targetIntake / 1000.0)}L)",
                        fontSize = 9.sp, color = C_Red.copy(.65f),
                        modifier = Modifier.padding(
                            top = (170 * (1f - targetFraction) - 14).coerceAtLeast(0f).dp))

                    Row(Modifier.fillMaxSize().padding(top = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        weekDays.forEachIndexed { idx, (_, value) ->
                            val isToday = idx == todayDow
                            val animFrac by animateFloatAsState(value / max,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                                label = "b$idx")
                            val barBrush = when {
                                value == 0         -> Brush.verticalGradient(listOf(cs.surfaceVariant, cs.surfaceVariant))
                                value >= targetIntake -> Brush.verticalGradient(listOf(Color(0xFF00BCD4).copy(.7f), C_Cyan))
                                isToday            -> Brush.verticalGradient(listOf(C_Light.copy(.6f), C_MidBlue))
                                else               -> Brush.verticalGradient(listOf(Color(0xFFBBDEFB).copy(.4f), C_Light.copy(.7f)))
                            }
                            Box(
                                Modifier.weight(1f)
                                    .fillMaxHeight(animFrac.coerceAtLeast(.03f))
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barBrush)
                            ) {
                                if (value > 0)
                                    Box(Modifier.fillMaxWidth().height(4.dp).align(Alignment.TopCenter)
                                        .background(Color.White.copy(.28f)))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEachIndexed { idx, label ->
                        val isToday = idx == todayDow
                        Text(label, fontSize = 10.sp,
                            color = if (isToday) C_MidBlue else cs.onSurfaceVariant,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ─── SmallStatCard (legacy) ───────────────────────────────────────────────────
@Composable
fun SmallStatCard(modifier: Modifier = Modifier, icon: String, label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Card(modifier, RoundedCornerShape(20.dp),
        CardDefaults.cardColors(cs.surface), CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                color = cs.onSurfaceVariant, letterSpacing = .5.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
        }
    }
}

// ─── HealthTipsCard ───────────────────────────────────────────────────────────
@Composable
fun HealthTipsCard(onReadMore: () -> Unit = {}) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp)).background(cs.surface)
    ) {
        Column {
            // Hero gradient diagonal
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Brush.linearGradient(
                        listOf(Color(0xFF003D6B), Color(0xFF005F73), Color(0xFF00838F)),
                        Offset.Zero, Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY))),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(.05f), size.width * .48f,
                        Offset(size.width * .85f, size.height * .3f))
                    drawCircle(Color(0xFF00BCD4).copy(.10f), size.width * .28f,
                        Offset(size.width * .1f, size.height * .8f))
                }
                DropIconStat(Modifier.size(68.dp), Color(0xFF4DD0E1))
            }

            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(C_MidBlue, Color(0xFF006064))))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("HEALTH TIPS", fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White, letterSpacing = 1.sp)
                    }
                    DropIconStat(Modifier.size(12.dp), C_MidBlue.copy(.35f))
                }
                Spacer(Modifier.height(10.dp))
                Text("Pentingnya Air Saat Pagi", fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Spacer(Modifier.height(7.dp))
                Text("Memulai hari dengan segelas air hangat dapat membantu mengaktifkan organ internal dan membuang racun sebelum mengonsumsi makanan pertama.",
                    fontSize = 13.sp, color = cs.onSurfaceVariant, lineHeight = 20.sp)
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50))
                        .background(C_MidBlue.copy(.10f))
                        .clickable { onReadMore() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Baca Selengkapnya", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = C_MidBlue)
                        Text("→", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = C_MidBlue)
                    }
                }
            }
        }
    }
}
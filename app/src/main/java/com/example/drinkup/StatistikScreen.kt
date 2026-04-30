package com.example.drinkup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatistikScreen(
    intakeViewModel: IntakeViewModel
) {
    val colorScheme = MaterialTheme.colorScheme
    val intakeState by intakeViewModel.state.collectAsState()

    // ── Data dari ViewModel ────────────────────────────────────────────────
    val history      = intakeState.history          // "MM-dd" -> total ml
    val weeklyHistory = intakeState.weeklyHistory   // "yyyy-MM-dd" -> total ml
    val targetIntake = intakeState.userTarget
    val todayEntries = intakeState.todayEntries

    // ── Bangun data 7 hari (Senin s/d hari ini) dari weeklyHistory ──────────
    val sdf          = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDow     = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val mondayOffset = -todayDow
    val weekDays     = (0..6).map { i ->
        val c       = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, mondayOffset + i) }
        val dateKey = sdf.format(c.time)
        val intake  = weeklyHistory[dateKey] ?: 0
        Pair(dateKey, intake)
    }
    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    val weeklyTotal = weekDays.sumOf { it.second }
    val recordMl    = weeklyHistory.values.maxOrNull() ?: 0
    val avgMl       = weeklyTotal / 7

    val avgL    = avgMl    / 1000.0
    val recordL = recordMl / 1000.0
    val totalL  = weeklyTotal / 1000.0

    // ── Pola jam minum dari entries hari ini ─────────────────────────────────
    // Kelompokkan per 3 jam: 0-3, 3-6, 6-9, 9-12, 12-15, 15-18, 18-21, 21-24
    val hourlyBuckets = IntArray(8) { 0 }
    todayEntries.forEach { entry ->
        val hour   = Calendar.getInstance().also { it.timeInMillis = entry.timestamp }.get(Calendar.HOUR_OF_DAY)
        val bucket = (hour / 3).coerceIn(0, 7)
        hourlyBuckets[bucket] += entry.amount
    }
    val hourLabels = listOf("00", "03", "06", "09", "12", "15", "18", "21")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Page title ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "Statistik Mingguan",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lacak hidrasi Anda sepanjang minggu.",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Top two stat cards ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎯",
                        label = "TARGET HARI INI",
                        value = "${String.format("%.1f", targetIntake / 1000.0)} L"
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📈",
                        label = "RATA-RATA",
                        value = "${String.format("%.1f", avgL)} L"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Rekor terbaik ────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REKOR TERBAIK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${String.format("%.1f", recordL)} L",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Aktivitas Hidrasi chart card ─────────────────────────────────
            item {
                HydrationChartCard(
                    weekDays    = weekDays,
                    dayLabels   = dayLabels,
                    targetIntake = targetIntake,
                    totalL      = totalL
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Progress vs Target ───────────────────────────────────────────
            item {
                ProgressVsTargetCard(
                    weekDays     = weekDays,
                    dayLabels    = dayLabels,
                    targetIntake = targetIntake
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Pola Jam Minum ───────────────────────────────────────────────
            item {
                HourlyPatternCard(
                    hourlyBuckets = hourlyBuckets,
                    hourLabels    = hourLabels
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Health Tips card ─────────────────────────────────────────────
            item {
                HealthTipsCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Progress vs Target Card ────────────────────────────────────────────────────
@Composable
fun ProgressVsTargetCard(
    weekDays     : List<Pair<String, Int>>,
    dayLabels    : List<String>,
    targetIntake : Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val todayDow    = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Progress vs Target",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                text = "Seberapa dekat tiap hari ke target",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            weekDays.forEachIndexed { idx, (_, intake) ->
                val pct      = (intake.toFloat() / targetIntake).coerceIn(0f, 1f)
                val isToday  = idx == todayDow
                val exceeded = intake >= targetIntake

                val animPct by animateFloatAsState(
                    targetValue   = pct,
                    animationSpec = tween(800 + idx * 60, easing = FastOutSlowInEasing),
                    label         = "prog$idx"
                )

                val barColor = when {
                    intake == 0 -> colorScheme.surfaceVariant
                    exceeded    -> Color(0xFF26C6DA)
                    isToday     -> Color(0xFF4FC3F7)
                    else        -> Color(0xFF4FC3F7).copy(alpha = 0.6f)
                }

                Row(
                    modifier          = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayLabels[idx],
                        fontSize = 12.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(32.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animPct)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(barColor.copy(alpha = 0.7f), barColor)
                                    )
                                )
                        )
                    }

                    Text(
                        text = if (intake == 0) "--" else "${(pct * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (exceeded) Color(0xFF26C6DA) else colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF26C6DA))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Target tercapai", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF4FC3F7).copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Sebagian", fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Hourly Pattern Card ────────────────────────────────────────────────────────
@Composable
fun HourlyPatternCard(
    hourlyBuckets : IntArray,
    hourLabels    : List<String>
) {
    val colorScheme = MaterialTheme.colorScheme
    val maxBucket   = hourlyBuckets.max().takeIf { it > 0 } ?: 1
    val peakBucket  = hourlyBuckets.indexOfFirst { it == hourlyBuckets.max() }

    val peakLabel = if (hourlyBuckets.max() > 0) {
        val start = peakBucket * 3
        val end   = start + 3
        "${String.format("%02d", start)}.00–${String.format("%02d", end)}.00"
    } else "--"

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Pola Jam Minum",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Distribusi minum hari ini",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                if (hourlyBuckets.max() > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PALING AKTIF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = peakLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (hourlyBuckets.max() == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada data minum hari ini",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier              = Modifier.fillMaxWidth().height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.Bottom
                ) {
                    hourlyBuckets.forEachIndexed { idx, amount ->
                        val frac    = amount.toFloat() / maxBucket
                        val isPeak  = idx == peakBucket && amount > 0

                        val animFrac by animateFloatAsState(
                            targetValue   = frac,
                            animationSpec = tween(700 + idx * 50, easing = FastOutSlowInEasing),
                            label         = "hour$idx"
                        )

                        val barColor = if (isPeak) Color(0xFF26C6DA) else Color(0xFF4FC3F7).copy(alpha = 0.5f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier            = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Box(
                                modifier         = Modifier
                                    .width(24.dp)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .background(colorScheme.surfaceVariant),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animFrac)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(barColor.copy(alpha = 0.7f), barColor)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hourLabels.forEach { label ->
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ── Hydration Chart Card (updated pakai weekDays) ─────────────────────────────
@Composable
fun HydrationChartCard(
    weekDays     : List<Pair<String, Int>>,
    dayLabels    : List<String>,
    targetIntake : Int,
    totalL       : Double
) {
    val colorScheme  = MaterialTheme.colorScheme
    val todayDow     = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val max          = maxOf(weekDays.maxOf { it.second }, targetIntake).toFloat()
    val barReached   = colorScheme.primary
    val barPartial   = colorScheme.surfaceVariant

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column {
                    Text("Aktivitas Hidrasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    Text("7 hari terakhir", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL MINGGU INI", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                    Text("${String.format("%.1f", totalL)} L", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val hasData = weekDays.any { it.second > 0 }
            if (!hasData) {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada data 😢", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            } else {
                val targetFraction = targetIntake / max
                val dashColor = colorScheme.outline

                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val yTarget = size.height * (1f - targetFraction)
                        drawLine(
                            color       = dashColor,
                            start       = Offset(0f, yTarget),
                            end         = Offset(size.width, yTarget),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                            cap         = StrokeCap.Round
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        Text(
                            text     = "TARGET (${String.format("%.1f", targetIntake / 1000.0)}L)",
                            fontSize = 9.sp,
                            color    = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = (160 * (1f - targetFraction) - 14).coerceAtLeast(0f).dp)
                        )
                    }

                    Row(
                        modifier              = Modifier.fillMaxSize().padding(top = 4.dp),
                        verticalAlignment     = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weekDays.forEachIndexed { idx, (_, value) ->
                            val fraction = value / max
                            val animatedFraction by animateFloatAsState(
                                targetValue   = fraction,
                                animationSpec = tween(700 + idx * 80),
                                label         = "bar_$idx"
                            )
                            val isToday  = idx == todayDow
                            val barColor = when {
                                value == 0         -> barPartial
                                value >= targetIntake -> barReached
                                isToday            -> Color(0xFF4FC3F7)
                                else               -> barPartial
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text      = label.uppercase(),
                            fontSize  = 10.sp,
                            color     = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text          = label,
                fontSize      = 10.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = value,
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HealthTipsCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1A5F6B), Color(0xFF0D3D4A)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 64.sp)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text          = "HEALTH TIPS",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = colorScheme.onSecondaryContainer,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text       = "Pentingnya Air Saat Pagi",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text       = "Memulai hari dengan segelas air hangat dapat membantu mengaktifkan organ internal dan membuang racun sebelum Anda mengonsumsi makanan pertama.",
                    fontSize   = 13.sp,
                    color      = colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "Baca Selengkapnya →",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.primary
                )
            }
        }
    }
}
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.math.*

// Warna air tetap hardcoded (warna visual animasi)
private val WgWater1 = Color(0xFF4FC3F7)
private val WgTeal   = Color(0xFF26C6DA)

private data class DayData(
    val label   : String,
    val intake  : Int,
    val target  : Int = 2000
)

@Composable
fun WeeklyGoalScreen(
    currentIntake : Int,
    targetIntake  : Int,
    onBack        : () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val days = listOf(
        DayData("Sen", 2200, targetIntake),
        DayData("Sel", 1800, targetIntake),
        DayData("Rab", 2400, targetIntake),
        DayData("Kam", 1600, targetIntake),
        DayData("Jum", currentIntake, targetIntake),
        DayData("Sab", 0, targetIntake),
        DayData("Min", 0, targetIntake),
    )
    val todayIndex = (dayOfWeek + 5) % 7

    val weeklyTotal  = days.sumOf { it.intake }
    val weeklyTarget = targetIntake * 7
    val weeklyPct    = (weeklyTotal.toFloat() / weeklyTarget).coerceIn(0f, 1f)
    val weeklyPctInt = (weeklyPct * 100).toInt()

    val animArc by animateFloatAsState(
        targetValue   = weeklyPct,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label         = "arc"
    )

    val motivasi = when {
        weeklyPctInt >= 100 -> "Luar biasa! Target minggu ini tercapai! 🎉"
        weeklyPctInt >= 75  -> "Hampir sampai! Terus semangat! 💪"
        weeklyPctInt >= 50  -> "Setengah jalan, jangan berhenti! 🚀"
        weeklyPctInt >= 25  -> "Awal yang baik, pertahankan ritme ini!"
        else                -> "Mulai hari ini, tubuhmu butuh air! 💧"
    }

    data class Badge(val emoji: String, val title: String, val desc: String, val unlocked: Boolean)
    val badges = listOf(
        Badge("🚀", "Fast Starter",    "Capai 100% target di Senin & Selasa", days[0].intake >= days[0].target && days[1].intake >= days[1].target),
        Badge("⚡", "Midweek Hero",    "Capai 100% target di Rabu & Kamis",   days[2].intake >= days[2].target && days[3].intake >= days[3].target),
        Badge("🏆", "Weekly Warrior",  "Capai 100% target selama 7 hari",     days.all { it.intake >= it.target }),
        Badge("💎", "Hydration Elite", "Total mingguan melebihi 15 liter",    weeklyTotal >= 15000),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER ───────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, null, tint = colorScheme.onSurface, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Tujuan Mingguan",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))

            Text(
                motivasi,
                style = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurfaceVariant)
            )

            Spacer(Modifier.height(20.dp))

            // ── CARD UTAMA — ARC PROGRESS ────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                color    = colorScheme.primaryContainer
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "TARGET MINGGUAN",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall.copy(
                                color         = colorScheme.onPrimaryContainer,
                                letterSpacing = 1.sp,
                                fontWeight    = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$weeklyPctInt",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color      = colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 64.sp
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "%",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color      = WgWater1,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        "Kamu sudah minum ${weeklyTotal / 1000f}L dari ${weeklyTarget / 1000f}L target mingguan.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier         = Modifier
                            .size(140.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke   = 16.dp.toPx()
                            val padding  = stroke / 2f
                            val arcRect  = androidx.compose.ui.geometry.Rect(
                                left   = padding,
                                top    = padding,
                                right  = size.width  - padding,
                                bottom = size.height - padding
                            )
                            drawArc(
                                color      = Color.White.copy(alpha = 0.18f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter  = false,
                                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                topLeft    = arcRect.topLeft,
                                size       = arcRect.size
                            )
                            drawArc(
                                brush      = Brush.sweepGradient(listOf(WgWater1, WgTeal, WgWater1)),
                                startAngle = 135f,
                                sweepAngle = 270f * animArc,
                                useCenter  = false,
                                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                topLeft    = arcRect.topLeft,
                                size       = arcRect.size
                            )
                        }
                        Text("💧", fontSize = 32.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── HYDRATION WAVES (BAR CHART) ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = colorScheme.surface,
                border   = BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val bestDay = days.maxByOrNull { it.intake }
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hydration Waves",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color      = colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "HARI TERBAIK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                bestDay?.label ?: "-",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    val maxIntake = days.maxOf { it.target }.toFloat()
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        days.forEachIndexed { idx, day ->
                            val barFrac  = (day.intake / maxIntake).coerceIn(0f, 1f)
                            val isToday  = idx == todayIndex
                            val barColor = when {
                                day.intake == 0 -> colorScheme.surfaceVariant
                                isToday         -> WgTeal
                                else            -> WgWater1
                            }

                            val animBar by animateFloatAsState(
                                targetValue   = barFrac,
                                animationSpec = tween(900 + idx * 80, easing = FastOutSlowInEasing),
                                label         = "bar$idx"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier            = Modifier.fillMaxHeight()
                            ) {
                                Box(
                                    modifier         = Modifier
                                        .width(28.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .background(colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animBar)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(barColor.copy(alpha = 0.7f), barColor)
                                                )
                                            )
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    day.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color      = if (isToday) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── RINGKASAN STATISTIK ──────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val daysHit = days.count { it.intake >= it.target && it.intake > 0 }
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon     = "✅",
                    label    = "HARI TERCAPAI",
                    value    = "$daysHit / 7"
                )
                val activeDays = days.count { it.intake > 0 }
                val avgIntake  = if (activeDays > 0) weeklyTotal / activeDays else 0
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon     = "📊",
                    label    = "RATA-RATA/HARI",
                    value    = "${avgIntake}ml"
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── CONSISTENCY INSIGHT ──────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = colorScheme.surface,
                border   = BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Row(
                    modifier          = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("✨", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Consistency Insight",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color      = colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Kamu paling aktif minum antara pukul 08.00–11.00. " +
                                    "Pertahankan ritme ini untuk menjaga hidrasi optimal sepanjang hari!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── WEEKLY BADGES ────────────────────────────────────────────────
            Text(
                "Weekly Badges",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = colorScheme.onBackground,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(14.dp))

            badges.forEach { badge ->
                BadgeRow(
                    emoji    = badge.emoji,
                    title    = badge.title,
                    desc     = badge.desc,
                    unlocked = badge.unlocked
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatMiniCard(
    modifier : Modifier,
    icon     : String,
    label    : String,
    value    : String
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(18.dp),
        color    = colorScheme.surface,
        border   = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    fontWeight    = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

@Composable
private fun BadgeRow(
    emoji    : String,
    title    : String,
    desc     : String,
    unlocked : Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor   = if (unlocked) colorScheme.tertiaryContainer else colorScheme.surfaceVariant
    val textColor = if (unlocked) colorScheme.onTertiaryContainer else colorScheme.onSurfaceVariant
    val iconAlpha = if (unlocked) 1f else 0.35f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = bgColor,
        border   = if (unlocked) BorderStroke(1.dp, WgWater1.copy(alpha = 0.4f))
        else BorderStroke(1.dp, colorScheme.outline)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) WgWater1.copy(alpha = 0.20f)
                        else colorScheme.outline.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    emoji,
                    fontSize = 22.sp,
                    color    = Color.Unspecified.copy(alpha = iconAlpha)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color      = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            if (unlocked) {
                Surface(
                    shape = CircleShape,
                    color = WgTeal.copy(alpha = 0.15f)
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle, null,
                        tint     = WgTeal,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(20.dp)
                    )
                }
            } else {
                Text(
                    "LOCKED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color         = colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        fontWeight    = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
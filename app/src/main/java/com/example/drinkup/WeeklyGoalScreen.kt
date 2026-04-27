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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.math.*

// ── Warna konsisten dengan DashboardScreen ──────────────────────────────────
private val WgBg      = Color(0xFFF0F4F8)
private val WgNavy    = Color(0xFF0D2D5E)
private val WgBlue    = Color(0xFF1565C0)
private val WgCard    = Color(0xFFFFFFFF)
private val WgHint    = Color(0xFF90A4AE)
private val WgWater1  = Color(0xFF4FC3F7)
private val WgWater2  = Color(0xFF0288D1)
private val WgTeal    = Color(0xFF26C6DA)
private val WgWeekly  = Color(0xFFE3F2FD)

// ── Model data hari ─────────────────────────────────────────────────────────
private data class DayData(
    val label   : String,
    val intake  : Int,      // ml
    val target  : Int = 2000
)

@Composable
fun WeeklyGoalScreen(
    currentIntake : Int,       // intake hari ini (ml)
    targetIntake  : Int,       // target harian (ml)
    onBack        : () -> Unit = {}
) {
    // Simulasi data 7 hari (Senin–Minggu)
    // Dalam implementasi nyata, ambil dari ViewModel/Firestore
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
    val todayIndex = (dayOfWeek + 5) % 7  // Senin=0 … Minggu=6

    val weeklyTotal  = days.sumOf { it.intake }
    val weeklyTarget = targetIntake * 7
    val weeklyPct    = (weeklyTotal.toFloat() / weeklyTarget).coerceIn(0f, 1f)
    val weeklyPctInt = (weeklyPct * 100).toInt()

    // Animasi arc
    val animArc by animateFloatAsState(
        targetValue   = weeklyPct,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label         = "arc"
    )

    // Motivasi text
    val motivasi = when {
        weeklyPctInt >= 100 -> "Luar biasa! Target minggu ini tercapai! 🎉"
        weeklyPctInt >= 75  -> "Hampir sampai! Terus semangat! 💪"
        weeklyPctInt >= 50  -> "Setengah jalan, jangan berhenti! 🚀"
        weeklyPctInt >= 25  -> "Awal yang baik, pertahankan ritme ini!"
        else                -> "Mulai hari ini, tubuhmu butuh air! 💧"
    }

    // Badge data
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
            .background(WgBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER ───────────────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(WgCard)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WgBg)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, null, tint = WgNavy, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Tujuan Mingguan",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = WgNavy,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))

            // ── SUBTITLE MOTIVASI ─────────────────────────────────────────────
            Text(
                motivasi,
                style = MaterialTheme.typography.bodyMedium.copy(color = WgHint)
            )

            Spacer(Modifier.height(20.dp))

            // ── CARD UTAMA — ARC PROGRESS ────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(24.dp),
                color    = WgNavy
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Label chip
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "TARGET MINGGUAN",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelSmall.copy(
                                color         = Color.White,
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
                                color      = Color.White,
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
                            color      = Color.White.copy(alpha = 0.80f),
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Arc donut progress
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
                            // Track
                            drawArc(
                                color      = Color.White.copy(alpha = 0.18f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter  = false,
                                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                topLeft    = arcRect.topLeft,
                                size       = arcRect.size
                            )
                            // Progress
                            drawArc(
                                brush      = Brush.sweepGradient(
                                    listOf(WgWater1, WgTeal, WgWater1)
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animArc,
                                useCenter  = false,
                                style      = Stroke(width = stroke, cap = StrokeCap.Round),
                                topLeft    = arcRect.topLeft,
                                size       = arcRect.size
                            )
                        }
                        // Ikon tetes air di tengah
                        Text("💧", fontSize = 32.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── HYDRATION WAVES (BAR CHART) ──────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = WgCard,
                border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
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
                                color      = WgNavy,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "HARI TERBAIK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = WgHint,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                bestDay?.label ?: "-",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = WgNavy,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Bar chart
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
                                day.intake == 0 -> Color(0xFFE0E8F0)
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
                                // Bar (track + fill)
                                Box(
                                    modifier         = Modifier
                                        .width(28.dp)
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFFE0E8F0)),
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
                                        color      = if (isToday) WgNavy else WgHint,
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
                // Hari tercapai
                val daysHit = days.count { it.intake >= it.target && it.intake > 0 }
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon     = "✅",
                    label    = "HARI TERCAPAI",
                    value    = "$daysHit / 7"
                )
                // Rata-rata harian
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
                color    = WgCard,
                border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
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
                                color      = WgNavy,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Kamu paling aktif minum antara pukul 08.00–11.00. " +
                                    "Pertahankan ritme ini untuk menjaga hidrasi optimal sepanjang hari!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = WgHint,
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
                    color      = WgNavy,
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

// ── Komponen kecil: StatMiniCard ─────────────────────────────────────────────
@Composable
private fun StatMiniCard(
    modifier : Modifier,
    icon     : String,
    label    : String,
    value    : String
) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(18.dp),
        color    = WgCard,
        border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = WgHint,
                    letterSpacing = 0.5.sp,
                    fontWeight    = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = WgNavy,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }
    }
}

// ── Komponen kecil: BadgeRow ──────────────────────────────────────────────────
@Composable
private fun BadgeRow(
    emoji    : String,
    title    : String,
    desc     : String,
    unlocked : Boolean
) {
    val bgColor   = if (unlocked) WgWeekly else Color(0xFFF5F5F5)
    val textColor = if (unlocked) WgNavy   else WgHint
    val iconAlpha = if (unlocked) 1f        else 0.35f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = bgColor,
        border   = if (unlocked) BorderStroke(1.dp, WgWater1.copy(alpha = 0.4f))
        else BorderStroke(1.dp, Color(0xFFE8EEF4))
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikon badge
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (unlocked) WgWater1.copy(alpha = 0.20f)
                        else Color(0xFFE0E0E0)
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
                        color      = WgHint,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(Modifier.width(8.dp))

            // Status badge
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
                        color         = WgHint,
                        letterSpacing = 0.5.sp,
                        fontWeight    = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
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
import java.text.SimpleDateFormat
import java.util.*

// Warna air tetap hardcoded (warna visual animasi)
private val WgWater1 = Color(0xFF4FC3F7)
private val WgTeal   = Color(0xFF26C6DA)

// Label hari dalam seminggu (Senin=0 ... Minggu=6)
private val DAY_LABELS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

@Composable
fun WeeklyGoalScreen(
    intakeViewModel : IntakeViewModel,
    onBack          : () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val intakeState by intakeViewModel.state.collectAsState()

    // ── Bangun data 7 hari dari weeklyHistory (Firestore) ────────────────────
    val sdf        = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Hari ini dalam minggu: Senin=0 ... Minggu=6
    val todayDow   = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7

    // Urut dari Senin minggu ini hingga Minggu
    val mondayOffset = -todayDow
    val weekDays = (0..6).map { i ->
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

    val daysHit       = weekDays.count { it.third >= targetIntake && it.third > 0 }
    val activeDays    = weekDays.count { it.third > 0 }
    val avgIntake     = weeklyTotal / 7
    val bestLabel     = weekDays.maxByOrNull { it.third }?.first ?: "-"

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── HEADER ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
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

                    val weeklyTotalL  = String.format("%.2f", weeklyTotal / 1000f)
                    val weeklyTargetL = String.format("%.1f", weeklyTarget / 1000f)
                    Text(
                        "Kamu sudah minum ${weeklyTotalL}L dari ${weeklyTargetL}L target mingguan.",
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
                            val stroke  = 16.dp.toPx()
                            val padding = stroke / 2f
                            val arcRect = androidx.compose.ui.geometry.Rect(
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
                                bestLabel,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    val maxBar = maxOf(weekDays.maxOf { it.third }, targetIntake).toFloat()
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        weekDays.forEachIndexed { idx, (label, _, intake) ->
                            val barFrac  = (intake / maxBar).coerceIn(0f, 1f)
                            val isToday  = idx == todayDow
                            val barColor = when {
                                intake == 0 -> colorScheme.surfaceVariant
                                isToday     -> WgTeal
                                else        -> WgWater1
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
                                    label,
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
                StatMiniCard(
                    modifier = Modifier.weight(1f),
                    icon     = "✅",
                    label    = "HARI TERCAPAI",
                    value    = "$daysHit / 7"
                )
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
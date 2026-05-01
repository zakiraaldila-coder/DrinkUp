package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─── Animated Flame ───────────────────────────────────────────────────────────
@Composable
fun FlameAnimation(modifier: Modifier = Modifier, size: Float = 120f) {
    val inf = rememberInfiniteTransition(label = "flame")

    val phase1 by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label         = "p1"
    )
    val phase2 by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label         = "p2"
    )
    val scaleAnim by inf.animateFloat(
        initialValue  = 0.97f,
        targetValue   = 1.03f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "scale"
    )

    Canvas(modifier = modifier.size((size * 1.2f).dp)) {
        val cx = this.size.width / 2f
        val cy = this.size.height * 0.88f
        val s  = size * density * scaleAnim

        // Outer glow
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(Color(0x33FF6B00), Color.Transparent),
                center = Offset(cx, cy - s * 0.2f),
                radius = s * 0.75f
            ),
            radius = s * 0.75f,
            center = Offset(cx, cy - s * 0.2f)
        )

        fun buildFlamePath(
            centerX: Float, baseY: Float,
            width: Float, height: Float,
            wobblePhase: Float
        ): Path {
            val path   = Path()
            val wobble = sin(wobblePhase) * width * 0.07f
            val tipX   = centerX + wobble * 0.5f
            val tipY   = baseY - height
            path.moveTo(centerX, baseY)
            path.cubicTo(
                centerX - width * 0.5f, baseY - height * 0.2f,
                tipX - width * 0.22f + sin(wobblePhase * 1.3f) * width * 0.05f, baseY - height * 0.65f,
                tipX, tipY
            )
            path.cubicTo(
                tipX + width * 0.22f + sin(wobblePhase * 0.9f) * width * 0.05f, baseY - height * 0.65f,
                centerX + width * 0.5f, baseY - height * 0.2f,
                centerX, baseY
            )
            path.close()
            return path
        }

        drawPath(buildFlamePath(cx, cy, s * 0.78f, s * 1.0f, phase1), Color(0xFFFF5722))
        drawPath(buildFlamePath(cx + sin(phase2) * s * 0.04f, cy, s * 0.55f, s * 0.82f, phase2 + 0.5f), Color(0xFFFF9800))
        drawPath(buildFlamePath(cx + sin(phase1 + 1f) * s * 0.03f, cy, s * 0.35f, s * 0.60f, phase1 + 1.2f), Color(0xFFFFCC02))

        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9C4), Color(0x00FFCC02)),
                center = Offset(cx, cy - s * 0.22f),
                radius = s * 0.16f
            ),
            radius = s * 0.16f,
            center = Offset(cx, cy - s * 0.22f)
        )
    }
}

// ─── Small Flame for calendar cells ──────────────────────────────────────────
@Composable
fun SmallFlame(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "sf")
    val p by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label         = "sfp"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height * 0.88f
        val s  = size.width

        fun flameLayer(w: Float, h: Float, ph: Float, col: Color) {
            val path   = Path()
            val wobble = sin(ph) * w * 0.08f
            val tipX   = cx + wobble * 0.4f
            val tipY   = cy - h
            path.moveTo(cx, cy)
            path.cubicTo(cx - w * 0.5f, cy - h * 0.2f, tipX - w * 0.22f, cy - h * 0.65f, tipX, tipY)
            path.cubicTo(tipX + w * 0.22f, cy - h * 0.65f, cx + w * 0.5f, cy - h * 0.2f, cx, cy)
            path.close()
            drawPath(path, col)
        }

        flameLayer(s * 0.75f, s * 0.95f, p, Color(0xFFFF5722))
        flameLayer(s * 0.52f, s * 0.75f, p + 0.5f, Color(0xFFFF9800))
        flameLayer(s * 0.30f, s * 0.52f, p + 1.2f, Color(0xFFFFCC02))
    }
}

// ─── Water Drop ───────────────────────────────────────────────────────────────
@Composable
fun WaterDropIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF42A5F5)) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val drop = Path()
        drop.moveTo(cx, 0f)
        drop.cubicTo(cx + size.width * 0.5f, size.height * 0.4f,
            cx + size.width * 0.5f, size.height * 0.75f, cx, size.height)
        drop.cubicTo(cx - size.width * 0.5f, size.height * 0.75f,
            cx - size.width * 0.5f, size.height * 0.4f, cx, 0f)
        drop.close()
        drawPath(drop, color)

        val shine = Path()
        shine.moveTo(cx - size.width * 0.08f, size.height * 0.25f)
        shine.cubicTo(cx - size.width * 0.18f, size.height * 0.42f,
            cx - size.width * 0.15f, size.height * 0.56f,
            cx - size.width * 0.06f, size.height * 0.56f)
        shine.cubicTo(cx - size.width * 0.02f, size.height * 0.56f,
            cx + size.width * 0.02f, size.height * 0.48f,
            cx - size.width * 0.08f, size.height * 0.25f)
        shine.close()
        drawPath(shine, Color.White.copy(alpha = 0.45f))
    }
}

// ─── Trophy ───────────────────────────────────────────────────────────────────
@Composable
fun TrophyIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFFB300)) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cup = Path()
        cup.moveTo(w * 0.18f, h * 0.05f)
        cup.lineTo(w * 0.82f, h * 0.05f)
        cup.cubicTo(w * 0.82f, h * 0.55f, w * 0.62f, h * 0.68f, w * 0.50f, h * 0.70f)
        cup.cubicTo(w * 0.38f, h * 0.68f, w * 0.18f, h * 0.55f, w * 0.18f, h * 0.05f)
        cup.close()
        drawPath(cup, color)

        drawArc(
            color = color, startAngle = 200f, sweepAngle = 160f, useCenter = false,
            topLeft = Offset(0f, h * 0.08f), size = Size(w * 0.26f, h * 0.34f),
            style = Stroke(width = w * 0.08f)
        )
        drawArc(
            color = color, startAngle = 200f, sweepAngle = -160f, useCenter = false,
            topLeft = Offset(w * 0.74f, h * 0.08f), size = Size(w * 0.26f, h * 0.34f),
            style = Stroke(width = w * 0.08f)
        )

        drawRect(color = color, topLeft = Offset(w * 0.41f, h * 0.70f), size = Size(w * 0.18f, h * 0.16f))
        drawRoundRect(color = color, topLeft = Offset(w * 0.25f, h * 0.84f),
            size = Size(w * 0.50f, h * 0.12f), cornerRadius = CornerRadius(4f, 4f))
    }
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────
@Composable
fun BarChartIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w      = size.width
        val h      = size.height
        val fracs  = listOf(0.5f, 0.75f, 1.0f)
        val colors = listOf(Color(0xFF4FC3F7), Color(0xFF29B6F6), Color(0xFF0288D1))
        val barW   = w * 0.22f
        val gap    = w * 0.08f
        val startX = (w - (fracs.size * barW + (fracs.size - 1) * gap)) / 2f

        fracs.forEachIndexed { i, frac ->
            val bh = h * frac * 0.85f
            drawRoundRect(
                color        = colors[i],
                topLeft      = Offset(startX + i * (barW + gap), h - bh),
                size         = Size(barW, bh),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

// ─── Target / Bullseye ───────────────────────────────────────────────────────
@Composable
fun TargetIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = minOf(size.width, size.height) / 2f
        drawCircle(Color(0xFFEF5350), radius = r,         center = Offset(cx, cy))
        drawCircle(Color.White,       radius = r * 0.65f, center = Offset(cx, cy))
        drawCircle(Color(0xFFEF5350), radius = r * 0.35f, center = Offset(cx, cy))
    }
}

// ─── StreakScreen ─────────────────────────────────────────────────────────────
@Composable
fun StreakScreen(
    onBack          : () -> Unit = {},
    intakeViewModel : IntakeViewModel = viewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val state       by intakeViewModel.state.collectAsState()

    val streak       = state.streak
    val bestStreak   = state.bestStreak
    val glassesMonth = state.glassesThisMonth
    val avgPerDay    = state.avgPerDayLiter

    val sdf       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDow  = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    val weekDays = (0..6).map { i ->
        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -todayDow + i) }
        Triple(
            dayLabels[i],
            cal.get(Calendar.DAY_OF_MONTH),
            (state.weeklyHistory[sdf.format(cal.time)] ?: 0) >= state.userTarget
        )
    }

    val nextMilestone = when {
        streak < 10  -> 10;  streak < 30  -> 30
        streak < 60  -> 60;  streak < 100 -> 100
        else         -> streak + 10
    }
    val prevMilestone = when {
        streak < 10  -> 0;   streak < 30  -> 10
        streak < 60  -> 30;  streak < 100 -> 60
        else         -> 100
    }
    val milestoneProgress =
        if (nextMilestone > prevMilestone)
            (streak - prevMilestone).toFloat() / (nextMilestone - prevMilestone)
        else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF3E0), colorScheme.background)))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // TOP BAR
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text("DrinkUp",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFF57C00), fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                    Text("Streak Kamu",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = colorScheme.onBackground, fontWeight = FontWeight.ExtraBold))
                }
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.align(Alignment.CenterEnd).size(42.dp)
                        .background(colorScheme.surface, RoundedCornerShape(14.dp))
                ) {
                    Icon(Icons.Rounded.ArrowBack, null,
                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {

                // FLAME HERO CARD
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.radialGradient(
                            colors = listOf(Color(0xFFFFE0B2), Color(0xFFFFF3E0)), radius = 600f))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FlameAnimation(size = 90f)
                        Spacer(Modifier.height(4.dp))
                        Text("$streak",
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = Color(0xFFE65100), fontWeight = FontWeight.Black))
                        Text("Hari Berturut-turut",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFF57C00), fontWeight = FontWeight.SemiBold))
                        Spacer(Modifier.height(10.dp))
                        val motivationText = when {
                            streak == 0 -> "Mulai streakmu hari ini!"
                            streak < 3  -> "Awal yang bagus, terus semangat!"
                            streak < 7  -> "Keren! Kamu sedang panas!"
                            streak < 14 -> "Luar biasa! Terus jaga ritme ini!"
                            streak < 30 -> "Konsisten sekali! Kamu juara!"
                            else        -> "Legenda hidrasi! Salut!"
                        }
                        Surface(shape = RoundedCornerShape(50), color = Color(0xFFF57C00).copy(alpha = 0.12f)) {
                            Text(motivationText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFBF360C), fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // MINGGU INI
                Text("MINGGU INI",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp))
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    weekDays.forEachIndexed { index, (label, dayNum, achieved) ->
                        val isToday = index == todayDow
                        val bgBrush: Brush = when {
                            isToday  -> Brush.verticalGradient(listOf(Color.White, Color.White))
                            achieved -> Brush.verticalGradient(listOf(Color(0xFFFF8F00), Color(0xFFE65100)))
                            else     -> Brush.verticalGradient(listOf(Color(0xFFFFCC80), Color(0xFFFFB74D)))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f).aspectRatio(0.60f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(brush = bgBrush)
                                .then(if (isToday)
                                    Modifier.border(2.dp, Color(0xFFF57C00), RoundedCornerShape(14.dp))
                                else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center) {
                                Text(label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isToday) Color(0xFFF57C00) else Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                Spacer(Modifier.height(4.dp))
                                if (achieved && !isToday) {
                                    SmallFlame(modifier = Modifier.size(18.dp))
                                } else {
                                    WaterDropIcon(
                                        modifier = Modifier.size(16.dp),
                                        color    = if (isToday) Color(0xFF42A5F5) else Color.White.copy(alpha = 0.7f))
                                }
                                Spacer(Modifier.height(3.dp))
                                Text("$dayNum",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isToday) colorScheme.onSurface else Color.White,
                                        fontWeight = FontWeight.ExtraBold))
                                Spacer(Modifier.height(4.dp))
                                Box(modifier = Modifier.size(4.dp).background(
                                    if (isToday) Color(0xFFF57C00) else Color.White.copy(alpha = 0.5f),
                                    CircleShape))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // STATS CARDS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    // Best Streak
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFFFF8F00), Color(0xFFE65100))))
                        .padding(14.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()) {
                            TrophyIcon(modifier = Modifier.size(28.dp), color = Color.White)
                            Spacer(Modifier.height(6.dp))
                            Text("$bestStreak",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White, fontWeight = FontWeight.ExtraBold))
                            Text("Streak Terbaik",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center)
                        }
                    }

                    // Gelas Bulan Ini
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                        color = colorScheme.surface, shadowElevation = 2.dp) {
                        Column(modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            WaterDropIcon(modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("$glassesMonth",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colorScheme.onSurface, fontWeight = FontWeight.ExtraBold))
                            Text("Gelas Bulan Ini",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center)
                        }
                    }

                    // Rata-rata/hari
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                        color = colorScheme.surface, shadowElevation = 2.dp) {
                        Column(modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            BarChartIcon(modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("${avgPerDay}L",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = colorScheme.onSurface, fontWeight = FontWeight.ExtraBold))
                            Text("Rata-rata/hari",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // PROGRESS MILESTONE
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                    color = colorScheme.surface, shadowElevation = 2.dp) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TargetIcon(modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Menuju Streak $nextMilestone Hari",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold))
                            }
                            Surface(shape = RoundedCornerShape(50),
                                color = Color(0xFFF57C00).copy(alpha = 0.12f)) {
                                Text("$streak/$nextMilestone",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFFF57C00), fontWeight = FontWeight.ExtraBold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(colorScheme.surfaceVariant)) {
                            val animProg by animateFloatAsState(
                                targetValue   = milestoneProgress.coerceIn(0f, 1f),
                                animationSpec = tween(1200, easing = FastOutSlowInEasing),
                                label         = "milestoneAnim"
                            )
                            Box(modifier = Modifier.fillMaxWidth(animProg).fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(
                                    listOf(Color(0xFFFFB74D), Color(0xFFE65100)))))
                        }

                        Spacer(Modifier.height(10.dp))

                        val remaining = nextMilestone - streak
                        Text(
                            if (remaining > 0) "$remaining hari lagi untuk mencapai milestone berikutnya!"
                            else "Kamu sudah mencapai milestone ini! Luar biasa!",
                            style = MaterialTheme.typography.bodySmall.copy(color = colorScheme.onSurfaceVariant))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // TOMBOL MINUM SEKARANG
                Button(
                    onClick  = onBack,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    WaterDropIcon(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Minum Sekarang & Jaga Streakmu!",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White, fontWeight = FontWeight.Bold))
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
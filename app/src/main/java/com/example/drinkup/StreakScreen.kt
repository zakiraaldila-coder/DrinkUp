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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─── Mascot pakai gambar PNG dari drawable ────────────────────────────────────
// Ukuran bertambah setiap 5 streak (base 80dp, +12dp per 5 streak, max 160dp)
// Overlay Canvas menambahkan efek glossy / 3D highlight agar lebih hidup
@Composable
fun MascotAnimation(
    modifier: Modifier = Modifier,
    streak: Int = 0
) {
    val levelStep    = streak / 5
    val mascotSizeDp = (80f + levelStep * 12f).coerceAtMost(160f).dp

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier         = Modifier.size(mascotSizeDp),
            contentAlignment = Alignment.Center
        ) {
            // Gambar maskot dasar
            Image(
                painter            = painterResource(id = R.drawable.mascot),
                contentDescription = "Maskot DrinkUp",
                modifier           = Modifier.fillMaxSize()
            )

            // ── Overlay Canvas: efek glossy 3D ──────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1) Highlight utama — oval putih transparan di kiri atas
                drawOval(
                    brush   = Brush.radialGradient(
                        colors  = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center  = Offset(w * 0.38f, h * 0.22f),
                        radius  = w * 0.32f
                    ),
                    topLeft = Offset(w * 0.12f, h * 0.06f),
                    size    = Size(w * 0.50f, h * 0.34f)
                )

                // 2) Specular kecil — titik paling terang (glint)
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.70f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.36f, h * 0.18f),
                        radius = w * 0.10f
                    ),
                    radius = w * 0.10f,
                    center = Offset(w * 0.36f, h * 0.18f)
                )

                // 3) Rim light bawah — bayangan biru gelap di bagian bawah
                drawOval(
                    brush   = Brush.radialGradient(
                        colors  = listOf(
                            Color(0xFF1A237E).copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center  = Offset(w * 0.50f, h * 0.88f),
                        radius  = w * 0.45f
                    ),
                    topLeft = Offset(w * 0.05f, h * 0.62f),
                    size    = Size(w * 0.90f, h * 0.38f)
                )

                // 4) Highlight telinga kiri atas — titik putih kecil
                drawCircle(
                    brush  = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.28f, h * 0.08f),
                        radius = w * 0.07f
                    ),
                    radius = w * 0.07f,
                    center = Offset(w * 0.28f, h * 0.08f)
                )
            }
        }
    }
}

// ─── Sparkle dekorasi bintang kecil ──────────────────────────────────────────
@Composable
fun SparkleDecor(modifier: Modifier = Modifier, color: Color = Color.White) {
    val inf = rememberInfiniteTransition(label = "sparkle")
    val alpha by inf.animateFloat(
        initialValue  = 0.2f,
        targetValue   = 0.85f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "sparkleFade"
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = size.width / 2f
        val path = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.25f, cy - r * 0.25f)
            lineTo(cx + r, cy)
            lineTo(cx + r * 0.25f, cy + r * 0.25f)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.25f, cy + r * 0.25f)
            lineTo(cx - r, cy)
            lineTo(cx - r * 0.25f, cy - r * 0.25f)
            close()
        }
        drawPath(path, color.copy(alpha = alpha))
    }
}

// ─── Small Flame for calendar cells (TIDAK DIUBAH) ───────────────────────────
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
        val cx   = size.width / 2f
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
        val w   = size.width
        val h   = size.height
        val cup = Path()
        cup.moveTo(w * 0.18f, h * 0.05f)
        cup.lineTo(w * 0.82f, h * 0.05f)
        cup.cubicTo(w * 0.82f, h * 0.55f, w * 0.62f, h * 0.68f, w * 0.50f, h * 0.70f)
        cup.cubicTo(w * 0.38f, h * 0.68f, w * 0.18f, h * 0.55f, w * 0.18f, h * 0.05f)
        cup.close()
        drawPath(cup, color)
        drawArc(color = color, startAngle = 200f, sweepAngle = 160f, useCenter = false,
            topLeft = Offset(0f, h * 0.08f), size = Size(w * 0.26f, h * 0.34f),
            style   = Stroke(width = w * 0.08f))
        drawArc(color = color, startAngle = 200f, sweepAngle = -160f, useCenter = false,
            topLeft = Offset(w * 0.74f, h * 0.08f), size = Size(w * 0.26f, h * 0.34f),
            style   = Stroke(width = w * 0.08f))
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
            drawRoundRect(color = colors[i],
                topLeft      = Offset(startX + i * (barW + gap), h - bh),
                size         = Size(barW, bh),
                cornerRadius = CornerRadius(3f, 3f))
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

    // ── DATA — LOGIC TIDAK DIUBAH ─────────────────────────────────────────────
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
    // ─────────────────────────────────────────────────────────────────────────

    val motivationText = when {
        streak == 0 -> "Mulai streakmu hari ini! 👋"
        streak < 3  -> "Awal yang bagus, terus semangat!"
        streak < 7  -> "Keren! Kamu sedang on fire!"
        streak < 14 -> "Luar biasa! Terus jaga ritme ini!"
        streak < 30 -> "Konsisten sekali! Kamu juara!"
        else        -> "Legenda hidrasi! Salut! 🏆"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1A35), Color(0xFF0E2040), Color(0xFF0B1A35))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TOP BAR ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        "DrinkUp",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color         = Color(0xFF00BFA5),
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        "Streak Kamu",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .background(Color(0xFF112545), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack, null,
                        tint     = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {

                // ── MASCOT HERO CARD ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF112545), Color(0xFF0E2040), Color(0xFF0B1A35)),
                                radius = 800f
                            )
                        )
                        .border(1.dp, Color(0xFF1B3560), RoundedCornerShape(32.dp))
                        .padding(vertical = 28.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dekorasi sparkle di sudut
                    SparkleDecor(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.TopStart)
                            .offset(x = 18.dp, y = 18.dp),
                        color    = Color(0xFF00BFA5).copy(alpha = 0.5f)
                    )
                    SparkleDecor(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-24).dp, y = 28.dp),
                        color    = Color(0xFF26C6DA).copy(alpha = 0.6f)
                    )
                    SparkleDecor(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 40.dp, y = (-20).dp),
                        color    = Color(0xFF4DD0C4).copy(alpha = 0.45f)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        // Bubble chat
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart   = 16.dp, topEnd    = 16.dp,
                                        bottomEnd  = 16.dp, bottomStart = 4.dp
                                    )
                                )
                                .background(Color(0xFF112545).copy(alpha = 0.95f))
                                .border(1.dp, Color(0xFF00BFA5).copy(alpha = 0.4f), RoundedCornerShape(
                                    topStart = 16.dp, topEnd = 16.dp,
                                    bottomEnd = 16.dp, bottomStart = 4.dp
                                ))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                motivationText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = Color(0xFF00BFA5),
                                    fontWeight = FontWeight.SemiBold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Mascot — dibesarkan
                        MascotAnimation(
                            modifier = Modifier.size(180.dp),
                            streak   = streak
                        )

                        Spacer(Modifier.height(2.dp))

                        // Angka streak
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "$streak",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color      = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "hari",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color      = Color(0xFF00BFA5),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Text(
                            "Berturut-turut",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color      = Color(0xFF8FA8C8),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Level badge (tampil jika sudah ada minimal 1 level)
                        val levelStep = streak / 5
                        if (levelStep > 0) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF00BFA5).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "⭐ Level ${levelStep + 1}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color      = Color(0xFF4DD0C4),
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── MINGGU INI (LOGIC TIDAK DIUBAH) ──────────────────────────
                Text(
                    "MINGGU INI",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color         = Color(0xFF8FA8C8),
                        fontWeight    = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    weekDays.forEachIndexed { index, (label, dayNum, achieved) ->
                        val isToday  = index == todayDow
                        val bgBrush: Brush = when {
                            isToday  -> Brush.verticalGradient(listOf(Color(0xFF112545), Color(0xFF0E2040)))
                            achieved -> Brush.verticalGradient(listOf(Color(0xFF00BFA5), Color(0xFF00897B)))
                            else     -> Brush.verticalGradient(listOf(Color(0xFF0F2040), Color(0xFF0B1A35)))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.60f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(brush = bgBrush)
                                .then(
                                    if (isToday) Modifier.border(2.dp, Color(0xFF00BFA5), RoundedCornerShape(14.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color      = if (isToday) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 9.sp
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                if (achieved && !isToday) {
                                    SmallFlame(modifier = Modifier.size(18.dp))
                                } else {
                                    WaterDropIcon(
                                        modifier = Modifier.size(16.dp),
                                        color    = if (isToday) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "$dayNum",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color      = if (isToday) Color(0xFF00BFA5) else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(
                                            if (isToday) Color(0xFF00BFA5) else Color.White.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── STATS CARDS (LOGIC TIDAK DIUBAH) ──────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Best Streak — teal dark
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFF00BFA5), Color(0xFF00897B))))
                            .padding(14.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier            = Modifier.fillMaxWidth()
                        ) {
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
                        color = Color(0xFF112545), shadowElevation = 0.dp) {
                        Column(modifier = Modifier.padding(14.dp)
                            .border(1.dp, Color(0xFF1B3560), RoundedCornerShape(20.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            WaterDropIcon(modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("$glassesMonth",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White, fontWeight = FontWeight.ExtraBold))
                            Text("Gelas Bulan Ini",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF8FA8C8), textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center)
                        }
                    }

                    // Rata-rata/hari
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF112545), shadowElevation = 0.dp) {
                        Column(modifier = Modifier.padding(14.dp)
                            .border(1.dp, Color(0xFF1B3560), RoundedCornerShape(20.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            BarChartIcon(modifier = Modifier.size(26.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("${avgPerDay}L",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.White, fontWeight = FontWeight.ExtraBold))
                            Text("Rata-rata/hari",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF8FA8C8), textAlign = TextAlign.Center),
                                textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── PROGRESS MILESTONE (LOGIC TIDAK DIUBAH) ───────────────────
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF112545), shadowElevation = 0.dp) {
                    Column(modifier = Modifier
                        .border(1.dp, Color(0xFF1B3560), RoundedCornerShape(22.dp))
                        .padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TargetIcon(modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Menuju Streak $nextMilestone Hari",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White, fontWeight = FontWeight.SemiBold))
                            }
                            Surface(shape = RoundedCornerShape(50),
                                color = Color(0xFF00BFA5).copy(alpha = 0.15f)) {
                                Text("$streak/$nextMilestone",
                                    style    = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF00BFA5), fontWeight = FontWeight.ExtraBold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.08f))) {
                            val animProg by animateFloatAsState(
                                targetValue   = milestoneProgress.coerceIn(0f, 1f),
                                animationSpec = tween(1200, easing = FastOutSlowInEasing),
                                label         = "milestoneAnim"
                            )
                            Box(modifier = Modifier
                                .fillMaxWidth(animProg)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF00BFA5), Color(0xFF26C6DA)))
                                ))
                        }

                        Spacer(Modifier.height(10.dp))

                        val remaining = nextMilestone - streak
                        Text(
                            if (remaining > 0) "$remaining hari lagi untuk mencapai milestone berikutnya!"
                            else "Kamu sudah mencapai milestone ini! Luar biasa!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8FA8C8))
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── TOMBOL MINUM SEKARANG ──────────────────────────────────────
                Button(
                    onClick  = onBack,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape    = RoundedCornerShape(50),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
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
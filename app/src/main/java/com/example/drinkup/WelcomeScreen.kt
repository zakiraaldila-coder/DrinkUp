package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drinkup.ui.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
// POPPINS FONT
// ─────────────────────────────────────────────────────────────
private val WPoppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ─────────────────────────────────────────────────────────────
// DATA MODEL SLIDE
// ─────────────────────────────────────────────────────────────
private data class OnboardSlide(
    val title       : String,
    val titleItalic : String,
    val subtitle    : String,
    val description : String,
    val ctaText     : String
)

private val slides = listOf(
    OnboardSlide(
        title       = "Lacak Konsumsi",
        titleItalic = "Harianmu",
        subtitle    = "Pantau setiap tetes air yang kamu minum dengan mudah dan akurat.",
        description = "",
        ctaText     = "Mulai Sekarang"
    ),
    OnboardSlide(
        title       = "Raih Target",
        titleItalic = "Hidrasimu",
        subtitle    = "Sesuaikan target harian berdasarkan berat badan dan aktivitasmu.",
        description = "",
        ctaText     = "Selanjutnya"
    ),
)

// ─────────────────────────────────────────────────────────────
// WELCOME SCREEN UTAMA
// ─────────────────────────────────────────────────────────────
@Composable
fun WelcomeScreen(
    onLoginClick    : () -> Unit,
    onRegisterClick : () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val scope       = rememberCoroutineScope()
    var dragOffset  by remember { mutableStateOf(0f) }

    // Animasi transisi halaman
    val animatedPage by animateFloatAsState(
        targetValue   = currentPage.toFloat(),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label         = "page"
    )

    // Animasi konten masuk
    var visible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        if (visible) 1f else 0f, tween(600), label = "alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    // Infinite animasi untuk ilustrasi
    val inf = rememberInfiniteTransition(label = "inf")
    val waveAnim by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )
    val floatAnim by inf.animateFloat(
        -8f, 8f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )
    val glowAnim by inf.animateFloat(
        0.6f, 1f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val slide = slides[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(
                    Color(0xFF08112A),   // SBgDeep atas
                    Color(0xFF0A1535),   // SBgMid tengah
                    Color(0xFF0D1A3E)    // bawah
                ))
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset < -80f && currentPage < slides.lastIndex ->
                                currentPage++
                            dragOffset >  80f && currentPage > 0 ->
                                currentPage--
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragOffset += delta }
                )
            }
    ) {
        // ── Blob dekorasi background ──────────────────────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-40).dp)
                .alpha(0.25f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00D4AA), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 100.dp)
                .alpha(0.15f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00BFFF), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .alpha(0.12f)
                .background(
                    Brush.radialGradient(listOf(Color(0xFF00D4AA), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .alpha(contentAlpha)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Logo header ───────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "DrinkUp",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color      = Color(0xFFFFFFFF),
                        fontFamily = WPoppins,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.weight(1f))
                // Lewati text (hanya slide 1 & 2)
                if (currentPage < slides.lastIndex) {
                    TextButton(onClick = { currentPage = slides.lastIndex }) {
                        Text(
                            text  = "Lewati",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = WPoppins
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Ilustrasi Canvas ──────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF0F2040), Color(0xFF0A1A30)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Canvas ilustrasi berbeda per slide
                when (currentPage) {
                    0 -> IllustrasiChart(
                        waveAnim  = waveAnim,
                        floatAnim = floatAnim,
                        glowAnim  = glowAnim
                    )
                    1 -> IllustrasiTarget(
                        floatAnim = floatAnim,
                        glowAnim  = glowAnim,
                        waveAnim  = waveAnim
                    )
                    2 -> IllustrasiStreak(
                        floatAnim = floatAnim,
                        glowAnim  = glowAnim,
                        waveAnim  = waveAnim
                    )
                }

                // Badge bawah — diperkecil dan diturunkan
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF0F2040))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier         = Modifier
                                .size(24.dp)
                                .background(Color(0xFF00D4AA).copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                when (currentPage) { 0 -> "📊"; else -> "🎯" },
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text  = when (currentPage) {
                                    0 -> "TARGET HARI INI"
                                    else -> "PROGRES MINGGU INI"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color         = Color(0xFF00D4AA),
                                    fontFamily    = WPoppins,
                                    fontWeight    = FontWeight.Bold,
                                    letterSpacing = 0.6.sp,
                                    fontSize      = 8.sp
                                )
                            )
                            Text(
                                text  = when (currentPage) {
                                    0 -> "2.5 Liter Tercapai"
                                    else -> "85% Target Mingguan"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = Color(0xFFFFFFFF),
                                    fontFamily = WPoppins,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Teks konten ───────────────────────────────────────
            Row {
                Text(
                    text  = slide.title + " ",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = Color(0xFFFFFFFF),
                        fontFamily = WPoppins,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            Text(
                text  = slide.titleItalic,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = Color(0xFF00D4AA),
                    fontFamily = WPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle  = FontStyle.Italic
                )
            )

            Spacer(Modifier.height(24.dp))

            // ── Dot indicator ─────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                slides.indices.forEach { i ->
                    val isActive = i == currentPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isActive) 28.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF00D4AA) else Color.White.copy(alpha = 0.20f))
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Spacer(Modifier.weight(1f))

            // ── Tombol CTA ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF00D4AA)),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick   = {
                        if (currentPage < slides.lastIndex) currentPage++
                        else onRegisterClick()
                    },
                    modifier  = Modifier.fillMaxSize(),
                    shape     = RoundedCornerShape(50.dp),
                ) {
                    Text(
                        text  = if (currentPage < slides.lastIndex) "Selanjutnya →" else "Mulai Sekarang →",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color      = Color(0xFF09122A),
                            fontFamily = WPoppins,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Link masuk ────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Sudah punya akun? ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color      = Color(0xFF8AAAC8),
                        fontFamily = WPoppins
                    )
                )
                TextButton(
                    onClick      = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text  = "Masuk",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = Color(0xFF00D4AA),
                            fontFamily = WPoppins,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 1 — Ilustrasi Bar Chart (Canvas)
// ─────────────────────────────────────────────────────────────
@Composable
private fun IllustrasiChart(
    waveAnim  : Float,
    floatAnim : Float,
    glowAnim  : Float
) {
    val barHeights = listOf(0.45f, 0.75f, 0.55f, 0.90f, 0.65f)
    val barColor   = Color(0xFF00D4AA)
    val barActive  = Color(0xFF26E5BC)

    val animBars = barHeights.mapIndexed { i, h ->
        val anim by animateFloatAsState(
            targetValue   = h,
            animationSpec = tween(800 + i * 120, easing = FastOutSlowInEasing),
            label         = "bar$i"
        )
        anim
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp, top = 20.dp, start = 24.dp, end = 24.dp)
    ) {
        val canvasW = size.width
        val canvasH = size.height
        val barCount = animBars.size
        val barW     = canvasW / (barCount * 2f)
        val gap      = barW * 0.6f

        // Grid lines
        repeat(4) { i ->
            val y = canvasH * (1f - (i + 1) / 4f)
            drawLine(
                color       = Color.White.copy(alpha = 0.08f),
                start       = Offset(0f, y),
                end         = Offset(canvasW, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Lingkaran dekoratif besar
        drawCircle(
            color  = Color.White.copy(alpha = 0.04f * glowAnim),
            radius = canvasH * 0.55f,
            center = Offset(canvasW * 0.5f, canvasH * 0.4f),
            style  = Stroke(width = 80.dp.toPx())
        )

        // Bar chart
        animBars.forEachIndexed { i, h ->
            val x   = gap + i * (barW + gap) + gap * 0.3f
            val barH = canvasH * h
            val top = canvasH - barH + floatAnim.dp.toPx() * 0.3f

            // Shadow bar
            drawRoundRect(
                color        = Color.Black.copy(alpha = 0.25f),
                topLeft      = Offset(x + 4.dp.toPx(), top + 4.dp.toPx()),
                size         = Size(barW, barH),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            // Bar utama dengan gradient
            val barGradient = Brush.verticalGradient(
                colors = if (i == 3)
                    listOf(Color(0xFF7FFFD4), barActive)
                else
                    listOf(barColor.copy(alpha = 0.9f), barColor.copy(alpha = 0.5f)),
                startY = top,
                endY   = canvasH
            )
            drawRoundRect(
                brush        = barGradient,
                topLeft      = Offset(x, top),
                size         = Size(barW, barH),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            // Highlight kecil di atas bar
            if (i == 3) {
                drawCircle(
                    color  = Color.White.copy(alpha = 0.9f),
                    radius = 4.dp.toPx(),
                    center = Offset(x + barW / 2, top - 8.dp.toPx())
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 2 — Ilustrasi Target / Radial Progress (Canvas)
// ─────────────────────────────────────────────────────────────
@Composable
private fun IllustrasiTarget(
    floatAnim : Float,
    glowAnim  : Float,
    waveAnim  : Float
) {
    val animProgress by animateFloatAsState(
        targetValue   = 0.85f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "radial"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp, top = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f + floatAnim.dp.toPx() * 0.4f
        val r  = minOf(size.width, size.height) * 0.38f

        // Ring luar (track)
        drawCircle(
            color  = Color.White.copy(alpha = 0.07f),
            radius = r + 18.dp.toPx(),
            center = Offset(cx, cy),
            style  = Stroke(width = 2.dp.toPx())
        )

        // Glow
        drawCircle(
            color  = Color(0xFF00D4AA).copy(alpha = 0.12f * glowAnim),
            radius = r * 1.5f,
            center = Offset(cx, cy)
        )

        // Track
        drawArc(
            color        = Color.White.copy(alpha = 0.1f),
            startAngle   = -90f,
            sweepAngle   = 360f,
            useCenter    = false,
            topLeft      = Offset(cx - r, cy - r),
            size         = Size(r * 2, r * 2),
            style        = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
        )

        // Progress arc
        val arcBrush = Brush.sweepGradient(
            colors = listOf(Color(0xFF00D4AA), Color(0xFF26E5BC), Color(0xFF00D4AA)),
            center = Offset(cx, cy)
        )
        drawArc(
            brush        = arcBrush,
            startAngle   = -90f,
            sweepAngle   = 360f * animProgress,
            useCenter    = false,
            topLeft      = Offset(cx - r, cy - r),
            size         = Size(r * 2, r * 2),
            style        = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
        )

        // Titik akhir progress
        val endAngle = Math.toRadians((-90f + 360f * animProgress).toDouble())
        val dotX     = cx + r * kotlin.math.cos(endAngle).toFloat()
        val dotY     = cy + r * kotlin.math.sin(endAngle).toFloat()
        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(dotX, dotY))
        drawCircle(color = Color(0xFF00D4AA), radius = 5.dp.toPx(), center = Offset(dotX, dotY))

        // Mini bars di dalam lingkaran
        val barsData = listOf(0.6f, 0.85f, 0.7f, 1.0f, 0.75f)
        val barW     = 12.dp.toPx()
        val barGap   = 18.dp.toPx()
        val totalW   = barsData.size * (barW + barGap) - barGap
        val startX   = cx - totalW / 2f
        val maxH     = r * 0.65f

        barsData.forEachIndexed { i, h ->
            val bx   = startX + i * (barW + barGap)
            val barH = maxH * h
            val by   = cy - barH / 2f
            drawRoundRect(
                color        = Color(0xFF00D4AA).copy(alpha = if (i == 3) 1f else 0.50f),
                topLeft      = Offset(bx, by),
                size         = Size(barW, barH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }

        // Teks % di tengah (pakai drawContext nativeCanvas)
        val paint = android.graphics.Paint().apply {
            color     = android.graphics.Color.WHITE
            textSize  = 28.dp.toPx()
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "85%",
            cx,
            cy + 60.dp.toPx(),
            paint
        )
        val smallPaint = android.graphics.Paint().apply {
            color    = android.graphics.Color.argb(160, 255, 255, 255)
            textSize = 11.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "Target Minggu Ini",
            cx,
            cy + 76.dp.toPx(),
            smallPaint
        )
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 3 — Ilustrasi Streak / Flame (Canvas)
// ─────────────────────────────────────────────────────────────
@Composable
private fun IllustrasiStreak(
    floatAnim : Float,
    glowAnim  : Float,
    waveAnim  : Float
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp, top = 20.dp, start = 24.dp, end = 24.dp)
    ) {
        val cx   = size.width / 2f
        val cy   = size.height / 2f + floatAnim.dp.toPx() * 0.3f

        // Glow oranye
        drawCircle(
            color  = Color(0xFFFF6F00).copy(alpha = 0.15f * glowAnim),
            radius = size.height * 0.4f,
            center = Offset(cx, cy)
        )

        // 7 hari streak boxes
        val dayW   = 36.dp.toPx()
        val dayGap = 8.dp.toPx()
        val days   = listOf(true, true, true, true, true, true, false)
        val totalW = days.size * (dayW + dayGap) - dayGap
        val startX = cx - totalW / 2f
        val boxY   = cy - 30.dp.toPx()

        days.forEachIndexed { i, done ->
            val bx = startX + i * (dayW + dayGap)
            drawRoundRect(
                color        = if (done) Color(0xFFFF8F00).copy(alpha = 0.85f)
                else Color.White.copy(alpha = 0.12f),
                topLeft      = Offset(bx, boxY),
                size         = Size(dayW, dayW),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            if (done) {
                // Checkmark / flame kecil
                drawCircle(
                    color  = Color(0xFFFFCC02),
                    radius = 4.dp.toPx(),
                    center = Offset(bx + dayW / 2f, boxY + dayW / 2f)
                )
            }
        }

        // Label hari
        val dayLabels = listOf("S","S","R","K","J","S","M")
        val labelPaint = android.graphics.Paint().apply {
            color     = android.graphics.Color.argb(180, 255, 255, 255)
            textSize  = 9.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        days.forEachIndexed { i, _ ->
            val bx = startX + i * (dayW + dayGap) + dayW / 2f
            drawContext.canvas.nativeCanvas.drawText(
                dayLabels[i], bx, boxY + dayW + 14.dp.toPx(), labelPaint
            )
        }

        // Teks angka streak besar
        val bigPaint = android.graphics.Paint().apply {
            color          = android.graphics.Color.WHITE
            textSize       = 52.dp.toPx()
            isFakeBoldText = true
            textAlign      = android.graphics.Paint.Align.CENTER
            isAntiAlias    = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "7", cx, cy - 60.dp.toPx(), bigPaint
        )
        val subPaint = android.graphics.Paint().apply {
            color     = android.graphics.Color.argb(160, 255, 255, 255)
            textSize  = 12.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "HARI BERTURUT-TURUT 🔥", cx, cy - 42.dp.toPx(), subPaint
        )

        // Wave line di bawah
        val waveY  = cy + 85.dp.toPx()
        val waveAmp = 8.dp.toPx()
        val path   = Path()
        path.moveTo(0f, waveY)
        val steps  = 60
        for (j in 0..steps) {
            val x = size.width * j / steps
            val y = waveY + waveAmp * kotlin.math.sin(
                (j.toFloat() / steps * 4 * Math.PI + waveAnim * 2 * Math.PI).toFloat()
            )
            if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path  = path,
            color = Color(0xFF00D4AA).copy(alpha = 0.35f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
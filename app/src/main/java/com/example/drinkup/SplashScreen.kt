package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Palette ────────────────────────────────────────────────────────────────────
private val BgTop      = Color(0xFF0A2050)   // navy atas (gelap)
private val BgMid      = Color(0xFF0C3070)   // biru tengah
private val BgBot      = Color(0xFF0A1A40)   // navy bawah
private val DropOuter  = Color(0xFF7ED8F6)   // biru muda terang drop
private val DropInner  = Color(0xFF0D2A5C)   // navy gelap isi drop
private val GlassRing  = Color(0x33FFFFFF)   // putih transparan untuk ring
private val WaveBar    = Color(0xFF5DD9F0)   // teal/light-blue bar
private val WaveBar2   = Color(0xFF29ABD4)
private val TrackColor = Color(0x33FFFFFF)   // track putih transparan
private val TextWhite  = Color(0xFFFFFFFF)
private val TextSub    = Color(0xFFB8D4F0)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // ── Phase ───────────────────────────────────────────────────────────────
    // 0 = awal, 1 = logo muncul, 2 = teks muncul, 3 = loading, 4 = fade out
    var phase by remember { mutableStateOf(0) }

    // ── Infinite transition ─────────────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "inf")

    // Float logo naik turun
    val floatY by inf.animateFloat(
        initialValue  = -7f,
        targetValue   = 7f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Rotasi ring luar (berputar lambat)
    val ringRot by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 360f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "ringRot"
    )

    // Pulse glow drop
    val pulseScale by inf.animateFloat(
        initialValue  = 0.96f,
        targetValue   = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(1600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // ── Wave loading: posisi kepala gelombang ───────────────────────────────
    // Kita simulasikan gelombang dengan multiple sinusoidal offset
    val wavePhase by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            tween(900, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Shimmer bar
    val shimmer by inf.animateFloat(
        initialValue  = -1f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(
            tween(1600, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // Dots label
    val dotsAnim by inf.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label         = "dots"
    )
    val dotsText = when ((dotsAnim * 4).toInt() % 4) {
        0    -> "MEMPERSIAPKAN HIDRASI ANDA"
        1    -> "MEMPERSIAPKAN HIDRASI ANDA  ·"
        2    -> "MEMPERSIAPKAN HIDRASI ANDA  · ·"
        else -> "MEMPERSIAPKAN HIDRASI ANDA  · · ·"
    }

    // ── Glow blob latar dekorasi ─────────────────────────────────────────────
    val blobPulse by inf.animateFloat(
        initialValue  = 0.7f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "blobPulse"
    )

    // ── Phase-driven animations ──────────────────────────────────────────────

    // Logo
    val logoScale by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 0f,
        animationSpec = tween(600),
        label         = "logoAlpha"
    )

    // Teks
    val textAlpha by animateFloatAsState(
        targetValue   = if (phase >= 2) 1f else 0f,
        animationSpec = tween(700),
        label         = "textAlpha"
    )
    val textSlide by animateFloatAsState(
        targetValue   = if (phase >= 2) 0f else 30f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "textSlide"
    )

    // Loading bar progress
    val barProgress by animateFloatAsState(
        targetValue   = if (phase >= 3) 1f else 0f,
        animationSpec = tween(2400, easing = FastOutSlowInEasing),
        label         = "barProgress"
    )

    // Bottom section fade
    val bottomAlpha by animateFloatAsState(
        targetValue   = if (phase >= 3) 1f else 0f,
        animationSpec = tween(500),
        label         = "bottomAlpha"
    )

    // Screen fade-out
    val screenAlpha by animateFloatAsState(
        targetValue   = if (phase >= 4) 0f else 1f,
        animationSpec = tween(450, easing = FastOutLinearInEasing),
        label         = "screenAlpha"
    )

    // ── Timing ───────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(200);  phase = 1   // logo spring masuk
        delay(750);  phase = 2   // teks slide up
        delay(600);  phase = 3   // loading bar gelombang mulai
        delay(2600); phase = 4   // fade out
        delay(460);  onSplashFinished()
    }

    // ── Root ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = screenAlpha)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to BgTop,
                        0.45f to BgMid,
                        1.00f to BgBot
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Dekorasi glow blob (latar) ────────────────────────────────────
        // Blob kiri atas
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-180).dp)
                .size(280.dp)
                .scale(blobPulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A5FA0).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        // Blob kanan bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-80).dp)
                .size(220.dp)
                .scale(1.1f - (blobPulse - 0.7f))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0B4D8A).copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )
        // Blob tengah besar (radius lebar, sangat transparan)
        Box(
            modifier = Modifier
                .offset(y = 60.dp)
                .size(320.dp)
                .alpha(0.10f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3AB4E8), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // ── Konten utama ──────────────────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.3f))

            // ── Logo ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .offset(y = floatY.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ring luar paling besar: frosted glass (putih sangat transparan)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .graphicsLayer { rotationZ = ringRot }
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    GlassRing,
                                    Color(0x00FFFFFF),
                                    GlassRing.copy(alpha = 0.08f),
                                    Color(0x00FFFFFF),
                                    GlassRing
                                )
                            ),
                            CircleShape
                        )
                )

                // Ring glass statis
                Box(
                    modifier = Modifier
                        .size(204.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x22FFFFFF),
                                    Color(0x08FFFFFF)
                                )
                            ),
                            CircleShape
                        )
                )

                // Lingkaran biru muda (outer circle drop)
                Box(
                    modifier = Modifier
                        .size(164.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF7ECFF0),
                                    Color(0xFF4EB8DE)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Lingkaran navy tengah
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0D2A5C), Color(0xFF071530))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Water drop icon (SVG-like dengan Canvas bisa diganti asset)
                        Text(
                            text     = "💧",
                            fontSize = 46.sp
                        )
                    }
                }

                // Highlight putih kecil di kiri atas (efek glossy)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = (-42).dp, y = (-46).dp)
                        .alpha(0.55f)
                        .background(
                            Brush.radialGradient(
                                listOf(Color.White, Color.Transparent)
                            ),
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── Nama App ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textSlide.dp)
            ) {
                // Shadow/glow teks
                Text(
                    text      = "DrinkUp",
                    fontSize  = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color     = Color(0xFF3AB4E8).copy(alpha = 0.4f),
                    modifier  = Modifier.offset(x = 2.dp, y = 4.dp)
                )
                Text(
                    text          = "DrinkUp",
                    fontSize      = 52.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    fontStyle     = FontStyle.Italic,
                    color         = TextWhite,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textSlide.dp)
            ) {
                Text(
                    text          = "MULAI HARIMU DENGAN SEHAT",
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextSub,
                    letterSpacing = 3.sp,
                    textAlign     = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Loading bar section ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomAlpha)
                    .padding(horizontal = 48.dp)
                    .padding(bottom = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Wave loading bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(TrackColor)
                ) {
                    // Progress utama dengan efek wave di ujung
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barProgress)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(WaveBar2, WaveBar, Color(0xFF8AE9FF))
                                )
                            )
                    ) {
                        // Shimmer sweep di atas bar
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.55f),
                                            Color.Transparent
                                        ),
                                        start = Offset(shimmer * 400f - 60f, 0f),
                                        end   = Offset(shimmer * 400f + 60f, 10f)
                                    )
                                )
                        )
                    }

                    // "Kepala gelombang" — bulat bergelombang di ujung progress
                    if (barProgress > 0.01f && barProgress < 0.98f) {
                        val waveAmp = (Math.sin(wavePhase.toDouble()) * 2.5f).toFloat()
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .fillMaxWidth(barProgress)
                                .wrapContentWidth(Alignment.End)
                                .offset(y = waveAmp.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFBBF0FF))
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Label
                Text(
                    text          = dotsText,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = TextSub,
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center
                )
            }
        }
    }
}
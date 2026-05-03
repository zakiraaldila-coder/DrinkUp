package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDeep    = Color(0xFF010914)
private val BgMid     = Color(0xFF041530)
private val BgAccent  = Color(0xFF062550)
private val Cyan      = Color(0xFF00D4FF)
private val CyanSoft  = Color(0xFF4DD9FF)
private val Gold      = Color(0xFFFFD166)
private val TextWhite = Color(0xFFFFFFFF)
private val TextSub   = Color(0xFF8EC8E8)

// ── Partikel ─────────────────────────────────────────────────────────────────
private data class Particle(
    val x: Float, val y: Float, val size: Float,
    val speedOffset: Float, val alpha: Float, val isGold: Boolean
)

private val particles = listOf(
    Particle(0.07f, 0.10f, 3f, 0.0f, 0.55f, false),
    Particle(0.88f, 0.07f, 2f, 0.5f, 0.40f, true),
    Particle(0.93f, 0.28f, 4f, 1.0f, 0.50f, false),
    Particle(0.04f, 0.42f, 2f, 1.5f, 0.35f, true),
    Particle(0.80f, 0.52f, 3f, 2.0f, 0.55f, false),
    Particle(0.14f, 0.68f, 5f, 0.7f, 0.40f, false),
    Particle(0.91f, 0.74f, 2f, 1.2f, 0.45f, true),
    Particle(0.42f, 0.04f, 3f, 1.8f, 0.35f, false),
    Particle(0.62f, 0.94f, 2f, 0.3f, 0.40f, true),
    Particle(0.22f, 0.86f, 4f, 2.5f, 0.50f, false),
    Particle(0.72f, 0.16f, 2f, 0.9f, 0.55f, true),
    Particle(0.52f, 0.80f, 3f, 1.6f, 0.40f, false),
)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // ── Phase ─────────────────────────────────────────────────────────────────
    // 0=init, 1=maskot muncul, 2=wave, 3=teks, 4=loading, 5=fade out
    var phase by remember { mutableIntStateOf(0) }

    val inf = rememberInfiniteTransition(label = "inf")

    // Float naik turun
    val floatY by inf.animateFloat(
        initialValue  = -7f, targetValue = 7f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "floatY"
    )
    // Wave goyang
    val waveRot by inf.animateFloat(
        initialValue  = -15f, targetValue = 15f,
        animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "waveRot"
    )
    // Partikel
    val particlePhase by inf.animateFloat(
        initialValue  = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "particlePhase"
    )
    // Blob pulse
    val blobPulse by inf.animateFloat(
        initialValue  = 0.88f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "blobPulse"
    )
    // Ring rotate
    val ringRot by inf.animateFloat(
        initialValue  = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "ringRot"
    )
    // Glow pulse bawah maskot
    val glowPulse by inf.animateFloat(
        initialValue  = 0.6f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowPulse"
    )
    // Shimmer bar
    val shimmer by inf.animateFloat(
        initialValue  = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    // Dots teks
    val dotsAnim by inf.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "dots"
    )
    val dotsText = when ((dotsAnim * 4).toInt() % 4) {
        0    -> "MEMPERSIAPKAN HIDRASI ANDA"
        1    -> "MEMPERSIAPKAN HIDRASI ANDA  ·"
        2    -> "MEMPERSIAPKAN HIDRASI ANDA  · ·"
        else -> "MEMPERSIAPKAN HIDRASI ANDA  · · ·"
    }

    // ── State animations ──────────────────────────────────────────────────────
    val mascotScale by animateFloatAsState(
        targetValue   = if (phase >= 1) 1f else 2.2f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "mascotScale"
    )
    val mascotAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(300), label = "mascotAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(700), label = "textAlpha"
    )
    val textSlide by animateFloatAsState(
        targetValue   = if (phase >= 3) 0f else 30f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "textSlide"
    )
    val barProgress by animateFloatAsState(
        targetValue   = if (phase >= 4) 1f else 0f,
        animationSpec = tween(2200, easing = FastOutSlowInEasing), label = "barProgress"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (phase >= 4) 1f else 0f,
        animationSpec = tween(500), label = "bottomAlpha"
    )
    val screenAlpha by animateFloatAsState(
        targetValue   = if (phase >= 5) 0f else 1f,
        animationSpec = tween(450, easing = FastOutLinearInEasing), label = "screenAlpha"
    )

    val isWaving = phase >= 2

    LaunchedEffect(Unit) {
        delay(150);  phase = 1
        delay(900);  phase = 2
        delay(500);  phase = 3
        delay(400);  phase = 4
        delay(2400); phase = 5
        delay(460);  onSplashFinished()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = screenAlpha)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to BgDeep,
                        0.35f to BgMid,
                        0.65f to BgAccent,
                        1.00f to BgMid
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Radial overlay subtle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x18004080), Color.Transparent),
                        radius = 1400f
                    )
                )
        )

        // Ambient glow kiri atas
        Box(
            modifier = Modifier
                .size(380.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .blur(110.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x22003A70), Color.Transparent)),
                    CircleShape
                )
        )

        // Ambient glow kanan bawah
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = 110.dp, y = 240.dp)
                .scale(1.15f - (blobPulse - 0.88f))
                .blur(100.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x1800C8F0), Color.Transparent)),
                    CircleShape
                )
        )

        // Accent gold blob kecil
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 130.dp, y = (-270).dp)
                .blur(70.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x18FFD166), Color.Transparent)),
                    CircleShape
                )
        )

        // Partikel bintang
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val py = sin(particlePhase + p.speedOffset) * 10f
                val px = cos(particlePhase * 0.7f + p.speedOffset) * 5f
                drawCircle(
                    color  = (if (p.isGold) Gold else Cyan).copy(alpha = p.alpha * 0.75f),
                    radius = p.size * density / 2f,
                    center = Offset(p.x * size.width + px * density, p.y * size.height + py * density)
                )
            }
        }

        // ── Konten ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Spacer atas lebih kecil → konten sedikit ke atas dari center
            Spacer(Modifier.weight(0.65f))

            // ── Maskot ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        scaleX       = mascotScale
                        scaleY       = mascotScale
                        alpha        = mascotAlpha
                        translationY = floatY
                    },
                contentAlignment = Alignment.Center
            ) {
                // Aura luar halus
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .blur(55.dp)
                        .background(
                            Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to Cyan.copy(alpha = 0.20f),
                                    0.5f to Cyan.copy(alpha = 0.07f),
                                    1.0f to Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Lingkaran kaca frosted
                Box(
                    modifier = Modifier
                        .size(212.dp)
                        .background(
                            Brush.radialGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0x2C005080),
                                    0.55f to Color(0x16003060),
                                    1.0f to Color(0x0A001830)
                                )
                            ),
                            CircleShape
                        )
                )

                // Ring berputar luar
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .graphicsLayer { rotationZ = ringRot }
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    Cyan.copy(alpha = 0.16f),
                                    Color.Transparent,
                                    Gold.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Ring dalam counter-rotate
                Box(
                    modifier = Modifier
                        .size(198.dp)
                        .graphicsLayer { rotationZ = -ringRot * 0.5f }
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    Gold.copy(alpha = 0.09f),
                                    Color.Transparent,
                                    Cyan.copy(alpha = 0.07f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Ground glow bawah maskot
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .offset(y = 55.dp)
                        .blur(28.dp)
                        .graphicsLayer { alpha = glowPulse * 0.65f }
                        .background(
                            Brush.radialGradient(listOf(Cyan.copy(alpha = 0.45f), Color.Transparent)),
                            CircleShape
                        )
                )

                // ── Gambar maskot ─────────────────────────────────────────
                // res/drawable/mascot_drinkup.png — PNG transparan, tubuh lengkap
                Image(
                    painter            = painterResource(R.drawable.mascot_drinkup),
                    contentDescription = "DrinkUp Mascot",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .size(192.dp)   // ← cukup besar agar nyata dalam lingkaran
                        .graphicsLayer {
                            rotationZ       = if (isWaving) waveRot * 0.42f else 0f
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                )
            }

            // Gap maskot → teks: rapat & proporsional
            Spacer(Modifier.height(22.dp))

            // ── DrinkUp ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textSlide.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "DrinkUp",
                    fontSize   = 54.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle  = FontStyle.Italic,
                    color      = Cyan.copy(alpha = 0.32f),
                    modifier   = Modifier.offset(x = 0.dp, y = 5.dp).blur(10.dp)
                )
                Text(
                    text          = "DrinkUp",
                    fontSize      = 54.sp,
                    fontWeight    = FontWeight.Black,
                    fontStyle     = FontStyle.Italic,
                    color         = TextWhite,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Tagline ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset(y = textSlide.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp).height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.60f)))
                        )
                )
                Text(
                    text          = "MULAI HARIMU DENGAN SEHAT",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextSub,
                    letterSpacing = 2.5.sp,
                    textAlign     = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(32.dp).height(1.dp)
                        .background(
                            Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.60f), Color.Transparent))
                        )
                )
            }

            // Spacer bawah push loading ke bawah layar
            Spacer(Modifier.weight(1f))

            // ── Loading ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomAlpha)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Dots bouncing
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.padding(bottom = 14.dp)
                ) {
                    repeat(3) { idx ->
                        val dotBounce by inf.animateFloat(
                            initialValue  = 0f, targetValue = -5f,
                            animationSpec = infiniteRepeatable(
                                tween(380, delayMillis = idx * 120, easing = FastOutSlowInEasing),
                                RepeatMode.Reverse
                            ),
                            label = "dot$idx"
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .offset(y = dotBounce.dp)
                                .background(if (idx == 1) Gold else CyanSoft, CircleShape)
                        )
                    }
                }

                // Bar progress
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x18FFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(barProgress)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF003A70), Cyan, Color(0xFFB8F0FF))
                                )
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.50f),
                                            Color.Transparent
                                        ),
                                        start = Offset(shimmer * 400f - 60f, 0f),
                                        end   = Offset(shimmer * 400f + 60f, 8f)
                                    )
                                )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text          = dotsText,
                    fontSize      = 9.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = TextSub.copy(alpha = 0.75f),
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// CARA MENYIAPKAN ASSET MASKOT:
// ──────────────────────────────────────────────────────────────────────────────
// 1. Simpan gambar maskot lengkap (PNG transparan) di:
//       res/drawable/mascot_drinkup.png
//
// 2. Animasi wave = rotasi seluruh maskot (waveRot * 0.42f).
//    Sesuaikan nilai 0.42f untuk intensitas goyang.
//
// 3. Untuk animasi lebih canggih gunakan Lottie:
//    implementation("com.airbnb.android:lottie-compose:6.4.0")
// ──────────────────────────────────────────────────────────────────────────────
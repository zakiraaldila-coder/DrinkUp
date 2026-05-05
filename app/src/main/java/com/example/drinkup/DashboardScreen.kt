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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import com.example.drinkup.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ── Palet tema senada halaman Statistik (navy biru medium, BUKAN hitam) ───────
private val BgDeep          = Color(0xFF0B1A35)   // navy biru utama — sama seperti bg statistik
private val BgMid           = Color(0xFF0E2040)   // navy layer 2
private val BgCard          = Color(0xFF112545)   // card surface — sedikit lebih terang dari bg
private val BgCardAlt       = Color(0xFF142C52)   // card alt
private val TealPrimary     = Color(0xFF00BFA5)   // teal/cyan utama (aksen statistik)
private val TealLight       = Color(0xFF4DD0C4)   // teal muda untuk highlight
private val TealDark        = Color(0xFF00897B)   // teal gelap
private val CyanAccent      = Color(0xFF26C6DA)   // cyan terang seperti bar chart statistik
private val CyanSoft        = Color(0xFF80DEEA)   // cyan pudar
private val BorderSubtle    = Color(0xFF1B3560)   // border kartu subtle
private val BorderGlow      = Color(0xFF224070)   // border dengan glow
private val TextPrimary     = Color(0xFFFFFFFF)
private val TextSecondary   = Color(0xFF8FA8C8)   // secondary text seperti di statistik
private val TextMuted       = Color(0xFF4A6880)   // text muted
private val AccentOrange    = Color(0xFFFF6D00)
private val AccentOrangeAlt = Color(0xFFFFAB40)

@Composable
fun DashboardScreen(
    onShowTambah           : () -> Unit = {},
    onNavigateToWeeklyGoal : () -> Unit = {},
    onNavigateToStreak     : () -> Unit = {},
    authViewModel          : AuthViewModel   = viewModel(),
    intakeViewModel        : IntakeViewModel = viewModel()
) {
    val userData       by authViewModel.userData.collectAsState()
    val userName        = userData.namaLengkap.ifBlank { "Kamu" }
    val dynamicTarget   = if (userData.beratBadan > 0) userData.beratBadan * 35 else 2000

    val intakeState    by intakeViewModel.state.collectAsState()
    val currentIntake   = intakeState.todayTotal
    val streak          = intakeState.streak

    LaunchedEffect(Unit) { intakeViewModel.startListening() }

    val progress = (currentIntake.toFloat() / dynamicTarget).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "progress"
    )

    val lastDrinkText = intakeState.lastDrinkAt?.let { ts ->
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(ts))
    } ?: "--:--"

    val gelasHariIni = intakeState.todayEntries.size

    val hour     = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 11 -> "Selamat Pagi,"
        hour < 15 -> "Selamat Siang,"
        hour < 18 -> "Selamat Sore,"
        else      -> "Selamat Malam,"
    }

    val quotes = listOf(
        "\"Air adalah energi kehidupan. Berikan tubuhmu bahan bakar terbaik hari ini.\"",
        "\"Mulai hari dengan segelas air — investasi terbaik untuk tubuhmu.\"",
        "\"Tetap terhidrasi, tetap produktif, tetap sehat setiap hari.\""
    )
    val todayQuote = quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]

    val inf = rememberInfiniteTransition(label = "inf")
    val waveOffset by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )
    val waveOffset2 by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "wave2"
    )

    val sdf         = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDow    = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val mondayOff   = -todayDow
    val weeklyTotal = (0..6).sumOf { i ->
        val c = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, mondayOff + i) }
        intakeState.weeklyHistory[sdf.format(c.time)] ?: 0
    }
    val weeklyTarget   = intakeState.userTarget * 7
    val weeklyProgress = ((weeklyTotal.toFloat() / weeklyTarget) * 100).toInt().coerceAtMost(100)
    val remaining      = (dynamicTarget - currentIntake).coerceAtLeast(0)
    val percentInt     = (animatedProgress * 100).toInt()

    // ── Root Box: background full dark navy (senada statistik) ────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    )  {
        // Wave dekorasi background 3 layer — lebih subtle, warna teal gelap
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path1 = androidx.compose.ui.graphics.Path()
            val sy1 = size.height * 0.55f
            path1.moveTo(0f, sy1)
            for (x in 0..size.width.toInt() step 4) {
                val y = sy1 + size.height * 0.055f *
                        sin((x / size.width * 2 * Math.PI + waveOffset2 * 2 * Math.PI).toFloat())
                path1.lineTo(x.toFloat(), y)
            }
            path1.lineTo(size.width, size.height); path1.lineTo(0f, size.height); path1.close()
            drawPath(path1, TealDark.copy(alpha = 0.22f))

            val path2 = androidx.compose.ui.graphics.Path()
            val sy2 = size.height * 0.63f
            path2.moveTo(0f, sy2)
            for (x in 0..size.width.toInt() step 4) {
                val y = sy2 + size.height * 0.045f *
                        sin((x / size.width * 2 * Math.PI + (waveOffset2 + 0.33f) * 2 * Math.PI).toFloat())
                path2.lineTo(x.toFloat(), y)
            }
            path2.lineTo(size.width, size.height); path2.lineTo(0f, size.height); path2.close()
            drawPath(path2, BgMid.copy(alpha = 0.75f))

            val path3 = androidx.compose.ui.graphics.Path()
            val sy3 = size.height * 0.72f
            path3.moveTo(0f, sy3)
            for (x in 0..size.width.toInt() step 4) {
                val y = sy3 + size.height * 0.035f *
                        sin((x / size.width * 2 * Math.PI + (waveOffset2 + 0.66f) * 2 * Math.PI).toFloat())
                path3.lineTo(x.toFloat(), y)
            }
            path3.lineTo(size.width, size.height); path3.lineTo(0f, size.height); path3.close()
            drawPath(path3, BgCardAlt.copy(alpha = 0.80f))
        }

        // ── Fixed content (non-scrollable) ───────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {

                Spacer(Modifier.height(16.dp))

                // ── GREETING + STREAK ─────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            greeting,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color         = TextSecondary,
                                fontWeight    = FontWeight.Medium,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Text(
                            "$userName! 👋",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color      = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(AccentOrange, AccentOrangeAlt)))
                            .clickable { onNavigateToStreak() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "$streak Hari Streak!",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White, fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── WATER CIRCLE ──────────────────────────────────────────────
                Box(
                    modifier         = Modifier.fillMaxWidth().height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow halo — teal glow seperti aksen statistik
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(
                                Brush.radialGradient(listOf(TealPrimary.copy(alpha = 0.12f), Color.Transparent)),
                                CircleShape
                            )
                    )
                    // Ring luar — teal gradient
                    Box(
                        modifier = Modifier
                            .size(218.dp)
                            .drawBehind {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.06f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 13.dp.toPx())
                                )
                                drawArc(
                                    brush      = Brush.sweepGradient(listOf(TealPrimary, CyanAccent, TealPrimary)),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter  = false,
                                    style      = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = 13.dp.toPx(), cap = StrokeCap.Round
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Lingkaran dalam dark card + wave teal
                        Box(
                            modifier         = Modifier.size(192.dp).clip(CircleShape).background(BgCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val fillH  = size.height * animatedProgress
                                val wAmp   = 9.dp.toPx()
                                val wFreq  = 2f
                                clipRect(left = 0f, top = size.height - fillH, right = size.width, bottom = size.height) {
                                    val pb = androidx.compose.ui.graphics.Path()
                                    pb.moveTo(0f, size.height - fillH + wAmp)
                                    for (x in 0..size.width.toInt() step 4) {
                                        pb.lineTo(x.toFloat(), size.height - fillH +
                                                wAmp * sin((x / size.width * wFreq * Math.PI + (waveOffset + 0.5f) * 2 * Math.PI).toFloat()))
                                    }
                                    pb.lineTo(size.width, size.height); pb.lineTo(0f, size.height); pb.close()
                                    drawPath(pb, Brush.verticalGradient(
                                        listOf(TealLight.copy(alpha = 0.06f), TealLight.copy(alpha = 0.10f)),
                                        startY = size.height - fillH, endY = size.height
                                    ))
                                    val pf = androidx.compose.ui.graphics.Path()
                                    pf.moveTo(0f, size.height - fillH)
                                    for (x in 0..size.width.toInt() step 4) {
                                        pf.lineTo(x.toFloat(), size.height - fillH +
                                                wAmp * sin((x / size.width * wFreq * Math.PI + waveOffset * 2 * Math.PI).toFloat()))
                                    }
                                    pf.lineTo(size.width, size.height); pf.lineTo(0f, size.height); pf.close()
                                    drawPath(pf, Brush.verticalGradient(
                                        listOf(TealPrimary.copy(alpha = 0.15f), CyanAccent.copy(alpha = 0.22f)),
                                        startY = size.height - fillH, endY = size.height
                                    ))
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$currentIntake",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp
                                    )
                                )
                                Text(
                                    "ML / ${dynamicTarget}ML",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = TextSecondary, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(TealPrimary.copy(alpha = 0.18f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        "$percentInt%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TealLight, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    if (remaining > 0) "💪 Sisa $remaining ml untuk hari ini"
                    else "🎉 Kamu sudah capai target hari ini!",
                    style    = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontWeight = FontWeight.Medium),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(14.dp))

                // ── TOMBOL MINUM SEKARANG — teal gradient ─────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brush.horizontalGradient(listOf(TealPrimary, CyanAccent)))
                            .clickable { onShowTambah() }
                            .padding(horizontal = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color.White.copy(alpha = 0.20f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Minum Sekarang",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── SECTION LABEL ─────────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.width(4.dp).height(18.dp)
                            .background(TealPrimary, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Aktivitas Hari Ini",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── ACTIVITY CARDS ────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Card Terakhir Minum
                    Box(
                        modifier = Modifier
                            .weight(1f).clip(RoundedCornerShape(16.dp)).background(BgCard)
                            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier.size(30.dp)
                                    .background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Rounded.Schedule, null, tint = TealPrimary, modifier = Modifier.size(16.dp)) }
                            Spacer(Modifier.height(6.dp))
                            Text("TERAKHIR MINUM", style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary, letterSpacing = 0.6.sp, fontWeight = FontWeight.SemiBold, fontSize = 9.sp
                            ))
                            Spacer(Modifier.height(2.dp))
                            Text(lastDrinkText, style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary, fontWeight = FontWeight.ExtraBold
                            ))
                        }
                    }
                    // Card Gelas Hari Ini
                    Box(
                        modifier = Modifier
                            .weight(1f).clip(RoundedCornerShape(16.dp)).background(BgCard)
                            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier.size(30.dp)
                                    .background(CyanAccent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Rounded.LocalDrink, null, tint = CyanAccent, modifier = Modifier.size(16.dp)) }
                            Spacer(Modifier.height(6.dp))
                            Text("GELAS HARI INI", style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary, letterSpacing = 0.6.sp, fontWeight = FontWeight.SemiBold, fontSize = 9.sp
                            ))
                            Spacer(Modifier.height(2.dp))
                            Text("$gelasHariIni Gelas", style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary, fontWeight = FontWeight.ExtraBold
                            ))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── TUJUAN MINGGUAN ───────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(BgCard)
                        .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(22.dp))
                        .clickable { onNavigateToWeeklyGoal() }
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color  = TealPrimary.copy(alpha = 0.07f), radius = 70.dp.toPx(),
                            center = Offset(size.width - 20.dp.toPx(), size.height / 2)
                        )
                    }
                    Row(
                        modifier          = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp).background(TealPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("⭐", fontSize = 22.sp) }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tujuan Mingguan", style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary, fontWeight = FontWeight.ExtraBold
                            ))
                            Spacer(Modifier.height(5.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(0.85f).height(5.dp)
                                    .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.08f))
                            ) {
                                Box(modifier = Modifier
                                    .fillMaxWidth(weeklyProgress / 100f).fillMaxHeight()
                                    .background(Brush.horizontalGradient(listOf(TealPrimary, CyanAccent)), RoundedCornerShape(50))
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("$weeklyProgress% Tercapai", style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary
                            ))
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}
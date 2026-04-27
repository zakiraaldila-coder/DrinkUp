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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.drinkup.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

private val DashBg      = Color(0xFFF0F4F8)
private val DashNavy    = Color(0xFF0D2D5E)
private val DashBlue    = Color(0xFF1565C0)
private val DashCard    = Color(0xFFFFFFFF)
private val DashHint    = Color(0xFF90A4AE)
private val DashWater1  = Color(0xFF4FC3F7)
private val DashWater2  = Color(0xFF0288D1)
private val DashTeal    = Color(0xFF26C6DA)
private val DashWeekly  = Color(0xFFE3F2FD)

@Composable
fun DashboardScreen(
    currentIntake          : Int,
    targetIntake           : Int,
    onAddWater             : (Int) -> Unit,
    streak                 : Int,
    onShowTambah           : () -> Unit = {},
    onNavigateToWeeklyGoal : () -> Unit = {},   // ← BARU: navigasi ke halaman tujuan mingguan
    authViewModel          : AuthViewModel = viewModel()
) {
    // Ambil nama user dari Firestore secara realtime
    var userName by remember { mutableStateOf("Kamu") }

    LaunchedEffect(Unit) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("namaLengkap") ?: "Kamu"
                }
        }
    }

    val progress = (currentIntake.toFloat() / targetIntake).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label         = "progress"
    )

    val hour     = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 11 -> "Selamat Pagi,"
        hour < 15 -> "Selamat Siang,"
        hour < 18 -> "Selamat Sore,"
        else      -> "Selamat Malam,"
    }

    // Quotes
    val quotes = listOf(
        "\"Air adalah energi kehidupan. Berikan tubuhmu bahan bakar terbaik hari ini.\"",
        "\"Mulai hari dengan segelas air — investasi terbaik untuk tubuhmu.\"",
        "\"Tetap terhidrasi, tetap produktif, tetap sehat setiap hari.\""
    )
    val todayQuote = quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]

    // Infinite wave animation
    val inf = rememberInfiniteTransition(label = "inf")
    val waveOffset by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DashBg)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))

            // ── GREETING + STREAK ─────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        greeting,
                        style = MaterialTheme.typography.bodyLarge.copy(color = DashHint)
                    )
                    Text(
                        "$userName!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color      = DashNavy,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
                // Streak badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$streak Hari Streak!",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color      = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── QUOTE CARD ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = DashCard,
                border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💧", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        todayQuote,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = DashNavy,
                            fontStyle  = FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Rounded.Refresh, null,
                        tint     = DashHint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── WATER CIRCLE ──────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ring luar abu-abu
                Box(
                    modifier         = Modifier
                        .size(260.dp)
                        .drawBehind {
                            drawCircle(
                                color = Color(0xFFE0E8F0),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 18.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Lingkaran air dengan wave
                    Box(
                        modifier         = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        // Wave fill
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val fillHeight = size.height * animatedProgress
                            val waveAmp    = 12.dp.toPx()
                            val waveFreq   = 2f

                            clipRect(
                                left   = 0f,
                                top    = size.height - fillHeight,
                                right  = size.width,
                                bottom = size.height
                            ) {
                                // Gelombang bawah (lebih gelap)
                                val path2 = androidx.compose.ui.graphics.Path()
                                path2.moveTo(0f, size.height - fillHeight + waveAmp)
                                for (x in 0..size.width.toInt() step 4) {
                                    val y = size.height - fillHeight +
                                            waveAmp * sin(
                                        (x / size.width * waveFreq * Math.PI + (waveOffset + 0.5f) * 2 * Math.PI).toFloat()
                                    )
                                    path2.lineTo(x.toFloat(), y)
                                }
                                path2.lineTo(size.width, size.height)
                                path2.lineTo(0f, size.height)
                                path2.close()
                                drawPath(
                                    path2, Brush.verticalGradient(
                                        listOf(DashWater2.copy(alpha = 0.7f), DashWater2),
                                        startY = size.height - fillHeight,
                                        endY   = size.height
                                    )
                                )

                                // Gelombang atas (lebih terang)
                                val path1 = androidx.compose.ui.graphics.Path()
                                path1.moveTo(0f, size.height - fillHeight)
                                for (x in 0..size.width.toInt() step 4) {
                                    val y = size.height - fillHeight +
                                            waveAmp * sin(
                                        (x / size.width * waveFreq * Math.PI + waveOffset * 2 * Math.PI).toFloat()
                                    )
                                    path1.lineTo(x.toFloat(), y)
                                }
                                path1.lineTo(size.width, size.height)
                                path1.lineTo(0f, size.height)
                                path1.close()
                                drawPath(
                                    path1, Brush.verticalGradient(
                                        listOf(DashWater1, DashWater2),
                                        startY = size.height - fillHeight,
                                        endY   = size.height
                                    )
                                )
                            }
                        }

                        // Teks angka di tengah
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text  = "$currentIntake",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    color      = DashNavy,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 56.sp
                                )
                            )
                            Text(
                                text  = "ML / ${targetIntake}ML",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color         = DashHint,
                                    letterSpacing = 1.sp,
                                    fontWeight    = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── TOMBOL MINUM SEKARANG ─────────────────────────────────────────
            Button(
                onClick   = onShowTambah,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape     = RoundedCornerShape(50),
                colors    = ButtonDefaults.buttonColors(containerColor = DashNavy),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Minum Sekarang",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── AKTIVITAS HARI INI ────────────────────────────────────────────
            Text(
                "Aktivitas Hari Ini",
                style = MaterialTheme.typography.titleLarge.copy(
                    color      = DashNavy,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(14.dp))

            // 2 card statistik
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Terakhir minum
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(18.dp),
                    color    = DashCard,
                    border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Rounded.Schedule, null,
                            tint     = DashNavy,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "TERAKHIR MINUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = DashHint,
                                letterSpacing = 0.5.sp,
                                fontWeight    = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                        Text(
                            if (currentIntake > 0) now else "--:--",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color      = DashNavy,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }

                // Gelas hari ini
                Surface(
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(18.dp),
                    color    = DashCard,
                    border   = BorderStroke(1.dp, Color(0xFFE8EEF4))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Rounded.LocalDrink, null,
                            tint     = DashNavy,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "GELAS HARI INI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = DashHint,
                                letterSpacing = 0.5.sp,
                                fontWeight    = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        val gelas = (currentIntake / 250).coerceAtLeast(0)
                        Text(
                            "$gelas Gelas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color      = DashNavy,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── TUJUAN MINGGUAN ───────────────────────────────────────────────
            // Sekarang card ini bisa diklik untuk buka halaman WeeklyGoalScreen
            val weeklyProgress = (progress * 100).toInt().coerceAtMost(100)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToWeeklyGoal() },   // ← klik buka halaman detail
                shape    = RoundedCornerShape(18.dp),
                color    = DashWeekly
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Star, null,
                            tint     = DashNavy,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tujuan Mingguan",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color      = DashNavy,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "$weeklyProgress% Tercapai",
                            style = MaterialTheme.typography.bodySmall.copy(color = DashHint)
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = DashHint)
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
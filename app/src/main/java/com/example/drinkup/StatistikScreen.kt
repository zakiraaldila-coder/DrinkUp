package com.example.drinkup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatistikScreen(
    history: Map<String, Int>,
    targetIntake: Int
) {
    val colorScheme = MaterialTheme.colorScheme

    val todayMl   = history.values.lastOrNull() ?: 0
    val avgMl     = if (history.isNotEmpty()) history.values.average().toInt() else 0
    val recordMl  = history.values.maxOrNull() ?: 0
    val totalWeek = history.values.sum()

    val avgL    = avgMl    / 1000.0
    val recordL = recordMl / 1000.0
    val totalL  = totalWeek / 1000.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Page title ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "Statistik Mingguan",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lacak hidrasi Anda sepanjang minggu.",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Top two stat cards ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🎯",
                        label = "TARGET HARI INI",
                        value = "${String.format("%.1f", targetIntake / 1000.0)} L"
                    )
                    SmallStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📈",
                        label = "RATA-RATA",
                        value = "${String.format("%.1f", avgL)} L"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ── Rekor terbaik (dark primary card) ──────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REKOR TERBAIK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${String.format("%.1f", recordL)} L",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Aktivitas Hidrasi chart card ─────────────────────────────────
            item {
                HydrationChartCard(
                    history = history,
                    targetIntake = targetIntake,
                    totalL = totalL
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Health Tips card ─────────────────────────────────────────────
            item {
                HealthTipsCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SmallStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
fun HydrationChartCard(
    history: Map<String, Int>,
    targetIntake: Int,
    totalL: Double
) {
    val colorScheme = MaterialTheme.colorScheme
    val sorted = history.toSortedMap().toList()
    val max    = maxOf(history.values.maxOrNull() ?: 1, targetIntake).toFloat()

    val barReached = colorScheme.primary
    val barPartial = colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Aktivitas Hidrasi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "7 hari terakhir",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL MINGGU INI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${String.format("%.1f", totalL)} L",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada data 😢", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            } else {
                val targetFraction = targetIntake / max
                val dashColor = colorScheme.outline

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val yTarget = size.height * (1f - targetFraction)
                        drawLine(
                            color = dashColor,
                            start = Offset(0f, yTarget),
                            end = Offset(size.width, yTarget),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                            cap = StrokeCap.Round
                        )
                    }
                    val targetFractionForLabel = targetIntake / max
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Text(
                            text = "TARGET (${String.format("%.1f", targetIntake / 1000.0)}L)",
                            fontSize = 9.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    top = (160 * (1f - targetFractionForLabel) - 14).coerceAtLeast(0f).dp
                                )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sorted.forEachIndexed { index, (_, value) ->
                            val fraction = value / max
                            val animatedFraction by animateFloatAsState(
                                targetValue = fraction,
                                animationSpec = tween(durationMillis = 700 + index * 80),
                                label = "bar_$index"
                            )
                            val barColor = if (value >= targetIntake) barReached else barPartial

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val days = listOf("SEN", "SEL", "RAB", "KAM", "JUM", "SAB", "MIN")
                    val usedDays = if (sorted.size <= 7) days.takeLast(sorted.size) else days

                    sorted.forEachIndexed { index, _ ->
                        Text(
                            text = usedDays.getOrElse(index) { "..." },
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthTipsCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color(0xFF1A5F6B), Color(0xFF0D3D4A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 64.sp)
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "HEALTH TIPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondaryContainer,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Pentingnya Air Saat Pagi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Memulai hari dengan segelas air hangat dapat membantu mengaktifkan organ internal dan membuang racun sebelum Anda mengonsumsi makanan pertama.",
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Baca Selengkapnya →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                )
            }
        }
    }
}
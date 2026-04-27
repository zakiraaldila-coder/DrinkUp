package com.example.drinkup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Definisikan warna yang dibutuhkan
private val TealLight = Color(0xFFB2DFDB)
private val TealDark = Color(0xFF00695C)

@Composable
fun ChartScreen(history: Map<String, Int>) {

    val target = 2000
    val max = maxOf(history.values.maxOrNull() ?: 1, target).toFloat()
    val sorted = history.toSortedMap().toList()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📊", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Grafik Minum Air",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada data 😢",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                // BAR CHART
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sorted.forEachIndexed { index, (date, value) ->
                        val targetProgress = value / max

                        val animatedProgress by animateFloatAsState(
                            targetValue = targetProgress,
                            animationSpec = tween(durationMillis = 800 + index * 100),
                            label = "barAnim_$index"
                        )

                        // Warna bergradasi tiap bar
                        val barColor = when {
                            value >= target -> Brush.verticalGradient(
                                listOf(Color(0xFF00BFA5), Color(0xFF00897B))
                            )
                            value >= target * 0.7f -> Brush.verticalGradient(
                                listOf(Color(0xFF26C6DA), Color(0xFF00ACC1))
                            )
                            else -> Brush.verticalGradient(
                                listOf(Color(0xFF80DEEA), Color(0xFF4DD0E1))
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Nilai di atas bar
                            Text(
                                text = "${value}ml",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealDark,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedProgress)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }

                // Garis bawah
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(TealLight)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Label tanggal di bawah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sorted.forEach { (date, _) ->
                        Text(
                            text = date.takeLast(5),
                            fontSize = 9.sp,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legenda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(color = Color(0xFF00BFA5), label = "Target tercapai")
                    LegendItem(color = Color(0xFF26C6DA), label = "> 70%")
                    LegendItem(color = Color(0xFF80DEEA), label = "< 70%")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray)
    }
}
package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Palet ────────────────────────────────────────────────────────────────────
private val NavyDeep   = Color(0xFF0A1F5C)
private val NavyMid    = Color(0xFF0D3B8E)
private val NavyCard   = Color(0xFF112870)
private val CardBorder = Color(0xFF1E3FA0)
private val WaterCyan  = Color(0xFF4FC3F7)
private val WaterBlue  = Color(0xFF29B6F6)
private val AccentMint = Color(0xFF00E5CC)
private val TextWhite  = Color(0xFFFFFFFF)
private val TextSub    = Color(0xFFB0C4E8)

data class WaterOption(
    val ml    : Int,
    val label : String,
    val sub   : String
)

@Composable
fun TambahScreen(
    onTambah   : (Int) -> Unit,
    onBatalkan : () -> Unit
) {
    var selectedOption by rememberSaveable { mutableStateOf<Int?>(1) }
    var customInput    by rememberSaveable { mutableStateOf("") }

    val options = listOf(
        WaterOption(150,  "150ml", "Gelas Kecil"),
        WaterOption(250,  "250ml", "Standar"),
        WaterOption(330,  "330ml", "Botol Kecil"),
        WaterOption(500,  "500ml", "Botol Sedang")
    )

    // Pulse pada tombol simpan
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulseScale by inf.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.025f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDeep)
    ) {
        // Dekorasi blob background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(WaterCyan.copy(alpha = 0.07f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 220.dp, y = 350.dp)
                .background(
                    Brush.radialGradient(listOf(AccentMint.copy(alpha = 0.06f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Handle bar
            Box(
                modifier = Modifier
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TextSub.copy(alpha = 0.35f))
            )

            Spacer(Modifier.height(18.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.radialGradient(listOf(WaterCyan.copy(alpha = 0.2f), NavyMid)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) { Text("💧", fontSize = 28.sp) }

            Spacer(Modifier.height(10.dp))

            Text(
                "Tambah Asupan Air",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color      = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pilih ukuran atau atur sendiri",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSub),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // ── Preview ringkas progress hari ini ──────────────────────
            Box(
                modifier         = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 8.dp.toPx()
                    val sweep  = 360f * (selectedOption?.let { options[it].ml.toFloat() / 500f } ?: 0f).coerceIn(0f, 1f)
                    drawArc(
                        color      = androidx.compose.ui.graphics.Color(0xFF1E3FA0),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style      = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    drawArc(
                        brush      = androidx.compose.ui.graphics.Brush.sweepGradient(listOf(
                            androidx.compose.ui.graphics.Color(0xFF4FC3F7),
                            androidx.compose.ui.graphics.Color(0xFF29B6F6)
                        )),
                        startAngle = -90f, sweepAngle = sweep, useCenter = false,
                        style      = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val previewMl = if (customInput.isNotEmpty()) customInput.toIntOrNull() ?: 0
                    else selectedOption?.let { options[it].ml } ?: 0
                    Text(
                        "${previewMl}ml",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite, fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        "dipilih",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSub, fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Section label ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(3.dp).height(14.dp).background(WaterCyan, RoundedCornerShape(50)))
                Spacer(Modifier.width(8.dp))
                Text(
                    "PILIHAN CEPAT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WaterCyan, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp, fontSize = 10.sp
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Grid 2x2 ──────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                options.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { opt ->
                            val idx        = options.indexOf(opt)
                            val isSelected = selectedOption == idx

                            val cardScale by animateFloatAsState(
                                targetValue   = if (isSelected) 1.04f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label         = "card$idx"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(cardScale)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.linearGradient(listOf(WaterCyan, WaterBlue))
                                        else
                                            Brush.linearGradient(listOf(NavyCard, NavyMid))
                                    )
                                    .then(
                                        if (!isSelected) Modifier.border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                                        else Modifier
                                    )
                                    .clickable { selectedOption = idx; customInput = "" }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        opt.label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = TextWhite, fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        opt.sub,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) TextWhite.copy(alpha = 0.7f) else TextSub,
                                            letterSpacing = 0.3.sp, fontSize = 10.sp
                                        )
                                    )
                                }
                                // Checkmark pojok kanan atas
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(7.dp)
                                            .size(16.dp)
                                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Input kustom ──────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(3.dp).height(14.dp).background(WaterCyan, RoundedCornerShape(50)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "JUMLAH KUSTOM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WaterCyan, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp, fontSize = 10.sp
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(
                            width = 1.dp,
                            brush = if (customInput.isNotEmpty())
                                Brush.horizontalGradient(listOf(AccentMint, WaterCyan))
                            else
                                Brush.horizontalGradient(listOf(CardBorder, CardBorder)),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier          = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = customInput,
                            onValueChange = {
                                customInput    = it.filter { c -> c.isDigit() }.take(4)
                                if (it.isNotEmpty()) selectedOption = null
                            },
                            placeholder = {
                                Text(
                                    "Contoh: 450",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSub.copy(alpha = 0.45f))
                                )
                            },
                            singleLine      = true,
                            modifier        = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = Color.Transparent,
                                unfocusedBorderColor    = Color.Transparent,
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor             = AccentMint
                            ),
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite, fontWeight = FontWeight.Bold
                            )
                        )
                        // Badge ML
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(WaterCyan.copy(alpha = 0.15f), AccentMint.copy(alpha = 0.15f)))
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "ML",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = WaterCyan, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Tombol Simpan ─────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(pulseScale)
                        .height(50.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .clickable {
                            val jumlah = when {
                                customInput.isNotEmpty() -> customInput.toIntOrNull() ?: 0
                                selectedOption != null   -> options[selectedOption!!].ml
                                else                     -> 0
                            }
                            if (jumlah > 0) { onTambah(jumlah); onBatalkan() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(NavyDeep, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Simpan",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = NavyDeep, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = onBatalkan) {
                    Text(
                        "Batalkan",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSub.copy(alpha = 0.55f), fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
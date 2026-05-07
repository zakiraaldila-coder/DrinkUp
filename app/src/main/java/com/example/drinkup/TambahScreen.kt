package com.example.drinkup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Palet ─────────────────────────────────────────────────────────────────────
private val NavyDeep   = Color(0xFF080E1C)
private val NavyMid    = Color(0xFF0E1E35)
private val NavyCard   = Color(0xFF0D1A30)
private val CardBorder = Color(0xFF1A3050)
private val WaterCyan  = Color(0xFF00C8D4)
private val WaterBlue  = Color(0xFF2196F3)
private val AccentMint = Color(0xFF4DD0C4)
private val TextWhite  = Color(0xFFFFFFFF)
private val TextSub    = Color(0xFF7A9AB8)

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

    // Hitung ml aktif
    val activeMl = when {
        customInput.isNotEmpty() -> customInput.toIntOrNull() ?: 0
        selectedOption != null   -> options[selectedOption!!].ml
        else                     -> 0
    }

    // Animasi arc (max 500ml)
    val animFraction by animateFloatAsState(
        targetValue   = (activeMl.toFloat() / 500f).coerceIn(0f, 1f),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label         = "arcFraction"
    )

    // Pulse tombol simpan
    val inf = rememberInfiniteTransition(label = "inf")
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
        // Background blob dekorasi
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(listOf(WaterCyan.copy(alpha = 0.06f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 230.dp, y = 320.dp)
                .background(
                    Brush.radialGradient(listOf(WaterBlue.copy(alpha = 0.05f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(24.dp))

            // ── Lingkaran Animasi Besar ────────────────────────────────
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val inset = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(inset, inset)

                    // Track (background arc)
                    drawArc(
                        color      = CardBorder,
                        startAngle = -220f,
                        sweepAngle = 260f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    // Arc aktif
                    if (animFraction > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(WaterBlue, WaterCyan, AccentMint, WaterCyan, WaterBlue)
                            ),
                            startAngle = -220f,
                            sweepAngle = 260f * animFraction,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Lingkaran dalam
                Box(
                    modifier = Modifier
                        .size(158.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF0E2A4A), Color(0xFF081828))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedContent(
                            targetState = activeMl,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                            },
                            label = "mlValue"
                        ) { ml ->
                            Text(
                                text = if (ml > 0) "$ml" else "–",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            "ML",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WaterCyan,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Keep your hydration flowing",
                fontSize = 12.sp,
                color = TextSub
            )

            Spacer(Modifier.height(22.dp))

            // ── PILIHAN CEPAT ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .background(WaterCyan, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PILIHAN CEPAT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaterCyan,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextWhite
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            opt.sub,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.3.sp,
                                            color = if (isSelected) TextWhite.copy(alpha = 0.7f) else TextSub
                                        )
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(7.dp)
                                                .size(18.dp)
                                                .background(Color.White.copy(alpha = 0.35f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector        = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint               = Color.White,
                                                modifier           = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── JUMLAH KUSTOM ─────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .background(WaterCyan, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "JUMLAH KUSTOM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WaterCyan,
                        letterSpacing = 1.2.sp
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
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSub.copy(alpha = 0.45f)
                                    )
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
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(WaterCyan.copy(alpha = 0.15f), AccentMint.copy(alpha = 0.15f))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "ML",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = WaterCyan
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
                        .background(Brush.horizontalGradient(listOf(WaterCyan, WaterBlue)))
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
                                .background(Color.White.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Add, null,
                                tint     = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Simpan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = onBatalkan) {
                    Text(
                        "BATALKAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = TextSub.copy(alpha = 0.55f)
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
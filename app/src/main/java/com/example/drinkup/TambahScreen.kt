package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TNavy   = Color(0xFF0D2D5E)
private val TTeal   = Color(0xFF1A7A8A)
private val TBg     = Color(0xFFF5F9FC)
private val TCard   = Color(0xFFFFFFFF)
private val THint   = Color(0xFF90A4AE)
private val TBlue   = Color(0xFF4FC3F7)
private val TField  = Color(0xFFF0F4F8)
private val TBorder = Color(0xFFE0EEF5)

data class WaterOption(
    val ml    : Int,
    val label : String,
    val sub   : String,
    val emoji : String
)

@Composable
fun TambahScreen(
    onTambah   : (Int) -> Unit,
    onBatalkan : () -> Unit
) {
    var selectedOption by rememberSaveable { mutableStateOf<Int?>(1) } // default 250ml
    var customInput    by rememberSaveable { mutableStateOf("") }

    val options = listOf(
        WaterOption(150,  "150ml", "GELAS KECIL",   "🥛"),
        WaterOption(250,  "250ml", "STANDAR",        "🥤"),
        WaterOption(330,  "330ml", "BOTOL KECIL",    "💧"),
        WaterOption(500,  "500ml", "BOTOL SEDANG",   "🍶")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Handle bar ────────────────────────────────────────────
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(TBorder)
        )

        Spacer(Modifier.height(24.dp))

        // ── Judul ─────────────────────────────────────────────────
        Text(
            "Tambah Asupan Air",
            style = MaterialTheme.typography.headlineMedium.copy(
                color      = TNavy,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Berikan tubuhmu hidrasi yang cukup hari ini.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color      = THint,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(32.dp))

        // ── PILIHAN CEPAT label ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "PILIHAN CEPAT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = TTeal,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Grid 2x2 ─────────────────────────────────────────────
        Column(
            modifier              = Modifier.padding(horizontal = 24.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp)
        ) {
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEachIndexed { i, opt ->
                        val globalIdx = options.indexOf(opt)
                        val isSelected = selectedOption == globalIdx
                        Box(
                            modifier         = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) TBlue else TCard
                                )
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color.Transparent else TBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedOption = globalIdx
                                    customInput    = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(opt.emoji, fontSize = 28.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    opt.label,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color      = if (isSelected) TNavy else TNavy,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    opt.sub,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color         = if (isSelected) TNavy.copy(alpha = 0.7f) else THint,
                                        letterSpacing = 0.5.sp,
                                        fontWeight    = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── ATUR JUMLAH KUSTOM ────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "ATUR JUMLAH KUSTOM",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = TTeal,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value         = customInput,
                onValueChange = {
                    customInput    = it.filter { c -> c.isDigit() }.take(4)
                    if (it.isNotEmpty()) selectedOption = null
                },
                placeholder   = {
                    Text(
                        "Contoh: 450",
                        style = MaterialTheme.typography.bodyLarge.copy(color = THint)
                    )
                },
                suffix        = {
                    Text(
                        "ML",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color      = TTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = TNavy.copy(alpha = 0.5f),
                    unfocusedBorderColor    = TBorder,
                    focusedContainerColor   = TField,
                    unfocusedContainerColor = TField,
                    cursorColor             = TNavy
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color      = TNavy,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(Modifier.weight(1f))

        // ── TOMBOL SIMPAN ─────────────────────────────────────────
        Column(
            modifier            = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick  = {
                    val jumlah = when {
                        customInput.isNotEmpty() -> customInput.toIntOrNull() ?: 0
                        selectedOption != null   -> options[selectedOption!!].ml
                        else                     -> 0
                    }
                    if (jumlah > 0) {
                        onTambah(jumlah)
                        onBatalkan()
                    }
                },
                modifier  = Modifier.fillMaxWidth().height(58.dp),
                shape     = RoundedCornerShape(50),
                colors    = ButtonDefaults.buttonColors(containerColor = TNavy),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(
                    "＋  Simpan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onBatalkan) {
                Text(
                    "BATALKAN",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color         = TNavy,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
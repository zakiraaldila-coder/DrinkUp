package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

// Warna aksen air tetap hardcoded (warna visual tombol pilihan)
private val TBlue = Color(0xFF4FC3F7)

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
    val colorScheme = MaterialTheme.colorScheme

    var selectedOption by rememberSaveable { mutableStateOf<Int?>(1) }
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
            .verticalScroll(rememberScrollState())
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Handle bar ────────────────────────────────────────────
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(colorScheme.outlineVariant)
        )

        Spacer(Modifier.height(24.dp))

        // ── Judul ─────────────────────────────────────────────────
        Text(
            "Tambah Asupan Air",
            style = MaterialTheme.typography.headlineMedium.copy(
                color      = colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Berikan tubuhmu hidrasi yang cukup hari ini.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color      = colorScheme.onSurfaceVariant,
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
                    color         = colorScheme.secondary,
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
                    row.forEachIndexed { _, opt ->
                        val globalIdx = options.indexOf(opt)
                        val isSelected = selectedOption == globalIdx
                        Box(
                            modifier         = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) TBlue else colorScheme.surface)
                                .border(
                                    width = if (isSelected) 0.dp else 1.dp,
                                    color = if (isSelected) Color.Transparent else colorScheme.outlineVariant,
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
                                        color      = colorScheme.onSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    opt.sub,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color         = if (isSelected) colorScheme.onSurface.copy(alpha = 0.7f) else colorScheme.onSurfaceVariant,
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
                    color         = colorScheme.secondary,
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
                        style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurfaceVariant)
                    )
                },
                suffix        = {
                    Text(
                        "ML",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color      = colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor    = colorScheme.outlineVariant,
                    focusedContainerColor   = colorScheme.surfaceVariant,
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                    cursorColor             = colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color      = colorScheme.onSurface,
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
                colors    = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(4.dp)
            ) {
                Text(
                    "＋  Simpan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onBatalkan) {
                Text(
                    "BATALKAN",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color         = colorScheme.onSurfaceVariant,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
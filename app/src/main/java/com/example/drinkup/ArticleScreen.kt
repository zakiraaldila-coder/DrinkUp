package com.example.drinkup

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ArticleScreen(
    onBack    : () -> Unit = {},
    onDrink   : () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Hero Image + Back Button ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Hero gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A5F6B), Color(0xFF0D3D4A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 80.sp)
            }

            // Overlay gelap di bawah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF0D3D4A).copy(alpha = 0.8f))
                        )
                    )
            )

            // Back button
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { onBack() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }

            // Tag EDUKASI KESEHATAN
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colorScheme.secondary.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    "EDUKASI KESEHATAN",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            Spacer(Modifier.height(20.dp))

            // ── Judul ─────────────────────────────────────────────────────────
            Text(
                text       = "Pentingnya Air Saat Pagi",
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = colorScheme.onBackground,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(16.dp))

            // ── Paragraf pembuka (drop cap style) ────────────────────────────
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text       = "B",
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.primary,
                    lineHeight = 52.sp,
                    modifier   = Modifier.padding(end = 4.dp, top = 2.dp)
                )
                Text(
                    text       = "ayangkan tubuh Anda sebagai mesin yang telah beristirahat selama 8 jam. Minum segelas air putih segera setelah bangun tidur adalah cara terbaik untuk \"menyalakan\" sistem tubuh Anda kembali dengan lembut dan efektif.",
                    fontSize   = 15.sp,
                    color      = colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = colorScheme.outlineVariant)
            Spacer(Modifier.height(24.dp))

            // ── Section 1 ─────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("↗", fontSize = 14.sp, color = colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = "Mengaktifkan Organ Internal",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text       = "Setelah tidur panjang, tubuh mengalami dehidrasi ringan. Air pertama yang Anda minum bertindak sebagai pelumas untuk organ-organ vital, membantu ginjal dan hati untuk mulai bekerja optimal dalam menyaring darah sejak menit pertama hari Anda dimulai.",
                fontSize   = 14.sp,
                color      = colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(24.dp))

            // ── Keuntungan Utama Card ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Keuntungan Utama:",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    val benefits = listOf(
                        Triple("🫀", "Detoksifikasi Alami", "Membuang sisa racun dari proses regenerasi sel semalam."),
                        Triple("⚡", "Boost Metabolisme", "Meningkatkan laju pembakaran kalori hingga 24% selama 90 menit."),
                        Triple("🧠", "Kesehatan Otak", "73% otak adalah air; hidrasi pagi mencegah kabut otak (brain fog)."),
                        Triple("✨", "Kulit Bercahaya", "Membantu sirkulasi darah ke kulit agar terlihat lebih segar dan kenyal.")
                    )

                    benefits.forEach { (emoji, title, desc) ->
                        Row(
                            modifier          = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colorScheme.onPrimaryContainer.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text       = title,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text       = desc,
                                    fontSize   = 12.sp,
                                    color      = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Tips Praktis ──────────────────────────────────────────────────
            Text(
                text       = "💧 Tips Praktis Pagi Hari",
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = colorScheme.onBackground
            )
            Spacer(Modifier.height(14.dp))

            val tips = listOf(
                "Gunakan air suhu ruang (bukan air es) agar tidak mengagetkan sistem pencernaan.",
                "Minumlah minimal 2 gelas (sekitar 500ml) sebelum sarapan atau minum kopi.",
                "Tambahkan irisan lemon untuk ekstra vitamin C dan aroma menyegarkan."
            )

            tips.forEachIndexed { idx, tip ->
                Row(
                    modifier          = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "${idx + 1}",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = tip,
                        fontSize   = 14.sp,
                        color      = colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── CTA Card ──────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                color    = colorScheme.primary
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "Siap Mulai Hari?",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = colorScheme.onPrimary,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text      = "Jangan lupa catat hidrasi pertama Anda hari ini di DrinkUp.",
                        fontSize  = 13.sp,
                        color     = colorScheme.onPrimary.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onDrink() },
                        shape   = RoundedCornerShape(50),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.onPrimary,
                            contentColor   = colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💧  Catat Minum Sekarang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
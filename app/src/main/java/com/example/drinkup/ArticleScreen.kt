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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

// ─── Design tokens (same as StatistikScreen) ──────────────────────────────────
private val ABgDeep      = Color(0xFF0B1629)
private val ABgCard      = Color(0xFF112240)
private val ABgCardAlt   = Color(0xFF0D1B36)
private val AAccentCyan  = Color(0xFF00E5FF)
private val AAccentBlue  = Color(0xFF2979FF)
private val AAccentTeal  = Color(0xFF00BFA5)
private val ATextPrimary = Color(0xFFE8F0FE)
private val ATextSec     = Color(0xFF7B93B8)

// ─── Canvas Icons ─────────────────────────────────────────────────────────────

@Composable
private fun ArticleDropIcon(modifier: Modifier = Modifier, color: Color = AAccentCyan) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val path = Path().apply {
            moveTo(cx, 0f)
            cubicTo(cx + size.width * .5f, size.height * .4f,
                cx + size.width * .5f, size.height * .75f, cx, size.height)
            cubicTo(cx - size.width * .5f, size.height * .75f,
                cx - size.width * .5f, size.height * .4f, cx, 0f)
            close()
        }
        drawPath(path, color)
        val shine = Path().apply {
            moveTo(cx - size.width * .09f, size.height * .26f)
            cubicTo(cx - size.width * .19f, size.height * .43f,
                cx - size.width * .16f, size.height * .57f,
                cx - size.width * .07f, size.height * .57f)
            cubicTo(cx - size.width * .02f, size.height * .57f,
                cx + size.width * .02f, size.height * .49f,
                cx - size.width * .09f, size.height * .26f)
            close()
        }
        drawPath(shine, Color.White.copy(.4f))
    }
}

@Composable
private fun TrendUpIcon(modifier: Modifier = Modifier, color: Color = AAccentBlue) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .12f, h * .72f); lineTo(w * .40f, h * .38f)
            lineTo(w * .62f, h * .56f); lineTo(w * .88f, h * .22f)
        }
        drawPath(path, color, style = Stroke(w * .10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val arrowPath = Path().apply {
            moveTo(w * .75f, h * .18f); lineTo(w * .88f, h * .22f); lineTo(w * .82f, h * .35f)
        }
        drawPath(arrowPath, color, style = Stroke(w * .09f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun BoltIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFFFCA28)) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .58f, 0f); lineTo(w * .22f, h * .52f); lineTo(w * .50f, h * .48f)
            lineTo(w * .42f, h); lineTo(w * .78f, h * .46f); lineTo(w * .50f, h * .50f); close()
        }
        drawPath(path, color)
    }
}

@Composable
private fun BrainIcon(modifier: Modifier = Modifier, color: Color = Color(0xFFAB47BC)) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawOval(color, Offset(w * .08f, h * .18f), Size(w * .42f, h * .60f))
        drawOval(color, Offset(w * .50f, h * .18f), Size(w * .42f, h * .60f))
        drawRoundRect(color, Offset(w * .30f, h * .70f), Size(w * .40f, h * .20f), CornerRadius(w * .05f))
        drawLine(Color.White.copy(.35f), Offset(w * .50f, h * .18f), Offset(w * .50f, h * .78f), w * .06f, StrokeCap.Round)
    }
}

@Composable
private fun SparkleIcon(modifier: Modifier = Modifier, color: Color = AAccentCyan) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height; val cx = w / 2f; val cy = h / 2f
        val arms = listOf(
            Pair(Offset(cx, cy - h * .46f), Offset(cx, cy + h * .46f)),
            Pair(Offset(cx - w * .46f, cy), Offset(cx + w * .46f, cy)),
            Pair(Offset(cx - w * .32f, cy - h * .32f), Offset(cx + w * .32f, cy + h * .32f)),
            Pair(Offset(cx + w * .32f, cy - h * .32f), Offset(cx - w * .32f, cy + h * .32f))
        )
        arms.forEachIndexed { i, (s, e) ->
            drawLine(color, s, e, if (i < 2) w * .09f else w * .06f, StrokeCap.Round)
        }
        drawCircle(Color.White, w * .08f, Offset(cx, cy))
    }
}

@Composable
private fun DetoxIcon(modifier: Modifier = Modifier, color: Color = Color(0xFF66BB6A)) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .50f, h * .90f)
            cubicTo(w * .10f, h * .70f, w * .05f, h * .30f, w * .50f, h * .05f)
            cubicTo(w * .95f, h * .30f, w * .90f, h * .70f, w * .50f, h * .90f)
            close()
        }
        drawPath(path, color)
        drawLine(Color.White.copy(.4f), Offset(w * .50f, h * .88f), Offset(w * .50f, h * .12f), w * .06f, StrokeCap.Round)
        drawLine(Color.White.copy(.25f), Offset(w * .50f, h * .55f), Offset(w * .25f, h * .35f), w * .04f, StrokeCap.Round)
        drawLine(Color.White.copy(.25f), Offset(w * .50f, h * .55f), Offset(w * .75f, h * .35f), w * .04f, StrokeCap.Round)
    }
}

// ─── ArticleScreen ────────────────────────────────────────────────────────────

@Composable
fun ArticleScreen(
    onBack  : () -> Unit = {},
    onDrink : () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ABgDeep)
            .verticalScroll(rememberScrollState())
    ) {

        // ── Hero Section ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0B1D3A), Color(0xFF0D2D50), Color(0xFF0A3355)),
                            Offset.Zero,
                            Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )
            // Dekorasi lingkaran
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(AAccentCyan.copy(.05f), size.width * .60f,
                    Offset(size.width * .85f, -size.height * .1f))
                drawCircle(AAccentBlue.copy(.08f), size.width * .40f,
                    Offset(-size.width * .05f, size.height * .85f))
                drawCircle(Color.White.copy(.03f), size.width * .25f,
                    Offset(size.width * .55f, size.height * .75f))
            }
            // Drop icon besar
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-16).dp)
            ) {
                ArticleDropIcon(modifier = Modifier.size(96.dp))
            }
            // Overlay fade bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, ABgDeep)
                        )
                    )
            )
            // Top bar: back button + title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(.08f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack, null,
                        tint = ATextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "ARTIKEL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ATextPrimary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Badge kategori
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AAccentBlue, AAccentTeal)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "EDUKASI KESEHATAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 22.dp)) {

            // Judul
            Text(
                "Pentingnya Air Saat\nPagi",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ATextPrimary,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Paragraf pembuka dengan drop cap
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 2.dp)
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AAccentBlue, AAccentTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("B", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Text(
                    "ayangkan tubuh Anda sebagai mesin yang telah beristirahat selama 8 jam. Minum segelas air putih segera setelah bangun tidur adalah cara terbaik untuk \"menyalakan\" sistem tubuh Anda kembali dengan lembut dan efektif.",
                    fontSize = 15.sp,
                    color = ATextSec,
                    lineHeight = 25.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Section: Mengaktifkan Organ Internal ──────────────────────────
            ArtSectionHeader(
                icon = { TrendUpIcon(Modifier.size(16.dp)) },
                iconBg = AAccentBlue.copy(.12f),
                title = "Mengaktifkan Organ Internal"
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Setelah tidur panjang, tubuh mengalami dehidrasi ringan. Air pertama yang Anda minum bertindak sebagai pelumas untuk organ-organ vital, membantu ginjal dan hati untuk mulai bekerja optimal dalam menyaring darah sejak menit pertama hari Anda dimulai.",
                fontSize = 14.sp,
                color = ATextSec,
                lineHeight = 23.sp
            )

            Spacer(Modifier.height(26.dp))

            // ── Keuntungan Utama Card ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(ABgCard)
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(.03f), size.width * .38f,
                        Offset(size.width * .92f, size.height * .12f))
                }
                Column(Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SparkleIcon(Modifier.size(20.dp))
                        Text(
                            "Keuntungan Utama",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ATextPrimary
                        )
                    }
                    Spacer(Modifier.height(18.dp))

                    data class Benefit(val iconId: Int, val title: String, val desc: String)
                    val benefits = listOf(
                        Benefit(0, "Detoksifikasi Alami",
                            "Membuang sisa racun dari proses regenerasi sel semalam."),
                        Benefit(1, "Boost Metabolisme",
                            "Meningkatkan laju pembakaran kalori hingga 24% selama 90 menit."),
                        Benefit(2, "Kesehatan Otak",
                            "73% otak adalah air; hidrasi pagi mencegah kabut otak (brain fog)."),
                        Benefit(3, "Kulit Bercahaya",
                            "Membantu sirkulasi darah ke kulit agar terlihat lebih segar dan kenyal.")
                    )

                    benefits.forEachIndexed { idx, benefit ->
                        if (idx > 0) {
                            Spacer(Modifier.height(4.dp))
                            HorizontalDivider(color = Color.White.copy(.07f))
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(
                            Modifier.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                when (benefit.iconId) {
                                    0 -> DetoxIcon(Modifier.size(20.dp))
                                    1 -> BoltIcon(Modifier.size(20.dp))
                                    2 -> BrainIcon(Modifier.size(20.dp))
                                    else -> SparkleIcon(Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(
                                    benefit.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ATextPrimary
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    benefit.desc,
                                    fontSize = 12.sp,
                                    color = ATextSec,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Tips Praktis Pagi Hari ────────────────────────────────────────
            ArtSectionHeader(
                icon = { ArticleDropIcon(Modifier.size(16.dp), AAccentBlue) },
                iconBg = AAccentBlue.copy(.12f),
                title = "Tips Praktis Pagi Hari"
            )
            Spacer(Modifier.height(16.dp))

            val tips = listOf(
                "Gunakan air suhu ruang (bukan air es) agar tidak mengagetkan sistem pencernaan.",
                "Minumlah minimal 2 gelas (sekitar 500ml) sebelum sarapan atau minum kopi.",
                "Tambahkan irisan lemon untuk ekstra vitamin C dan aroma menyegarkan."
            )

            tips.forEachIndexed { idx, tip ->
                Row(
                    Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AAccentBlue, AAccentTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${idx + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        tip,
                        fontSize = 14.sp,
                        color = ATextSec,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // ── CTA Card ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AAccentBlue, Color(0xFF0064B4), AAccentTeal),
                            Offset.Zero,
                            Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(.06f), size.width * .42f,
                        Offset(size.width * .88f, size.height * .25f))
                    drawCircle(Color.White.copy(.04f), size.width * .25f,
                        Offset(size.width * .05f, size.height * .85f))
                }
                Column(
                    Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drop icon
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ArticleDropIcon(Modifier.size(28.dp), Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Siap Mulai Hari?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Jangan lupa catat hidrasi pertama Anda hari ini di DrinkUp.",
                        fontSize = 13.sp,
                        color = Color.White.copy(.75f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = { onDrink() },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = AAccentBlue
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ArticleDropIcon(Modifier.size(18.dp), AAccentBlue)
                            Text(
                                "Catat Minum Sekarang",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─── Section header helper ────────────────────────────────────────────────────

@Composable
private fun ArtSectionHeader(
    icon  : @Composable () -> Unit,
    iconBg: Color,
    title : String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ATextPrimary
        )
    }
}
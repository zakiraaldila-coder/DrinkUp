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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

// ─── Poppins FontFamily ───────────────────────────────────────────────────────
private val Poppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ─── Design tokens ────────────────────────────────────────────────────────────
private val ABgDeep      = Color(0xFF0A1628)
private val ABgCard      = Color(0xFF112240)
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
private fun TrendUpIcon(modifier: Modifier = Modifier, color: Color = AAccentCyan) {
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
                .height(300.dp)
        ) {
            // Background gradient biru gelap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0D1F3C), Color(0xFF0B1629))
                        )
                    )
            )
            // Dekorasi lingkaran halus
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(AAccentCyan.copy(.06f), size.width * .55f,
                    Offset(size.width * .80f, -size.height * .05f))
                drawCircle(AAccentBlue.copy(.07f), size.width * .45f,
                    Offset(-size.width * .10f, size.height * .80f))
            }

            // Drop icon di tengah hero
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 8.dp)
            ) {
                ArticleDropIcon(modifier = Modifier.size(100.dp))
            }

            // Badge EDUKASI KESEHATAN di bawah icon
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 74.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(listOf(AAccentBlue, AAccentTeal))
                    )
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Text(
                    "EDUKASI KESEHATAN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.2.sp,
                    fontFamily = Poppins
                )
            }

            // Fade ke bawah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, ABgDeep)))
            )

            // Top bar: back button + "ARTIKEL"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(.10f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ArrowBack, null,
                        tint = ATextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Text(
                    "ARTIKEL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ATextPrimary,
                    letterSpacing = 1.8.sp,
                    fontFamily = Poppins,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        Column(Modifier.padding(horizontal = 20.dp)) {

            Spacer(Modifier.height(8.dp))

            // Judul artikel
            Text(
                "Pentingnya Air Saat Pagi",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ATextPrimary,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                fontFamily = Poppins,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(22.dp))

            // Paragraf pembuka dengan drop cap "B"
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .padding(end = 9.dp, top = 3.dp)
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(AAccentBlue, AAccentTeal))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "B",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = Poppins
                    )
                }
                Text(
                    "ayangkan tubuh Anda sebagai mesin yang telah beristirahat selama 8 jam. " +
                            "Minum segelas air putih segera setelah bangun tidur adalah cara terbaik untuk " +
                            "\"menyalakan\" sistem tubuh Anda kembali dengan lembut dan efektif.",
                    fontSize = 14.sp,
                    color = ATextSec,
                    lineHeight = 23.sp,
                    fontFamily = Poppins
                )
            }

            Spacer(Modifier.height(26.dp))

            // ── Mengaktifkan Organ Internal ───────────────────────────────────
            ArtSectionHeader(
                icon  = { TrendUpIcon(Modifier.size(17.dp)) },
                iconBg = AAccentBlue.copy(.13f),
                title  = "Mengaktifkan Organ Internal"
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Setelah tidur panjang, tubuh mengalami dehidrasi ringan. Air pertama yang Anda " +
                        "minum bertindak sebagai pelumas untuk organ-organ vital, membantu ginjal dan hati " +
                        "untuk mulai bekerja optimal dalam menyaring darah sejak menit pertama hari Anda dimulai.",
                fontSize = 14.sp,
                color = ATextSec,
                lineHeight = 22.sp,
                fontFamily = Poppins
            )

            Spacer(Modifier.height(24.dp))

            // ── Keuntungan Utama Card ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ABgCard)
                    .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(20.dp))
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(.025f), size.width * .40f,
                        Offset(size.width * .94f, size.height * .10f))
                }
                Column(Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SparkleIcon(Modifier.size(20.dp))
                        Text(
                            "Keuntungan Utama",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ATextPrimary,
                            fontFamily = Poppins
                        )
                    }
                    Spacer(Modifier.height(14.dp))

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
                            HorizontalDivider(color = Color.White.copy(.07f), thickness = 0.8.dp)
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(
                            Modifier.padding(vertical = 7.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(.07f)),
                                contentAlignment = Alignment.Center
                            ) {
                                when (benefit.iconId) {
                                    0    -> DetoxIcon(Modifier.size(19.dp))
                                    1    -> BoltIcon(Modifier.size(19.dp))
                                    2    -> BrainIcon(Modifier.size(19.dp))
                                    else -> SparkleIcon(Modifier.size(19.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    benefit.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ATextPrimary,
                                    fontFamily = Poppins
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    benefit.desc,
                                    fontSize = 12.sp,
                                    color = ATextSec,
                                    lineHeight = 17.sp,
                                    fontFamily = Poppins
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            // ── Tips Praktis Pagi Hari ────────────────────────────────────────
            ArtSectionHeader(
                icon  = { ArticleDropIcon(Modifier.size(16.dp), AAccentCyan) },
                iconBg = AAccentCyan.copy(.10f),
                title  = "Tips Praktis Pagi Hari"
            )
            Spacer(Modifier.height(14.dp))

            val tips = listOf(
                "Gunakan air suhu ruang (bukan air es) agar tidak mengagetkan sistem pencernaan.",
                "Minumlah minimal 2 gelas (sekitar 500ml) sebelum sarapan atau minum kopi.",
                "Tambahkan irisan lemon untuk ekstra vitamin C dan aroma menyegarkan."
            )

            tips.forEachIndexed { idx, tip ->
                Row(
                    Modifier.padding(vertical = 7.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AAccentBlue, AAccentTeal))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${idx + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = Poppins
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        tip,
                        fontSize = 14.sp,
                        color = ATextSec,
                        lineHeight = 22.sp,
                        fontFamily = Poppins,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── CTA Card ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(AAccentBlue, Color(0xFF005FA3), AAccentTeal),
                            Offset.Zero,
                            Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(Color.White.copy(.07f), size.width * .45f,
                        Offset(size.width * .90f, size.height * .20f))
                    drawCircle(Color.White.copy(.04f), size.width * .28f,
                        Offset(size.width * .04f, size.height * .90f))
                }
                Column(
                    Modifier.padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ArticleDropIcon(Modifier.size(26.dp), Color.White)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Siap Mulai Hari?",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontFamily = Poppins
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Jangan lupa catat hidrasi pertama Anda hari ini di DrinkUp.",
                        fontSize = 13.sp,
                        color = Color.White.copy(.78f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        fontFamily = Poppins
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick  = { onDrink() },
                        shape    = RoundedCornerShape(50),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = AAccentBlue
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ArticleDropIcon(Modifier.size(17.dp), AAccentBlue)
                            Text(
                                "Catat Minum Sekarang",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 15.sp,
                                fontFamily = Poppins
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(44.dp))
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
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(11.dp))
        Text(
            title,
            fontSize   = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = ATextPrimary,
            fontFamily = Poppins
        )
    }
}
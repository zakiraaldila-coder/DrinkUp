package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenderAvatar(
    gender   : String,
    size     : Dp    = 60.dp,
    modifier : Modifier = Modifier
) {
    val (bgColor, fgColor, emoji) = when (gender.uppercase()) {
        "L"  -> Triple(Color(0xFF1A2F6B), Color(0xFF7ECFF0), "♂")
        "P"  -> Triple(Color(0xFF6B1A3F), Color(0xFFF0A0C0), "♀")
        else -> Triple(Color(0xFF0D1B4B), Color(0xFFB0C4D8), "?")
    }

    val badgeSp = (size.value * 0.22f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.fillMaxSize()
        ) {
            // Kepala
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .clip(CircleShape)
                    .background(fgColor)
            )
            Spacer(Modifier.height(size * 0.04f))
            // Badan — setengah ellipse (FIXED)
            Box(
                modifier = Modifier
                    .width(size * 0.55f)
                    .height(size * 0.30f)
                    .clip(
                        RoundedCornerShape(
                            topStartPercent    = 50,
                            topEndPercent      = 50,
                            bottomStartPercent = 0,
                            bottomEndPercent   = 0
                        )
                    )
                    .background(fgColor)
            )
        }
        // Badge gender
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(size * 0.04f)
                .size(size * 0.30f)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = emoji,
                fontSize   = badgeSp,
                fontWeight = FontWeight.Bold,
                color      = bgColor
            )
        }
    }
}
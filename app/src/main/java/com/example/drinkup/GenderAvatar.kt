package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GenderAvatar(
    gender   : String,
    nama     : String   = "",
    size     : Dp       = 60.dp,
    modifier : Modifier = Modifier
) {

    // Accent utama DrinkUp
    val tealPrimary = Color(0xFF00D4AA)

    // Gradient NAVY persis style sidebar
    val bgGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF12385A), // pusat navy cerah
            Color(0xFF0D2745), // transisi biru navy
            Color(0xFF091A33), // navy medium
            Color(0xFF060D1F)  // tepi gelap
        ),
        radius = size.value * 2.2f
    )

    // Inisial nama
    val initials = nama
        .trim()
        .split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }

    val textSp = (size.value * 0.38f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgGradient)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00D4AA),
                        Color(0xFF00BFFF)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {

        // Glow tengah supaya mirip sidebar
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1D5C8C).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Text(
            text       = initials,
            fontSize   = textSp,
            fontWeight = FontWeight.Bold,
            color      = tealPrimary
        )
    }
}
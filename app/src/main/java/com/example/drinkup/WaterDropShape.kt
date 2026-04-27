package com.example.drinkup

import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class WaterDropShape : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            cubicTo(w * 0.85f, h * 0.35f, w, h * 0.6f, w / 2f, h)
            cubicTo(0f, h * 0.6f, w * 0.15f, h * 0.35f, w / 2f, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}
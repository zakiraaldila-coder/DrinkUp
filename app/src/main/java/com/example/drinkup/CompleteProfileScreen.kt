package com.example.drinkup

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CP_NavyDark   = Color(0xFF0D1B4B)
private val CP_NavyMid    = Color(0xFF1A2F6B)
private val CP_TealAccent = Color(0xFF00BFA5)
private val CP_BgGray     = Color(0xFFF2F4F8)
private val CP_CardWhite  = Color(0xFFFFFFFF)
private val CP_MaleBlue   = Color(0xFF1565C0)
private val CP_FemaleRose = Color(0xFFAD1457)
private val CP_TextSec    = Color(0xFF8A94A6)

@Composable
fun CompleteProfileScreen(
    authViewModel   : AuthViewModel,
    onProfileComplete: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CP_NavyDark, CP_NavyMid, Color(0xFF0A1A40)))
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // ── Logo ────────────────────────────────────────────────────────
            Text(
                text       = "💧 DrinkUp",
                fontSize   = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle  = FontStyle.Italic,
                color      = Color.White
            )

            Spacer(Modifier.height(40.dp))

            // ── Title card ───────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = CP_CardWhite),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = "Satu langkah lagi! 🎉",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = CP_NavyDark,
                        textAlign  = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = "Pilih jenis kelamin untuk melengkapi profilmu. Avatar kamu akan disesuaikan!",
                        fontSize  = 13.sp,
                        color     = CP_TextSec,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Gender cards ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GenderPickCard(
                            modifier    = Modifier.weight(1f),
                            gender      = "L",
                            label       = "Laki-laki",
                            emoji       = "♂",
                            activeColor = CP_MaleBlue,
                            isSelected  = selectedGender == "L",
                            onClick     = { selectedGender = "L" }
                        )
                        GenderPickCard(
                            modifier    = Modifier.weight(1f),
                            gender      = "P",
                            label       = "Perempuan",
                            emoji       = "♀",
                            activeColor = CP_FemaleRose,
                            isSelected  = selectedGender == "P",
                            onClick     = { selectedGender = "P" }
                        )
                    }

                    // Error
                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text      = errorMsg,
                            color     = Color(0xFFD32F2F),
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Tombol Lanjut ────────────────────────────────────────
                    Button(
                        onClick = {
                            if (selectedGender.isEmpty()) {
                                errorMsg = "Pilih jenis kelamin dulu ya!"
                                return@Button
                            }
                            isLoading = true
                            authViewModel.saveGender(
                                gender    = selectedGender,
                                onSuccess = {
                                    isLoading = false
                                    onProfileComplete()
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMsg  = err
                                }
                            )
                        },
                        enabled  = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape  = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CP_NavyDark,
                            contentColor   = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Mulai Hidrasi! 💧",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Gender pick card ───────────────────────────────────────────────────────────
@Composable
fun GenderPickCard(
    modifier    : Modifier,
    gender      : String,
    label       : String,
    emoji       : String,
    activeColor : Color,
    isSelected  : Boolean,
    onClick     : () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue   = if (isSelected) 1.04f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label         = "scale"
    )

    Card(
        modifier  = modifier
            .scale(scale)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) activeColor else Color(0xFFDDE2EC),
                shape = RoundedCornerShape(20.dp)
            ),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isSelected) activeColor.copy(alpha = 0.08f) else Color(0xFFF8F9FC)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar preview
            GenderAvatar(gender = gender, size = 72.dp)

            Spacer(Modifier.height(12.dp))

            Text(
                text       = "$emoji $label",
                fontSize   = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = if (isSelected) activeColor else Color(0xFF8A94A6),
                textAlign  = TextAlign.Center
            )

            if (isSelected) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(activeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
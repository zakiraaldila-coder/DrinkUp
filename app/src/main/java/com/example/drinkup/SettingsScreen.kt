package com.example.drinkup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val S_NavyDark      = Color(0xFF0D1B4B)
private val S_NavyMid       = Color(0xFF1A2F6B)
private val S_TealAccent    = Color(0xFF00BFA5)
private val S_TealLight     = Color(0xFFB2EBF2)
private val S_BgGray        = Color(0xFFF2F4F8)
private val S_CardWhite     = Color(0xFFFFFFFF)
private val S_TextPrimary   = Color(0xFF0D1B4B)
private val S_TextSecondary = Color(0xFF8A94A6)

@Composable
fun SettingsScreen(
    onLogout               : () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    var darkMode   by remember { mutableStateOf(false) }
    var notifikasi by remember { mutableStateOf(true) }
    var nama       by remember { mutableStateOf("Pengguna") }
    var email      by remember { mutableStateOf("") }
    var beratBadan by remember { mutableStateOf(0) }
    var gender     by remember { mutableStateOf("") }

    val targetAir = beratBadan * 35

    // Fetch dari Firestore — realtime
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    nama       = doc.getString("namaLengkap") ?: "Pengguna"
                    email      = doc.getString("email") ?: ""
                    beratBadan = doc.getLong("beratBadan")?.toInt() ?: 0
                    gender     = doc.getString("gender") ?: ""
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(S_BgGray)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Page Title ───────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text       = "Settings",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = S_TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "Customize your sanctuary experience",
                        fontSize = 14.sp,
                        color    = S_TextSecondary
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Profile Section ──────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                        Text(
                            text       = "Edit",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = S_TealAccent,
                            modifier   = Modifier.clickable { onNavigateToEditProfile() }
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier  = Modifier.fillMaxWidth().clickable { onNavigateToEditProfile() },
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = S_CardWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GenderAvatar(gender = gender, size = 60.dp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nama, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                                Text(email, fontSize = 12.sp, color = S_TextSecondary)
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(S_TealLight)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text       = "PRO MEMBER",
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color      = S_NavyMid
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Health Data ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Health Data", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                    Spacer(Modifier.height(12.dp))

                    // Weight card
                    Card(
                        modifier  = Modifier.fillMaxWidth().clickable { onNavigateToEditProfile() },
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = S_CardWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("🧳", fontSize = 20.sp)
                                Text(
                                    text          = "WEIGHT",
                                    fontSize      = 11.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    color         = S_TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text       = if (beratBadan > 0) "$beratBadan" else "—",
                                    fontSize   = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = S_TextPrimary
                                )
                                Text(
                                    text     = " kg",
                                    fontSize = 16.sp,
                                    color    = S_TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            if (beratBadan > 0) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress   = (beratBadan / 150f).coerceIn(0f, 1f),
                                    modifier   = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color      = S_TealAccent,
                                    trackColor = Color(0xFFE0E4ED)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text       = "Edit di profil →",
                                    fontSize   = 11.sp,
                                    color      = S_TealAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text       = "Atur berat badan di Edit Profil →",
                                    fontSize   = 11.sp,
                                    color      = S_TealAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Daily Target card
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = S_NavyDark),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("💧", fontSize = 20.sp)
                                Text(
                                    text          = "DAILY TARGET",
                                    fontSize      = 11.sp,
                                    fontWeight    = FontWeight.SemiBold,
                                    color         = Color(0xFF8AA4D6),
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text       = if (targetAir > 0) String.format("%.1f", targetAir / 1000.0) else "—",
                                    fontSize   = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = Color.White
                                )
                                if (targetAir > 0) {
                                    Text(
                                        text     = "L",
                                        fontSize = 20.sp,
                                        color    = Color(0xFF8AA4D6),
                                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text     = if (beratBadan > 0) "Berdasarkan berat badan $beratBadan kg" else "Atur berat badan di Edit Profil",
                                fontSize = 12.sp,
                                color    = Color(0xFF8AA4D6)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Preferences ──────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = S_CardWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                            // Dark Mode
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(S_NavyMid.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("🌙", fontSize = 20.sp) }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Dark Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = S_TextPrimary)
                                    Text("Switch to the abyss theme", fontSize = 12.sp, color = S_TextSecondary)
                                }
                                Switch(
                                    checked         = darkMode,
                                    onCheckedChange = { darkMode = it },
                                    colors          = SwitchDefaults.colors(
                                        checkedThumbColor   = Color.White,
                                        checkedTrackColor   = S_TealAccent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCDD3DC)
                                    )
                                )
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F2F5), thickness = 1.dp)

                            // Smart Reminders
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(S_TealAccent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("🔔", fontSize = 20.sp) }
                                Spacer(Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Smart Reminders", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = S_TextPrimary)
                                    Text("Adaptive hydration alerts", fontSize = 12.sp, color = S_TextSecondary)
                                }
                                Switch(
                                    checked         = notifikasi,
                                    onCheckedChange = { notifikasi = it },
                                    colors          = SwitchDefaults.colors(
                                        checkedThumbColor   = Color.White,
                                        checkedTrackColor   = S_TealAccent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color(0xFFCDD3DC)
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Account ──────────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = S_TextPrimary)
                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = S_CardWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                            // Logout
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .clickable { onLogout() }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("↪", fontSize = 20.sp, color = S_TextPrimary)
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text       = "Logout",
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = S_TextPrimary,
                                    modifier   = Modifier.weight(1f)
                                )
                                Icon(Icons.Filled.ChevronRight, null, tint = S_TextSecondary, modifier = Modifier.size(20.dp))
                            }

                            Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F2F5), thickness = 1.dp)

                            // Delete Account
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .clickable { }
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFEBEE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("✕", fontSize = 13.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text       = "Delete Account",
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color      = Color(0xFFD32F2F),
                                    modifier   = Modifier.weight(1f)
                                )
                                Text(text = "Permanent Action", fontSize = 11.sp, color = S_TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
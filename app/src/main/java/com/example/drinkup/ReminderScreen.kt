package com.example.drinkup

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat

// ── Color Palette ──────────────────────────────────────────────────────────────
private val NavyDark      = Color(0xFF0D1B4B)
private val NavyMid       = Color(0xFF1A2F6B)
private val TealAccent    = Color(0xFF00BFA5)
private val TealLight     = Color(0xFFB2EBF2)
private val BgGray        = Color(0xFFF2F4F8)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF0D1B4B)
private val TextSecondary = Color(0xFF8A94A6)
private val DayChipActive = Color(0xFF26C6DA)

// ── Models ─────────────────────────────────────────────────────────────────────
data class ReminderItem(
    val id: Int,
    val label: String,
    val hour: Int,
    val minute: Int,
    val days: List<String>,     // e.g. ["MON","WED","FRI"] or ["EVERYDAY"]
    val intervalMillis: Long,
    val isActive: Boolean,
    val icon: String
)

enum class TimeUnit { MENIT, JAM }

// ── Main Screen ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen() {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("drinkup_prefs", Context.MODE_PRIVATE)

    var reminders by remember {
        mutableStateOf(
            listOf(
                ReminderItem(0, "Minum Air Pagi",  8,  0, listOf("MON","WED","FRI"), 8*3600_000L, true,  "💧"),
                ReminderItem(1, "Minum Air Siang", 10, 30, listOf("EVERYDAY"),        2*3600_000L, true,  "💧"),
                ReminderItem(2, "Minum Air Sore",  14,  0, listOf("WEEKENDS"),        4*3600_000L, false, "💧"),
                ReminderItem(3, "Minum Air Malam", 17, 15, listOf("MON","TUE","THU"), 6*3600_000L, true,  "💧"),
            )
        )
    }

    var showAddSheet    by remember { mutableStateOf(false) }
    var showPermWarning by remember { mutableStateOf(false) }
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) showPermWarning = true
        }
    } else null

    fun toggleReminder(item: ReminderItem, on: Boolean) {
        if (on && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            else showPermWarning = true
            return
        }
        reminders = reminders.map {
            if (it.id == item.id) {
                if (on) AlarmHelper.setRepeatingAlarm(context, item.intervalMillis)
                else    AlarmHelper.cancelAlarm(context)
                it.copy(isActive = on)
            } else it
        }
    }

    fun addReminder(hour: Int, minute: Int, label: String, days: List<String>, intervalMillis: Long) {
        val newId = (reminders.maxOfOrNull { it.id } ?: -1) + 1
        AlarmHelper.setRepeatingAlarm(context, intervalMillis)
        reminders = reminders + ReminderItem(
            id             = newId,
            label          = label,
            hour           = hour,
            minute         = minute,
            days           = days,
            intervalMillis = intervalMillis,
            isActive       = true,
            icon           = "💧"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Page Title ───────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text       = "Reminders",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text     = "Keep your hydration flowing throughout the day.",
                        fontSize = 14.sp,
                        color    = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── + Tambah Reminder Button ─────────────────────────────────────
            item {
                Button(
                    onClick  = { showAddSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(54.dp),
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyDark,
                        contentColor   = Color.White
                    )
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Add,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text       = "+ Tambah Reminder",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Permission Warning ───────────────────────────────────────────
            if (showPermWarning) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Izin Notifikasi Diperlukan",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize   = 13.sp,
                                    color      = Color(0xFFE65100)
                                )
                                Text("Aktifkan di pengaturan HP kamu", fontSize = 11.sp, color = Color(0xFFBF360C))
                            }
                            TextButton(onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                )
                            }) { Text("Buka", color = Color(0xFFE65100), fontSize = 12.sp) }
                        }
                    }
                }
            }

            // ── Reminder List ────────────────────────────────────────────────
            if (reminders.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔕", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada reminder",
                                fontWeight = FontWeight.SemiBold,
                                color      = TextSecondary,
                                fontSize   = 15.sp
                            )
                            Text(
                                "Klik + Tambah Reminder untuk membuat baru",
                                color    = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(reminders, key = { it.id }) { item ->
                    ReminderCard(
                        item     = item,
                        onToggle = { on -> toggleReminder(item, on) }
                    )
                }
            }

            // ── Smart Reminders Info Card ────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD6EEF8))
                ) {
                    Box {
                        // Decorative large drop icon
                        Text(
                            text     = "💧",
                            fontSize = 72.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .offset(y = 8.dp),
                            color    = Color(0x33000000)
                        )
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text       = "Smart Reminders",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text     = "We'll space out your alerts based on your daily goal and waking hours for optimal cellular hydration.",
                                fontSize = 13.sp,
                                color    = TextSecondary,
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth(0.72f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // ── Add Reminder Bottom Sheet ────────────────────────────────────────
        if (showAddSheet) {
            AddReminderSheet(
                sheetState = sheetState,
                onDismiss  = { showAddSheet = false },
                onSave     = { hour, minute, label, days, millis ->
                    addReminder(hour, minute, label, days, millis)
                    showAddSheet = false
                }
            )
        }
    }
}

// ── Reminder Card ──────────────────────────────────────────────────────────────
@Composable
fun ReminderCard(item: ReminderItem, onToggle: (Boolean) -> Unit) {
    val timeStr = String.format("%02d:%02d", item.hour, item.minute)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) CardWhite else Color(0xFFF5F6FA)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text       = timeStr,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (item.isActive) TextPrimary else TextSecondary
                )
                Switch(
                    checked         = item.isActive,
                    onCheckedChange = onToggle,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor       = Color.White,
                        checkedTrackColor       = TealAccent,
                        uncheckedThumbColor     = Color.White,
                        uncheckedTrackColor     = Color(0xFFCDD3DC)
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Day chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (item.isActive) DayChipActive.copy(alpha = 0.18f)
                                else Color(0xFFE8EAF0)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = day,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (item.isActive) NavyMid else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ── Add Reminder Bottom Sheet ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, label: String, days: List<String>, millis: Long) -> Unit
) {
    var selectedHour   by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(30) }
    var isAm           by remember { mutableStateOf(true) }
    var reminderLabel  by remember { mutableStateOf("Minum Pagi") }
    var vibrationOn    by remember { mutableStateOf(true) }
    var selectedDays   by remember { mutableStateOf(setOf("MON","TUE","WED","THU","FRI")) }
    var selectedSound  by remember { mutableStateOf("flowing") }

    val allDays = listOf("S","M","T","W","T","F","S")
    val dayKeys = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = CardWhite,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle       = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint               = TextPrimary
                    )
                }
                Text(
                    text       = "Tambah Reminder",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = TextPrimary)
                }
            }

            // ── Clock Picker ─────────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular arc background
                Box(
                    modifier         = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F4FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Hour picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { selectedHour = (selectedHour % 12) + 1 }) {
                                Text("▲", fontSize = 16.sp, color = TextSecondary)
                            }
                            Text(
                                text       = String.format("%02d", selectedHour),
                                fontSize   = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = NavyDark
                            )
                            Text("JAM", fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.sp)
                            IconButton(onClick = {
                                selectedHour = if (selectedHour == 1) 12 else selectedHour - 1
                            }) {
                                Text("▼", fontSize = 16.sp, color = TextSecondary)
                            }
                        }

                        Text(
                            ":",
                            fontSize   = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color      = NavyDark,
                            modifier   = Modifier.padding(horizontal = 4.dp)
                        )

                        // Minute picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { selectedMinute = (selectedMinute + 1) % 60 }) {
                                Text("▲", fontSize = 16.sp, color = TextSecondary)
                            }
                            Text(
                                text       = String.format("%02d", selectedMinute),
                                fontSize   = 52.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = NavyDark
                            )
                            Text("MENIT", fontSize = 11.sp, color = TextSecondary, letterSpacing = 1.sp)
                            IconButton(onClick = {
                                selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                            }) {
                                Text("▼", fontSize = 16.sp, color = TextSecondary)
                            }
                        }

                        // AM/PM
                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isAm) NavyDark else Color(0xFFE8EAF0))
                                    .clickable { isAm = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "AM",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (isAm) Color.White else TextSecondary
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (!isAm) NavyDark else Color(0xFFE8EAF0))
                                    .clickable { isAm = false }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "PM",
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = if (!isAm) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Hint text
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕐", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text("Geser untuk menyesuaikan waktu", fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // ── Nama Pengingat ───────────────────────────────────────────
                Text(
                    text          = "NAMA PENGINGAT",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = reminderLabel,
                    onValueChange = { reminderLabel = it },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(20.dp),
                    placeholder   = { Text("Minum Pagi", color = TextSecondary) },
                    leadingIcon   = { Text("❯", fontSize = 16.sp, color = TealAccent) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = TealAccent,
                        unfocusedBorderColor = Color(0xFFE0E4ED)
                    ),
                    singleLine    = true
                )

                Spacer(Modifier.height(24.dp))

                // ── Ulangi Setiap Hari ───────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text          = "ULANGI SETIAP HARI",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text     = "Pilih Semua",
                        fontSize = 13.sp,
                        color    = TealAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            selectedDays = if (selectedDays.size == 7) emptySet()
                            else dayKeys.toSet()
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    allDays.forEachIndexed { index, dayLabel ->
                        val key       = dayKeys[index]
                        val isSelected = key in selectedDays
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DayChipActive else Color(0xFFE8EAF0))
                                .clickable {
                                    selectedDays = if (isSelected)
                                        selectedDays - key else selectedDays + key
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = dayLabel,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Bunyi Notifikasi ─────────────────────────────────────────
                Text(
                    text          = "BUNYI NOTIFIKASI",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Flowing Stream
                    SoundOption(
                        modifier    = Modifier.weight(1f),
                        icon        = "〰️",
                        title       = "Flowing Stream",
                        subtitle    = "Suara Air Mengalir",
                        isSelected  = selectedSound == "flowing",
                        onClick     = { selectedSound = "flowing" }
                    )
                    // Gentle Drop
                    SoundOption(
                        modifier    = Modifier.weight(1f),
                        icon        = "🔔",
                        title       = "Gentle Drop",
                        subtitle    = "Tetesan Air",
                        isSelected  = selectedSound == "drop",
                        onClick     = { selectedSound = "drop" }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Getaran ───────────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFF5F6FA)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyDark),
                            contentAlignment = Alignment.Center
                        ) { Text("📳", fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Getaran", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Text("Aktifkan getaran saat alarm berbunyi", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked         = vibrationOn,
                            onCheckedChange = { vibrationOn = it },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = Color.White,
                                checkedTrackColor   = TealAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCDD3DC)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Save Button ───────────────────────────────────────────────
                Button(
                    onClick = {
                        val actualHour = if (!isAm && selectedHour != 12) selectedHour + 12
                        else if (isAm && selectedHour == 12) 0
                        else selectedHour
                        val days = if (selectedDays.size == 7) listOf("EVERYDAY")
                        else selectedDays.toList()
                        onSave(actualHour, selectedMinute, reminderLabel, days, 2 * 3600_000L)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyDark,
                        contentColor   = Color.White
                    )
                ) {
                    Text("✓", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Simpan Reminder",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Sound Option Card ──────────────────────────────────────────────────────────
@Composable
fun SoundOption(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isSelected) TealLight else Color(0xFFF5F6FA)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = if (isSelected) BorderStroke(0.dp, Color.Transparent) else null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(icon, fontSize = 24.sp)
                Spacer(Modifier.height(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(NavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
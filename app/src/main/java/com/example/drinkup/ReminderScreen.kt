package com.example.drinkup

import android.os.Build
import android.provider.Settings
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat

// ── Data Model ────────────────────────────────────────────────────────────────
data class ReminderItem(
    val id        : Int,
    val label     : String,
    val hour      : Int,
    val minute    : Int,
    val days      : List<String>,
    val isActive  : Boolean,
    val vibration : Boolean = true
)

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen() {
    val db  = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    var reminders       by remember { mutableStateOf(listOf<ReminderItem>()) }
    val context         = LocalContext.current
    val colorScheme     = MaterialTheme.colorScheme
    var showAddSheet    by remember { mutableStateOf(false) }
    var showPermWarning by remember { mutableStateOf(false) }
    var deleteTarget    by remember { mutableStateOf<ReminderItem?>(null) }
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val permLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) showPermWarning = true
        }
    } else null

    // ── Helper: refresh list dari Firestore ───────────────────────────────────
    reminders = result.map { doc ->

        val item = ReminderItem(
            id = doc.getLong("reminderId")!!.toInt(),
            label = doc.getString("label") ?: "",
            hour = doc.getLong("hour")!!.toInt(),
            minute = doc.getLong("minute")!!.toInt(),
            days = doc.get("days") as List<String>,
            isActive = doc.getBoolean("isActive") ?: true
        )

        // 🔥 INI KUNCI NOTIF HIDUP LAGI
        if (item.isActive) {
            AlarmHelper.scheduleAlarm(
                context,
                item.id,
                item.hour,
                item.minute,
                item.days,
                item.label,
                true
            )
        }

        item
    }

    // ── Toggle aktif/nonaktif ─────────────────────────────────────────────────
    fun toggleReminder(item: ReminderItem, on: Boolean) {
        if (uid == null) return
        db.collection("users")
            .document(uid)
            .collection("reminders")
            .get()
            .addOnSuccessListener { result ->
                result.find { it.getLong("reminderId")!!.toInt() == item.id }
                    ?.reference
                    ?.update("isActive", on)
            }
    }

    // ── Load awal ─────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        refreshReminders()
    }

    // ── Konfirmasi Hapus Dialog ───────────────────────────────────────────────
    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus Reminder?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Apakah kamu yakin ingin menghapus \"${target.label}\"?\n" +
                            "Alarm ini tidak akan berbunyi lagi."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (uid != null) {
                            db.collection("users")
                                .document(uid)
                                .collection("reminders")
                                .get()
                                .addOnSuccessListener { result ->
                                    result.find { it.getLong("reminderId")!!.toInt() == target.id }
                                        ?.reference
                                        ?.delete()
                                        ?.addOnSuccessListener { refreshReminders() }
                                }
                        }
                        deleteTarget = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Batal")
                }
            }
        ) // ← closing AlertDialog yang benar
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Page Title ─────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text       = "Reminders",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text     = "Keep your hydration flowing throughout the day.",
                        fontSize = 14.sp,
                        color    = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── + Tambah Reminder Button ───────────────────────────────────
            item {
                Button(
                    onClick  = { showAddSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(54.dp),
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor   = colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("+ Tambah Reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Permission Warning ─────────────────────────────────────────
            if (showPermWarning) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier          = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Notifikasi diblokir",
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Aktifkan notifikasi di pengaturan untuk menerima pengingat.",
                                    fontSize = 13.sp,
                                    color    = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        TextButton(
                            onClick  = {
                                val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                context.startActivity(i)
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 8.dp, bottom = 8.dp)
                        ) {
                            Text("Buka Pengaturan →")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Empty State ────────────────────────────────────────────────
            if (reminders.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💧", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada reminder",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Tap tombol di atas untuk menambahkan",
                                fontSize = 14.sp,
                                color    = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Reminder List ──────────────────────────────────────────────
            items(reminders, key = { it.id }) { item ->
                ReminderCard(
                    item     = item,
                    onToggle = { on -> toggleReminder(item, on) },
                    onDelete = { deleteTarget = item }
                )
            }
        }

        // ── Add Reminder Bottom Sheet ──────────────────────────────────────
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState       = sheetState,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                AddReminderSheet(
                    onDismiss = { showAddSheet = false },
                    onSave    = { hour, minute, label, days, vibration ->
                        if (uid == null) return@AddReminderSheet

                        val reminderId = System.currentTimeMillis().toInt()
                        val data = hashMapOf(
                            "reminderId" to reminderId,
                            "hour"       to hour,
                            "minute"     to minute,
                            "label"      to label,
                            "days"       to days,
                            "isActive"   to true
                        )

                        db.collection("users")
                            .document(uid)
                            .collection("reminders")
                            .add(data)
                            .addOnSuccessListener {
                                // Refresh list SETELAH data berhasil disimpan
                                refreshReminders()
                                showAddSheet = false
                            }
                    }
                )
            }
        }
    }
}

// ── Reminder Card ─────────────────────────────────────────────────────────────
@Composable
fun ReminderCard(
    item    : ReminderItem,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeStr     = String.format("%02d:%02d", item.hour, item.minute)
    val isActive    = item.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isActive) colorScheme.surface
            else colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            // ── Baris 1: Jam + Toggle + Hapus ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = timeStr,
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (isActive) colorScheme.onSurface
                    else colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.Delete,
                            contentDescription = "Hapus reminder",
                            tint               = colorScheme.error.copy(alpha = 0.7f),
                            modifier           = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Switch(
                        checked         = isActive,
                        onCheckedChange = onToggle,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = Color.White,
                            checkedTrackColor   = colorScheme.secondary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = colorScheme.outline
                        )
                    )
                }
            }

            // ── Label ──────────────────────────────────────────────────────
            Text(
                text       = item.label,
                fontSize   = 14.sp,
                color      = if (isActive) colorScheme.secondary
                else colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(10.dp))

            // ── Days Chips ─────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val dayLabels = when {
                    item.days.contains("EVERYDAY") -> listOf("EVERYDAY")
                    item.days.contains("WEEKENDS") -> listOf("WEEKENDS")
                    else                           -> item.days
                }
                dayLabels.forEach { day ->
                    DayChip(label = day, isActive = isActive)
                }
            }
        }
    }
}

// ── Day Chip ──────────────────────────────────────────────────────────────────
@Composable
fun DayChip(label: String, isActive: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isActive) colorScheme.secondaryContainer
                else colorScheme.surfaceVariant
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (isActive) colorScheme.onSecondaryContainer
            else colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

// ── Add Reminder Sheet ────────────────────────────────────────────────────────
@Composable
fun AddReminderSheet(
    onDismiss : () -> Unit,
    onSave    : (hour: Int, minute: Int, label: String, days: List<String>, vibration: Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var selectedHour   by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm           by remember { mutableStateOf(true) }
    var reminderLabel  by remember { mutableStateOf("") }
    var vibrationOn    by remember { mutableStateOf(true) }

    val allDays  = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    val dayKeys  = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.outline)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(20.dp))
        Text(
            text       = "Tambah Reminder",
            fontSize   = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = colorScheme.onSurface,
            modifier   = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(20.dp))

        // ── Time Picker ───────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Jam
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { if (selectedHour < 12) selectedHour++ else selectedHour = 1 }) {
                    Text("▲", fontSize = 18.sp, color = colorScheme.secondary)
                }
                Text(
                    text       = String.format("%02d", selectedHour),
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.onSurface
                )
                IconButton(onClick = { if (selectedHour > 1) selectedHour-- else selectedHour = 12 }) {
                    Text("▼", fontSize = 18.sp, color = colorScheme.secondary)
                }
            }

            Text(
                text       = ":",
                fontSize   = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = colorScheme.onSurface,
                modifier   = Modifier.padding(horizontal = 8.dp)
            )

            // Menit
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { selectedMinute = (selectedMinute + 5) % 60 }) {
                    Text("▲", fontSize = 18.sp, color = colorScheme.secondary)
                }
                Text(
                    text       = String.format("%02d", selectedMinute),
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.onSurface
                )
                IconButton(onClick = { selectedMinute = (selectedMinute - 5 + 60) % 60 }) {
                    Text("▼", fontSize = 18.sp, color = colorScheme.secondary)
                }
            }

            Spacer(Modifier.width(16.dp))

            // AM/PM
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                listOf(true to "AM", false to "PM").forEach { (amVal, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAm == amVal) colorScheme.secondary else colorScheme.surfaceVariant
                            )
                            .clickable { isAm = amVal }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text       = label,
                            fontWeight = FontWeight.Bold,
                            color      = if (isAm == amVal) Color.White else colorScheme.onSurfaceVariant
                        )
                    }
                    if (amVal) Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {

            // ── Nama Pengingat ─────────────────────────────────────────────
            Text(
                text          = "NAMA PENGINGAT",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = reminderLabel,
                onValueChange = { reminderLabel = it },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(20.dp),
                placeholder   = { Text("Minum Pagi", color = colorScheme.onSurfaceVariant) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colorScheme.secondary,
                    unfocusedBorderColor = colorScheme.outline
                ),
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            // ── Pilih Hari ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text          = "ULANGI SETIAP HARI",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text       = "Pilih Semua",
                    fontSize   = 13.sp,
                    color      = colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.clickable {
                        selectedDays = if (selectedDays.size == 7) emptySet() else dayKeys.toSet()
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                allDays.forEachIndexed { index, dayLabel ->
                    val key        = dayKeys[index]
                    val isSelected = key in selectedDays
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colorScheme.secondary else colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedDays = if (isSelected) selectedDays - key else selectedDays + key
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = dayLabel,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (isSelected) Color.White else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Getaran ────────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
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
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) { Text("📳", fontSize = 20.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Getaran",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = colorScheme.onSurface
                        )
                        Text(
                            "Aktifkan getaran saat alarm berbunyi",
                            fontSize = 12.sp,
                            color    = colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked         = vibrationOn,
                        onCheckedChange = { vibrationOn = it },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = Color.White,
                            checkedTrackColor   = colorScheme.secondary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = colorScheme.outline
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Simpan Button ──────────────────────────────────────────────
            Button(
                onClick = {
                    if (selectedDays.isEmpty()) return@Button
                    val actualHour = when {
                        !isAm && selectedHour != 12 -> selectedHour + 12
                        isAm && selectedHour == 12  -> 0
                        else                        -> selectedHour
                    }
                    val finalDays  = if (selectedDays.size == 7) listOf("EVERYDAY")
                    else dayKeys.filter { it in selectedDays }
                    val finalLabel = reminderLabel.ifBlank { "Minum Air" }
                    onSave(actualHour, selectedMinute, finalLabel, finalDays, vibrationOn)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor   = colorScheme.onPrimary
                ),
                enabled = selectedDays.isNotEmpty()
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
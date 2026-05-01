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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Data Model ────────────────────────────────────────────────────────────────
data class ReminderItem(
    val id        : String  = "",   // Firestore document ID (String)
    val label     : String  = "",
    val hour      : Int     = 8,
    val minute    : Int     = 0,
    val days      : List<String> = emptyList(),
    val isActive  : Boolean = true,
    val vibration : Boolean = true
)

// ── Firestore helpers ─────────────────────────────────────────────────────────
private fun remindersCollection() = FirebaseFirestore.getInstance()
    .collection("users")
    .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    .collection("reminders")

private fun ReminderItem.toMap() = mapOf(
    "label"     to label,
    "hour"      to hour,
    "minute"    to minute,
    "days"      to days,
    "isActive"  to isActive,
    "vibration" to vibration
)

private fun Map<String, Any>.toReminderItem(id: String) = ReminderItem(
    id        = id,
    label     = this["label"] as? String ?: "",
    hour      = (this["hour"] as? Long)?.toInt() ?: 8,
    minute    = (this["minute"] as? Long)?.toInt() ?: 0,
    days      = (this["days"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
    isActive  = this["isActive"] as? Boolean ?: true,
    vibration = this["vibration"] as? Boolean ?: true
)

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen() {
    val colorScheme = MaterialTheme.colorScheme
    val context     = LocalContext.current

    var reminders   by remember { mutableStateOf<List<ReminderItem>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }

    var showAddSheet    by remember { mutableStateOf(false) }
    var showPermWarning by remember { mutableStateOf(false) }
    val sheetState      = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Load reminders dari Firestore (realtime) ──────────────────────────────
    DisposableEffect(Unit) {
        val reg = remindersCollection()
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val loaded = snap.documents.mapNotNull { doc ->
                        (doc.data ?: return@mapNotNull null).toReminderItem(doc.id)
                    }.sortedWith(compareBy({ it.hour }, { it.minute }))
                    reminders = loaded
                    isLoading = false

                    // Re-schedule alarm yang aktif (supaya tetap jalan setelah login ulang)
                    loaded.filter { it.isActive }.forEach { item ->
                        AlarmHelper.scheduleReminder(
                            context    = context,
                            reminderId = item.id.hashCode(),
                            hour       = item.hour,
                            minute     = item.minute,
                            days       = item.days,
                            label      = item.label,
                            vibration  = item.vibration
                        )
                    }
                }
            }
        onDispose { reg.remove() }
    }

    val permLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) showPermWarning = true
        }
    } else null

    // ── Toggle reminder (update Firestore + alarm) ────────────────────────────
    fun toggleReminder(item: ReminderItem, on: Boolean) {
        if (on && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                permLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            else showPermWarning = true
            return
        }
        remindersCollection().document(item.id).update("isActive", on)
        if (on) {
            AlarmHelper.scheduleReminder(
                context    = context,
                reminderId = item.id.hashCode(),
                hour       = item.hour,
                minute     = item.minute,
                days       = item.days,
                label      = item.label,
                vibration  = item.vibration
            )
        } else {
            AlarmHelper.cancelReminder(context, item.id.hashCode(), item.days)
        }
    }

    // ── Tambah reminder baru (simpan ke Firestore) ────────────────────────────
    fun addReminder(
        hour      : Int,
        minute    : Int,
        label     : String,
        days      : List<String>,
        vibration : Boolean
    ) {
        val newItem = ReminderItem(
            id        = "",
            label     = label,
            hour      = hour,
            minute    = minute,
            days      = days,
            isActive  = true,
            vibration = vibration
        )
        remindersCollection().add(newItem.toMap())
            .addOnSuccessListener { docRef ->
                AlarmHelper.scheduleReminder(
                    context    = context,
                    reminderId = docRef.id.hashCode(),
                    hour       = hour,
                    minute     = minute,
                    days       = days,
                    label      = label,
                    vibration  = vibration
                )
            }
    }

    // ── Hapus reminder (dari Firestore + cancel alarm) ────────────────────────
    fun deleteReminder(item: ReminderItem) {
        AlarmHelper.cancelReminder(context, item.id.hashCode(), item.days)
        remindersCollection().document(item.id).delete()
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

            // ── Loading ────────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
            }

            // ── Reminder List / Empty State ────────────────────────────────
            if (!isLoading && reminders.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔕", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada reminder",
                                fontWeight = FontWeight.SemiBold,
                                color      = colorScheme.onSurfaceVariant,
                                fontSize   = 15.sp
                            )
                            Text(
                                "Klik + Tambah Reminder untuk membuat baru",
                                color    = colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(reminders, key = { it.id }) { item ->
                    ReminderCard(
                        item     = item,
                        onToggle = { on -> toggleReminder(item, on) },
                        onDelete = { deleteReminder(item) }
                    )
                }
            }

            // ── Info Card ──────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.tertiaryContainer)
                ) {
                    Box {
                        Text(
                            text     = "💧",
                            fontSize = 72.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .offset(y = 8.dp),
                            color = Color(0x33000000)
                        )
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text       = "Smart Reminders",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text       = "We'll space out your alerts based on your daily goal and waking hours for optimal cellular hydration.",
                                fontSize   = 13.sp,
                                color      = colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                lineHeight = 20.sp,
                                modifier   = Modifier.fillMaxWidth(0.72f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
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
                        addReminder(hour, minute, label, days, vibration)
                        showAddSheet = false
                    }
                )
            }
        }
    }
}

// ── Reminder Card ─────────────────────────────────────────────────────────────
@Composable
fun ReminderCard(
    item     : ReminderItem,
    onToggle : (Boolean) -> Unit,
    onDelete : () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeStr     = String.format("%02d:%02d", item.hour, item.minute)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (item.isActive) colorScheme.surface
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
                Column {
                    Text(
                        text       = timeStr,
                        fontSize   = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (item.isActive) colorScheme.onSurface else colorScheme.onSurfaceVariant
                    )
                    Text(
                        text     = item.label,
                        fontSize = 12.sp,
                        color    = if (item.isActive) colorScheme.secondary else colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector        = Icons.Filled.Delete,
                            contentDescription = "Hapus reminder",
                            tint               = colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked         = item.isActive,
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

            Spacer(Modifier.height(10.dp))

            // ── Days Chips ─────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val dayLabels = when {
                    item.days.contains("EVERYDAY") -> listOf("EVERYDAY")
                    item.days.contains("WEEKENDS") -> listOf("WEEKENDS")
                    else                           -> item.days
                }
                dayLabels.forEach { day ->
                    DayChip(label = day, isActive = item.isActive)
                }
            }
        }
    }

    // ── Konfirmasi hapus ───────────────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title   = { Text("Hapus Reminder?", fontWeight = FontWeight.Bold) },
            text    = { Text("Reminder \"${item.label}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
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

        // ── Clock Picker ──────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier         = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { selectedHour = (selectedHour % 12) + 1 }) {
                            Text("▲", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text       = String.format("%02d", selectedHour),
                            fontSize   = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = colorScheme.onSurface
                        )
                        Text("JAM", fontSize = 11.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        IconButton(onClick = {
                            selectedHour = if (selectedHour == 1) 12 else selectedHour - 1
                        }) {
                            Text("▼", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        ":",
                        fontSize   = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color      = colorScheme.onSurface,
                        modifier   = Modifier.padding(horizontal = 4.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { selectedMinute = (selectedMinute + 1) % 60 }) {
                            Text("▲", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text       = String.format("%02d", selectedMinute),
                            fontSize   = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = colorScheme.onSurface
                        )
                        Text("MENIT", fontSize = 11.sp, color = colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        IconButton(onClick = {
                            selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                        }) {
                            Text("▼", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(
                        modifier            = Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isAm) colorScheme.primary else colorScheme.surfaceVariant)
                                .clickable { isAm = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "AM",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isAm) colorScheme.primary else colorScheme.surfaceVariant)
                                .clickable { isAm = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PM",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (!isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier              = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("🕐", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text("Geser untuk menyesuaikan waktu", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
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
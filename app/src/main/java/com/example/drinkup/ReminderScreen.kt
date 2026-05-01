package com.example.drinkup

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
        // Update Firestore
        remindersCollection().document(item.id)
            .update("isActive", on)

        // Update alarm
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
            id        = "",   // akan diisi Firestore
            label     = label,
            hour      = hour,
            minute    = minute,
            days      = days,
            isActive  = true,
            vibration = vibration
        )
        remindersCollection().add(newItem.toMap())
            .addOnSuccessListener { docRef ->
                // Schedule alarm pakai docRef.id.hashCode() sebagai reminderId
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
                                Text(
                                    "Aktifkan di pengaturan HP kamu",
                                    fontSize = 11.sp,
                                    color    = Color(0xFFBF360C)
                                )
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

            // ── Reminder List ──────────────────────────────────────────────
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
            AddReminderSheet(
                sheetState = sheetState,
                onDismiss  = { showAddSheet = false },
                onSave     = { hour, minute, label, days, vibration ->
                    addReminder(hour, minute, label, days, vibration)
                    showAddSheet = false
                }
            )
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
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isActive) colorScheme.surface else colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
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
                    // Tombol hapus
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint     = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.days.forEach { day ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (item.isActive) colorScheme.secondary.copy(alpha = 0.18f)
                                else colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text       = day,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = if (item.isActive) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                    }
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

// ── Add Reminder Bottom Sheet ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    sheetState : SheetState,
    onDismiss  : () -> Unit,
    onSave     : (hour: Int, minute: Int, label: String, days: List<String>, vibration: Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var selectedHour   by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(30) }
    var isAm           by remember { mutableStateOf(true) }
    var reminderLabel  by remember { mutableStateOf("") }
    var vibrationOn    by remember { mutableStateOf(true) }
    var selectedDays   by remember { mutableStateOf(setOf("MON","TUE","WED","THU","FRI")) }

    val allDays = listOf("S","M","T","W","T","F","S")
    val dayKeys = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle       = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = colorScheme.onSurface)
                }
                Text(
                    text       = "Tambah Reminder",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.onSurface
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null, tint = colorScheme.onSurface)
                }
            }

            // ── Clock Picker ──────────────────────────────────────────────
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

                        Text(":", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface, modifier = Modifier.padding(horizontal = 4.dp))

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
                                Text("AM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
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
                                Text("PM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant)
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

                // ── Nama Pengingat ─────────────────────────────────────────
                Text("NAMA PENGINGAT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = reminderLabel,
                    onValueChange = { reminderLabel = it },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(20.dp),
                    placeholder   = { Text("Minum Pagi", color = colorScheme.onSurfaceVariant) },
                    leadingIcon   = { Text("❯", fontSize = 16.sp, color = colorScheme.secondary) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colorScheme.secondary,
                        unfocusedBorderColor = colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                // ── Ulangi Setiap Hari ─────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("ULANGI SETIAP HARI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
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
                                .background(if (isSelected) colorScheme.secondary else colorScheme.surfaceVariant)
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - key else selectedDays + key
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = dayLabel,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isSelected) Color.White else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Getaran ────────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth().padding(16.dp),
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
                            Text("Getaran", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colorScheme.onSurface)
                            Text("Aktifkan getaran saat alarm berbunyi", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
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

                // ── Simpan Button ──────────────────────────────────────────
                Button(
                    onClick = {
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape  = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor   = colorScheme.onPrimary
                    )
                ) {
                    Text("✓", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan Reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
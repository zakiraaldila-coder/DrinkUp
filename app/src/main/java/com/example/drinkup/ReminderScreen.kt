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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Data Model ────────────────────────────────────────────────────────────────
data class ReminderItem(
    val id        : String  = "",
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

    DisposableEffect(Unit) {
        val reg = remindersCollection()
            .addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val loaded = snap.documents.mapNotNull { doc ->
                        (doc.data ?: return@mapNotNull null).toReminderItem(doc.id)
                    }.sortedWith(compareBy({ it.hour }, { it.minute }))
                    reminders = loaded
                    isLoading = false

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

    fun addReminder(hour: Int, minute: Int, label: String, days: List<String>, vibration: Boolean) {
        val newItem = ReminderItem(id = "", label = label, hour = hour, minute = minute,
            days = days, isActive = true, vibration = vibration)
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

    fun deleteReminder(item: ReminderItem) {
        AlarmHelper.cancelReminder(context, item.id.hashCode(), item.days)
        remindersCollection().document(item.id).delete()
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(text = "Reminders", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text(text = "Keep your hydration flowing throughout the day.",
                        fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(20.dp))
            }

            item {
                Button(
                    onClick  = { showAddSheet = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(54.dp),
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

            if (showPermWarning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape  = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Notifikasi diblokir", fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer)
                                Text("Aktifkan notifikasi di pengaturan untuk menerima pengingat.",
                                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                        TextButton(
                            onClick  = {
                                val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                context.startActivity(i)
                            },
                            modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 8.dp)
                        ) { Text("Buka Pengaturan →") }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
            }

            if (!isLoading && reminders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔕", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Belum ada reminder", fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurfaceVariant, fontSize = 15.sp)
                            Text("Klik + Tambah Reminder untuk membuat baru",
                                color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
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

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.tertiaryContainer)
                ) {
                    Box {
                        Text(text = "💧", fontSize = 72.sp,
                            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).offset(y = 8.dp),
                            color = Color(0x33000000))
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(text = "Smart Reminders", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                                color = colorScheme.onTertiaryContainer)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "We'll space out your alerts based on your daily goal and waking hours for optimal cellular hydration.",
                                fontSize = 13.sp,
                                color = colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                                lineHeight = 20.sp,
                                modifier = Modifier.fillMaxWidth(0.72f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

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

    // ── Warna adaptif berdasarkan status aktif ────────────────────────────────
    val cardBg      = if (item.isActive) colorScheme.surface
    else colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val timeColor   = if (item.isActive) colorScheme.onSurface
    else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val labelColor  = if (item.isActive) colorScheme.secondary
    else colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val borderColor = if (item.isActive) Color.Transparent
    else colorScheme.outline.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .then(
                // Tambah border tipis saat non-aktif agar batas card tetap terlihat
                if (!item.isActive) Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            ),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isActive) 2.dp else 0.dp   // flat saat non-aktif
        ),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

            // ── Baris 1: Jam + Toggle + Hapus ─────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    // Badge "Nonaktif" kecil di atas jam
                    if (!item.isActive) {
                        Surface(
                            shape  = RoundedCornerShape(6.dp),
                            color  = colorScheme.outline.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text     = "NONAKTIF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color    = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text       = timeStr,
                        fontSize   = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = timeColor,
                        // Strikethrough tipis saat non-aktif sebagai visual cue
                        textDecoration = if (!item.isActive) TextDecoration.None else TextDecoration.None
                    )
                    Text(
                        text     = item.label,
                        fontSize = 12.sp,
                        color    = labelColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector        = Icons.Filled.Delete,
                            contentDescription = "Hapus reminder",
                            tint               = colorScheme.error.copy(
                                alpha = if (item.isActive) 0.7f else 0.4f
                            )
                        )
                    }
                    Switch(
                        checked         = item.isActive,
                        onCheckedChange = onToggle,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = Color.White,
                            checkedTrackColor    = colorScheme.secondary,
                            uncheckedThumbColor  = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            uncheckedTrackColor  = colorScheme.outline.copy(alpha = 0.3f),
                            uncheckedBorderColor = colorScheme.outline.copy(alpha = 0.3f)
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
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
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
                else colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (!isActive) 1.dp else 0.dp,
                color = if (!isActive) colorScheme.outline.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (isActive) colorScheme.onSecondaryContainer
            else colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

// ── Add Reminder Sheet ────────────────────────────────────────────────────────
// (tidak ada perubahan pada AddReminderSheet — bagian ini tidak perlu diubah)
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
    }
}
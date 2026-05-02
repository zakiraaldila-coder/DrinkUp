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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// ── Data Model ────────────────────────────────────────────────────────────────
data class ReminderItem(
    val id        : String       = "",
    val label     : String       = "",
    val hour      : Int          = 8,
    val minute    : Int          = 0,
    val days      : List<String> = emptyList(),
    val isActive  : Boolean      = true,
    val vibration : Boolean      = true
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
                    // ✅ TIDAK ada scheduleReminder di sini.
                    // Snapshot listener fire setiap kali Firestore berubah,
                    // termasuk setelah addReminder/toggleReminder — sehingga
                    // kalau schedule dilakukan di sini akan dobel dengan
                    // schedule yang sudah dilakukan di addReminder/toggleReminder.
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
        val newItem = ReminderItem(
            id = "", label = label, hour = hour, minute = minute,
            days = days, isActive = true, vibration = vibration
        )
        remindersCollection().add(newItem.toMap())
            .addOnSuccessListener { docRef ->
                val reminderId = docRef.id.hashCode()
                // ✅ Cancel dulu sebelum schedule — pastikan tidak ada
                // sisa alarm lama dengan reminderId yang sama di AlarmManager
                AlarmHelper.cancelReminder(context, reminderId, days)
                AlarmHelper.scheduleReminder(
                    context    = context,
                    reminderId = reminderId,
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

            if (showPermWarning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
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
                        ) { Text("Buka Pengaturan →") }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

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
                items(items = reminders, key = { it.id }) { item ->
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
                    shape    = RoundedCornerShape(24.dp),
                    colors   = CardDefaults.cardColors(containerColor = colorScheme.tertiaryContainer)
                ) {
                    Box {
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
    val colorScheme       = MaterialTheme.colorScheme
    val timeStr           = String.format("%02d:%02d", item.hour, item.minute)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // ✅ FIX #4: State aktif & nonaktif yang konsisten secara visual — tidak "jauh berubah"
    // Prinsip: card nonaktif tetap punya struktur yang sama, hanya lebih redup & ada badge
    val cardBg    = colorScheme.surface  // selalu sama, bedanya cuma opacity konten
    val alpha     = if (item.isActive) 1f else 0.45f
    val timeColor = colorScheme.onSurface.copy(alpha = alpha)
    val labelColor = if (item.isActive) colorScheme.secondary
    else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isActive) 2.dp else 0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (item.isActive) colorScheme.surface
            else colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    // ✅ FIX #4: Badge "NONAKTIF" lebih rapi — sama posisi, tidak menggeser layout
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier.padding(bottom = 4.dp)
                    ) {
                        if (!item.isActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colorScheme.errorContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text          = "NONAKTIF",
                                    fontSize      = 9.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = colorScheme.onErrorContainer.copy(alpha = 0.75f),
                                    letterSpacing = 1.sp,
                                    modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text           = timeStr,
                        fontSize       = 36.sp,
                        fontWeight     = FontWeight.ExtraBold,
                        color          = timeColor,
                        textDecoration = TextDecoration.None
                    )
                    Text(text = item.label, fontSize = 12.sp, color = labelColor)
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

// ── Number Picker ─────────────────────────────────────────────────────────────
@Composable
fun NumberPicker(
    value : Int,
    onUp  : () -> Unit,
    onDown: () -> Unit,
    label : String
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onUp) {
            Text("▲", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
        }
        Text(
            text       = String.format("%02d", value),
            fontSize   = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = colorScheme.onSurface
        )
        IconButton(onClick = onDown) {
            Text("▼", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
        }
        Text(
            text          = label,
            fontSize      = 11.sp,
            color         = colorScheme.onSurfaceVariant,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 1.sp
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

    var displayHour    by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var isAm           by remember { mutableStateOf(true) }
    var reminderLabel  by remember { mutableStateOf("") }
    var vibrationOn    by remember { mutableStateOf(true) }

    val allDayLabels = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    val dayKeys      = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    // ✅ Konversi 12-jam → 24-jam dengan derivedStateOf (bukan property getter)
    val hour24 by remember {
        derivedStateOf {
            when {
                isAm && displayHour == 12  -> 0
                !isAm && displayHour != 12 -> displayHour + 12
                else                       -> displayHour
            }
        }
    }

    val canSave = selectedDays.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
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

        Spacer(Modifier.height(24.dp))

        // ── Time Picker ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                NumberPicker(
                    value  = displayHour,
                    onUp   = { displayHour = if (displayHour == 12) 1 else displayHour + 1 },
                    onDown = { displayHour = if (displayHour == 1) 12 else displayHour - 1 },
                    label  = "JAM"
                )

                // ✅ FIX: padding(horizontal, bottom) tidak valid — gunakan start/end/bottom
                Text(
                    text       = ":",
                    fontSize   = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = colorScheme.onSurface,
                    modifier   = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
                )

                NumberPicker(
                    value  = selectedMinute,
                    onUp   = { selectedMinute = if (selectedMinute == 59) 0 else selectedMinute + 1 },
                    onDown = { selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1 },
                    label  = "MENIT"
                )

                Spacer(Modifier.width(16.dp))

                // ✅ FIX: Surface(onClick) → Box + clickable
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(if (isAm) colorScheme.primary else colorScheme.surface)
                            .clickable { isAm = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "AM",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            color      = if (isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(if (!isAm) colorScheme.primary else colorScheme.surface)
                            .clickable { isAm = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "PM",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            color      = if (!isAm) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text     = "🕐  Geser untuk menyesuaikan waktu",
                fontSize = 12.sp,
                color    = colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Nama Pengingat ────────────────────────────────────────────────────
        Text(
            text          = "NAMA PENGINGAT",
            fontSize      = 11.sp,
            fontWeight    = FontWeight.Bold,
            color         = colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier      = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = reminderLabel,
            onValueChange = { reminderLabel = it },
            placeholder   = {
                Text("Minum Pagi", color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            },
            shape      = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier   = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = colorScheme.primary,
                unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.4f)
            )
        )

        Spacer(Modifier.height(24.dp))

        // ── Hari Pengulangan ──────────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = "ULANGI SETIAP HARI",
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            TextButton(
                onClick = {
                    selectedDays = if (selectedDays.size == 7) emptySet() else dayKeys.toSet()
                }
            ) {
                Text(
                    text       = "Pilih Semua",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ✅ FIX: Surface(onClick) → Box + clickable
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            allDayLabels.forEachIndexed { i, dayLabel ->
                val key        = dayKeys[i]
                val isSelected = key in selectedDays
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) colorScheme.primary else colorScheme.surfaceVariant
                        )
                        .clickable {
                            selectedDays = if (isSelected) selectedDays - key else selectedDays + key
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = dayLabel,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isSelected) colorScheme.onPrimary
                        else colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Getaran ───────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📳", fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Getaran",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp,
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
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colorScheme.secondary
                )
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Tombol Simpan ─────────────────────────────────────────────────────
        Button(
            onClick  = {
                val label = reminderLabel.ifBlank { "Minum Air" }
                onSave(hour24, selectedMinute, label, selectedDays.toList(), vibrationOn)
            },
            enabled  = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp),
            shape  = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor         = colorScheme.primary,
                contentColor           = colorScheme.onPrimary,
                disabledContainerColor = colorScheme.surfaceVariant,
                disabledContentColor   = colorScheme.onSurfaceVariant
            )
        ) {
            Text("✓  Simpan Reminder", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
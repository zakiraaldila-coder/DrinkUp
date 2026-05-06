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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.Canvas

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

// ── Poppins FontFamily ────────────────────────────────────────────────────────
private val Poppins = FontFamily(
    Font(R.font.poppins_reguler,   FontWeight.Normal),
    Font(R.font.poppins_medium,    FontWeight.Medium),
    Font(R.font.poppins_semibold,  FontWeight.SemiBold),
    Font(R.font.poppins_bold,      FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
)

// ── Design tokens ─────────────────────────────────────────────────────────────
private val RBgDeep     = Color(0xFF0B1629)
private val RBgCard     = Color(0xFF112240)
private val RCyan       = Color(0xFF00E5FF)
private val RBlue       = Color(0xFF2979FF)
private val RTeal       = Color(0xFF00BFA5)
private val RTextPrimary= Color(0xFFE8F0FE)
private val RTextSec    = Color(0xFF7B93B8)
private val RTextMuted  = Color(0xFF3D5A80)

// ── Canvas: Bell Icon ─────────────────────────────────────────────────────────
@Composable
private fun BellIcon(modifier: Modifier = Modifier, color: Color = RCyan, muted: Boolean = false) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height; val cx = w / 2f
        val bodyPath = Path().apply {
            moveTo(cx, 0f)
            cubicTo(cx + w * .42f, 0f, cx + w * .42f, h * .55f, cx + w * .48f, h * .72f)
            lineTo(cx - w * .48f, h * .72f)
            cubicTo(cx - w * .42f, h * .55f, cx - w * .42f, 0f, cx, 0f)
            close()
        }
        drawPath(bodyPath, color)
        drawArc(Color.White.copy(.5f), 200f, 140f, false,
            Offset(cx - w * .08f, -h * .04f), Size(w * .16f, h * .16f),
            style = Stroke(w * .07f, cap = StrokeCap.Round))
        drawRoundRect(color, Offset(cx - w * .48f, h * .68f), Size(w * .96f, h * .12f),
            CornerRadius(w * .04f))
        drawArc(color, 0f, 180f, false, Offset(cx - w * .12f, h * .76f), Size(w * .24f, h * .16f))
        if (muted) {
            drawLine(Color.White.copy(.8f), Offset(w * .15f, h * .15f), Offset(w * .85f, h * .85f),
                w * .09f, StrokeCap.Round)
        }
    }
}

// ── Canvas: Clock Icon ────────────────────────────────────────────────────────
@Composable
private fun ClockIconR(modifier: Modifier = Modifier, color: Color = RCyan) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f; val cy = size.height / 2f; val r = size.width / 2f
        drawCircle(color, r, Offset(cx, cy))
        drawCircle(Color.White.copy(.15f), r * .82f, Offset(cx, cy), style = Stroke(r * .06f))
        drawLine(Color.White, Offset(cx, cy), Offset(cx, cy - r * .50f), r * .08f, StrokeCap.Round)
        drawLine(Color.White, Offset(cx, cy), Offset(cx + r * .36f, cy), r * .07f, StrokeCap.Round)
        drawCircle(Color.White, r * .07f, Offset(cx, cy))
    }
}

// ── Canvas: Vibrate Icon ──────────────────────────────────────────────────────
@Composable
private fun VibrateIcon(modifier: Modifier = Modifier, color: Color = RCyan) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        drawRoundRect(color, Offset(w * .28f, h * .08f), Size(w * .44f, h * .84f), CornerRadius(w * .08f))
        drawLine(color.copy(.6f), Offset(w * .16f, h * .30f), Offset(w * .08f, h * .44f), w * .06f, StrokeCap.Round)
        drawLine(color.copy(.6f), Offset(w * .16f, h * .56f), Offset(w * .08f, h * .70f), w * .06f, StrokeCap.Round)
        drawLine(color.copy(.6f), Offset(w * .84f, h * .30f), Offset(w * .92f, h * .44f), w * .06f, StrokeCap.Round)
        drawLine(color.copy(.6f), Offset(w * .84f, h * .56f), Offset(w * .92f, h * .70f), w * .06f, StrokeCap.Round)
        drawRoundRect(Color.White.copy(.2f), Offset(w * .34f, h * .18f), Size(w * .32f, h * .50f), CornerRadius(w * .04f))
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen() {
    val context     = LocalContext.current

    var reminders   by remember { mutableStateOf<List<ReminderItem>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(true) }
    var isSaving    by remember { mutableStateOf(false) }

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
        val reminderId = item.id.hashCode()
        remindersCollection().document(item.id).update("isActive", on)
        if (on) {
            AlarmHelper.cancelReminder(context, reminderId)
            AlarmHelper.scheduleReminder(context, reminderId, item.hour, item.minute,
                item.days, item.label, item.vibration)
        } else {
            AlarmHelper.cancelReminder(context, reminderId)
        }
    }

    fun addReminder(hour: Int, minute: Int, label: String, days: List<String>, vibration: Boolean) {
        if (isSaving) return
        isSaving = true
        val newItem = ReminderItem(id = "", label = label, hour = hour, minute = minute,
            days = days, isActive = true, vibration = vibration)
        remindersCollection().add(newItem.toMap())
            .addOnSuccessListener { docRef ->
                val reminderId = docRef.id.hashCode()
                AlarmHelper.cancelReminder(context, reminderId)
                AlarmHelper.scheduleReminder(context, reminderId, hour, minute, days, label, vibration)
                isSaving = false
            }
            .addOnFailureListener { isSaving = false }
    }

    fun deleteReminder(item: ReminderItem) {
        AlarmHelper.cancelReminder(context, item.id.hashCode())
        remindersCollection().document(item.id).delete()
    }

    Box(modifier = Modifier.fillMaxSize().background(RBgDeep)) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            // ── Hero Header ──────────────────────────────────────────────────
            item {
                val infiniteTransition = rememberInfiniteTransition(label = "hero")
                val floatY by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = -8f,
                    animationSpec = infiniteRepeatable(
                        tween(3200, easing = FastOutSlowInEasing), RepeatMode.Reverse
                    ), label = "floatY"
                )
                val rotBell by infiniteTransition.animateFloat(
                    initialValue = -3f, targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse
                    ), label = "rotBell"
                )
                val glowR by infiniteTransition.animateFloat(
                    initialValue = .40f, targetValue = .58f,
                    animationSpec = infiniteRepeatable(
                        tween(2800, easing = LinearEasing), RepeatMode.Reverse
                    ), label = "glowR"
                )

                // Hero card + FAB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Hero card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colorStops = arrayOf(
                                        0f    to Color(0xFF0B2240),
                                        0.55f to Color(0xFF0C3050),
                                        1f    to Color(0xFF0A3D45)
                                    ),
                                    start = Offset.Zero,
                                    end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        // Background glow circles
                        Canvas(Modifier.fillMaxSize()) {
                            drawCircle(
                                Brush.radialGradient(
                                    listOf(RCyan.copy(.15f), Color.Transparent),
                                    Offset(size.width * .88f, size.height * .12f),
                                    size.width * glowR
                                ),
                                size.width * glowR,
                                Offset(size.width * .88f, size.height * .12f)
                            )
                            drawCircle(
                                Brush.radialGradient(
                                    listOf(RBlue.copy(.20f), Color.Transparent),
                                    Offset(0f, size.height * .95f),
                                    size.width * .35f
                                ),
                                size.width * .35f,
                                Offset(0f, size.height * .95f)
                            )
                            // Partikel titik
                            listOf(
                                floatArrayOf(.18f, .12f), floatArrayOf(.52f, .08f),
                                floatArrayOf(.38f, .62f), floatArrayOf(.11f, .52f)
                            ).forEachIndexed { i, (rx, ry) ->
                                drawCircle(Color.White.copy(.04f + i * .012f), size.width * .008f,
                                    Offset(size.width * rx, size.height * ry))
                            }
                        }

                        // Bell floating di kanan
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 20.dp)
                                .offset(y = floatY.dp)
                                .graphicsLayer { rotationZ = rotBell }
                                .size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawCircle(RCyan.copy(.06f), size.width / 2f)
                                drawCircle(RCyan.copy(.10f), size.width / 2f,
                                    style = Stroke(size.width * .022f))
                                drawCircle(Color.White.copy(.04f), size.width * .36f,
                                    style = Stroke(size.width * .015f))
                                drawCircle(Color.White.copy(.05f), size.width * .32f)
                            }
                            BellIcon(Modifier.size(46.dp), Color.White.copy(.55f))
                        }

                        // Teks kiri
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 24.dp)
                        ) {
                            // Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(.10f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "PENGINGAT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RCyan,
                                    letterSpacing = 2.sp,
                                    fontFamily = Poppins

                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Reminders",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RTextPrimary,
                                letterSpacing = (-1).sp,
                                fontFamily = Poppins

                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Keep your hydration flowing\nthroughout the day.",
                                fontSize = 12.sp,
                                color = RTextSec,
                                lineHeight = 18.sp,
                                fontFamily = Poppins

                            )
                        }
                    }

                    // FAB Tambah Reminder — di bawah hero card
                }
                Spacer(Modifier.height(4.dp))

                // Tombol Tambah Reminder — full width
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00B4F0), Color(0xFF2979FF))
                            )
                        )
                        .clickable { showAddSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    // Shimmer overlay
                    Canvas(Modifier.fillMaxSize()) {
                        drawRect(
                            Brush.linearGradient(
                                listOf(Color.Transparent, Color.White.copy(.07f), Color.Transparent),
                                Offset(size.width * .25f, 0f),
                                Offset(size.width * .75f, size.height)
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(.20f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            "Tambah Reminder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontFamily = Poppins

                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── Peringatan Izin ──────────────────────────────────────────────
            if (showPermWarning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB71C1C).copy(.9f), Color(0xFFEF5350).copy(.8f))
                                )
                            )
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(.15f)),
                                Alignment.Center
                            ) {
                                BellIcon(Modifier.size(20.dp), Color.White, muted = true)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Notifikasi diblokir", fontWeight = FontWeight.ExtraBold,
                                    color = Color.White, fontSize = 14.sp,
                                    fontFamily = Poppins
                                )
                                Text("Aktifkan notifikasi di pengaturan untuk menerima pengingat.",
                                    fontSize = 12.sp, color = Color.White.copy(.75f), lineHeight = 18.sp,
                                    fontFamily = Poppins
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                context.startActivity(i)
                            },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 4.dp)
                        ) {
                            Text("Buka Pengaturan →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                fontFamily = Poppins
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Loading ──────────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                        CircularProgressIndicator(color = RCyan, strokeWidth = 2.5.dp)
                    }
                }
            }

            // ── Empty State ──────────────────────────────────────────────────
            if (!isLoading && reminders.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(RBgCard),
                            contentAlignment = Alignment.Center
                        ) {
                            BellIcon(Modifier.size(40.dp), RTextMuted)
                        }
                        Spacer(Modifier.height(22.dp))
                        Text(
                            "Belum ada reminder",
                            fontWeight = FontWeight.ExtraBold,
                            color = RTextPrimary,
                            fontSize = 18.sp,
                            fontFamily = Poppins

                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            buildAnnotatedString {
                                append("Klik ")
                                withStyle(SpanStyle(color = RCyan, fontWeight = FontWeight.Bold)) {
                                    append("Tambah Reminder")
                                }
                                append(" untuk membuat baru")
                            },
                            color = RTextSec,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = Poppins

                        )
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

            // ── Smart Reminders Info Card ────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(RBgCard)
                ) {
                    // Drop dekorasi besar kanan
                    Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).size(80.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val path = Path().apply {
                                moveTo(cx, 0f)
                                cubicTo(cx + size.width*.5f, size.height*.4f,
                                    cx + size.width*.5f, size.height*.75f, cx, size.height)
                                cubicTo(cx - size.width*.5f, size.height*.75f,
                                    cx - size.width*.5f, size.height*.4f, cx, 0f)
                                close()
                            }
                            drawPath(path, Color.White.copy(.05f))
                        }
                    }
                    Column(Modifier.padding(20.dp).fillMaxWidth(.75f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RCyan.copy(.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ClockIconR(Modifier.size(20.dp), RCyan)
                            }
                            Text(
                                "Smart Reminders",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RTextPrimary,
                                fontFamily = Poppins

                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "We'll space out your alerts based on your daily goal and waking hours for optimal cellular hydration.",
                            fontSize = 13.sp,
                            color = RTextSec,
                            lineHeight = 20.sp,
                            fontFamily = Poppins

                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Bottom Sheet ─────────────────────────────────────────────────────
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState       = sheetState,
                shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle       = null
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
    val timeStr           = String.format("%02d:%02d", item.hour, item.minute)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val contentAlpha by animateFloatAsState(
        targetValue   = if (item.isActive) 1f else 0.45f,
        animationSpec = tween(300),
        label         = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (item.isActive)
                    Brush.linearGradient(
                        listOf(Color(0xFF0F2347), Color(0xFF102B54)),
                        Offset.Zero,
                        Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                else
                    Brush.linearGradient(
                        listOf(RBgCard, RBgCard),
                        Offset.Zero, Offset.Zero
                    )
            )
    ) {
        // Dekorasi glow sudut kanan atas
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                if (item.isActive) RCyan.copy(.07f) else Color.White.copy(.02f),
                size.width * .32f,
                Offset(size.width * .94f, -size.height * .18f)
            )
        }
        // Accent bar kiri
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(72.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(
                    if (item.isActive)
                        Brush.verticalGradient(listOf(RCyan, RBlue))
                    else
                        Brush.verticalGradient(listOf(RTextMuted, RTextMuted))
                )
        )

        Column(Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    // Badge status
                    if (!item.isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFB71C1C).copy(.45f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("NONAKTIF", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF8A80), letterSpacing = 1.sp,
                                fontFamily = Poppins
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RCyan.copy(.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("AKTIF", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                                color = RCyan, letterSpacing = 1.sp,
                                fontFamily = Poppins
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text          = timeStr,
                        fontSize      = 38.sp,
                        fontWeight    = FontWeight.ExtraBold,
                        color         = if (item.isActive) RTextPrimary else RTextPrimary.copy(.35f),
                        textDecoration= TextDecoration.None,
                        letterSpacing = (-1).sp,
                        fontFamily = Poppins

                    )
                    Text(
                        text       = item.label,
                        fontSize   = 12.sp,
                        color      = if (item.isActive) RCyan.copy(.85f) else RTextMuted,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Poppins

                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked         = item.isActive,
                        onCheckedChange = onToggle,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = Color.White,
                            checkedTrackColor    = RCyan,
                            uncheckedThumbColor  = Color.White.copy(.4f),
                            uncheckedTrackColor  = Color.White.copy(.08f),
                            uncheckedBorderColor = Color.White.copy(.12f)
                        )
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isActive) Color(0xFFEF5350).copy(.12f)
                                else Color.White.copy(.04f)
                            )
                            .clickable { showDeleteConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus",
                            tint = if (item.isActive) Color(0xFFEF5350).copy(.75f) else RTextMuted,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val dayLabels = when {
                    item.days.contains("EVERYDAY") -> listOf("EVERYDAY")
                    item.days.contains("WEEKENDS")  -> listOf("WEEKENDS")
                    else                            -> item.days
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
            title   = { Text("Hapus Reminder?", fontWeight = FontWeight.Bold,
                fontFamily = Poppins
            ) },
            text    = { Text("Reminder \"${item.label}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold,
                        fontFamily = Poppins
                    )
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
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) RCyan.copy(.14f) else Color.White.copy(.05f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = if (isActive) RCyan else RTextMuted,
            fontFamily = Poppins

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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(RBlue.copy(.18f))
                .clickable { onUp() },
            contentAlignment = Alignment.Center
        ) {
            Text("▲", fontSize = 14.sp, color = RCyan,
                fontFamily = Poppins
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text       = String.format("%02d", value),
            fontSize   = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = RTextPrimary,
            fontFamily = Poppins

        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(RBlue.copy(.18f))
                .clickable { onDown() },
            contentAlignment = Alignment.Center
        ) {
            Text("▼", fontSize = 14.sp, color = RCyan,
                fontFamily = Poppins
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = RTextSec, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
            fontFamily = Poppins
        )
    }
}

// ── Add Reminder Sheet ────────────────────────────────────────────────────────
@Composable
fun AddReminderSheet(
    onDismiss : () -> Unit,
    onSave    : (hour: Int, minute: Int, label: String, days: List<String>, vibration: Boolean) -> Unit
) {
    var displayHour    by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var isAm           by remember { mutableStateOf(true) }
    var reminderLabel  by remember { mutableStateOf("") }
    var vibrationOn    by remember { mutableStateOf(true) }

    val allDayLabels = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    val dayKeys      = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

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
            .padding(bottom = 36.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(RTextMuted.copy(.5f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(22.dp))

        // Header sheet
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
            Column {
                Text("Tambah Reminder", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = RTextPrimary,
                    fontFamily = Poppins
                )
                Text("Atur waktu pengingat minum air", fontSize = 12.sp, color = RTextSec,
                    fontFamily = Poppins
                )
            }
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(RBlue.copy(.14f)),
                Alignment.Center
            ) {
                BellIcon(Modifier.size(18.dp), RBlue)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Time Picker ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(RBgCard)
                .padding(vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.matchParentSize()) {
                drawCircle(RCyan.copy(.05f), size.width * .32f,
                    Offset(size.width * .88f, size.height * .3f))
            }
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
                Text(":", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold,
                    color = RTextPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp).offset(y = (-10).dp),
                    fontFamily = Poppins
                )
                NumberPicker(
                    value  = selectedMinute,
                    onUp   = { selectedMinute = if (selectedMinute == 59) 0 else selectedMinute + 1 },
                    onDown = { selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1 },
                    label  = "MENIT"
                )
                Spacer(Modifier.width(20.dp))
                // AM/PM
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            .background(if (isAm) RCyan else Color.White.copy(.08f))
                            .clickable { isAm = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AM", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                            color = if (isAm) RBgDeep else RTextSec,
                            fontFamily = Poppins
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                            .background(if (!isAm) RCyan else Color.White.copy(.08f))
                            .clickable { isAm = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PM", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                            color = if (!isAm) RBgDeep else RTextSec,
                            fontFamily = Poppins
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                ClockIconR(Modifier.size(12.dp), RTextMuted)
                Text("Geser untuk menyesuaikan waktu", fontSize = 11.sp, color = RTextSec,
                    fontFamily = Poppins
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Nama Pengingat ────────────────────────────────────────────────────
        Row(Modifier.padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(4.dp).clip(CircleShape).background(RCyan))
            Text("NAMA PENGINGAT", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                color = RTextSec, letterSpacing = 1.sp,
                fontFamily = Poppins
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = reminderLabel,
            onValueChange = { reminderLabel = it },
            placeholder   = { Text("Minum Pagi", color = RTextMuted,
                fontFamily = Poppins
            ) },
            shape         = RoundedCornerShape(16.dp),
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = RCyan,
                unfocusedBorderColor = RTextMuted.copy(.4f),
                focusedTextColor     = RTextPrimary,
                unfocusedTextColor   = RTextPrimary
            )
        )

        Spacer(Modifier.height(24.dp))

        // ── Hari Pengulangan ──────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(4.dp).clip(CircleShape).background(RCyan))
                Text("ULANGI SETIAP HARI", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold,
                    color = RTextSec, letterSpacing = 1.sp,
                    fontFamily = Poppins
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(RBlue.copy(.12f))
                    .clickable { selectedDays = if (selectedDays.size == 7) emptySet() else dayKeys.toSet() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Pilih Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RCyan,
                    fontFamily = Poppins
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), Arrangement.SpaceBetween) {
            allDayLabels.forEachIndexed { i, dayLabel ->
                val key        = dayKeys[i]
                val isSelected = key in selectedDays
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected)
                                Brush.linearGradient(listOf(RBlue, RCyan.copy(.8f)))
                            else
                                Brush.linearGradient(listOf(RBgCard, RBgCard))
                        )
                        .clickable { selectedDays = if (isSelected) selectedDays - key else selectedDays + key },
                    contentAlignment = Alignment.Center
                ) {
                    Text(dayLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else RTextSec,
                        fontFamily = Poppins
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Getaran ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (vibrationOn) RBlue.copy(.10f) else RBgCard)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (vibrationOn) RCyan.copy(.14f) else RTextMuted.copy(.12f)),
                contentAlignment = Alignment.Center
            ) {
                VibrateIcon(Modifier.size(22.dp), if (vibrationOn) RCyan else RTextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Getaran", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RTextPrimary,
                    fontFamily = Poppins
                )
                Text("Aktifkan getaran saat alarm berbunyi", fontSize = 12.sp, color = RTextSec,
                    fontFamily = Poppins
                )
            }
            Switch(
                checked         = vibrationOn,
                onCheckedChange = { vibrationOn = it },
                colors          = SwitchDefaults.colors(
                    checkedThumbColor    = Color.White,
                    checkedTrackColor    = RCyan,
                    uncheckedThumbColor  = RTextMuted.copy(.6f),
                    uncheckedTrackColor  = RTextMuted.copy(.18f),
                    uncheckedBorderColor = RTextMuted.copy(.18f)
                )
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Tombol Simpan ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    if (canSave)
                        Brush.horizontalGradient(listOf(RBlue, RTeal))
                    else
                        Brush.horizontalGradient(listOf(RBgCard, RBgCard))
                )
                .clickable(enabled = canSave) {
                    val label = reminderLabel.ifBlank { "Minum Air" }
                    onSave(hour24, selectedMinute, label, selectedDays.toList(), vibrationOn)
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BellIcon(Modifier.size(18.dp), if (canSave) Color.White else RTextMuted)
                Text(
                    "Simpan Reminder",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (canSave) Color.White else RTextMuted,
                    fontFamily = Poppins

                )
            }
        }
    }
}
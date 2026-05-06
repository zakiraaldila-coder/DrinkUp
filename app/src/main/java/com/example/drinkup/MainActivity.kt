package com.example.drinkup

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drinkup.ui.theme.DrinkUpTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch

// ── Palette senada SettingsScreen (dark navy + teal) ─────────────────────────
private val DrawerBg       = Color(0xFF09122A)
private val DrawerSurface  = Color(0xFF0F2040)
private val DrawerBorder   = Color(0xFF1B3560)
private val DrawerText     = Color(0xFFFFFFFF)
private val DrawerSubText  = Color(0xFF8AAAC8)
private val DrawerMuted    = Color(0xFF4A6A90)
private val DrawerActive   = Color(0xFF00D4AA)
private val DrawerCyan     = Color(0xFF00BFFF)
private val DrawerDivider  = Color(0xFF1B3560)
private val DrawerDanger   = Color(0xFFFF4D6A)

object Routes {
    const val SPLASH           = "splash"
    const val WELCOME          = "welcome"
    const val LOGIN            = "login"
    const val REGISTER         = "register"
    const val DASHBOARD        = "dashboard"
    const val STATISTIK        = "statistik"
    const val REMINDER         = "reminder"
    const val SETTINGS         = "settings"
    const val EDIT_PROFILE     = "edit_profile"
    const val WEEKLY_GOAL      = "weekly_goal"
    const val COMPLETE_PROFILE = "complete_profile"
    const val TAMBAH           = "tambah"
    const val ARTICLE          = "article"
    const val STREAK           = "streak"
}

data class NavItem(val route: String, val icon: ImageVector, val label: String)

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                android.widget.Toast.makeText(
                    this,
                    "Izin notifikasi diperlukan agar pengingat bisa tampil",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // ✅ FIX: Tidak delete/recreate channel saat app dibuka.
            // Delete channel lama hanya jika belum ada, supaya tidak restart service.
            if (manager.getNotificationChannel("alarm_channel") == null) {
                val channel = NotificationChannel(
                    "alarm_channel",
                    "Alarm Reminder",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description          = "Reminder minum air harian DrinkUp"
                    enableVibration(false)  // ✅ FIX: vibration dihandle AlarmService, bukan channel
                    setSound(null, null)    // ✅ FIX: HAPUS sound dari channel — MediaPlayer yang handle audio
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        createNotificationChannel()
        askNotificationPermission()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            DrinkUpTheme(darkTheme = themeViewModel.isDarkMode) {
                DrinkUpApp(authViewModel, themeViewModel)
            }
        }
    }
}

// ── App Composable ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkUpApp(
    authViewModel  : AuthViewModel,
    themeViewModel : ThemeViewModel
) {
    val navController    = rememberNavController()
    var currentRoute     by remember { mutableStateOf(Routes.DASHBOARD) }
    var showDrawer       by remember { mutableStateOf(false) }
    var showHamburger    by remember { mutableStateOf(false) }
    val intakeViewModel  : IntakeViewModel = viewModel()
    val drawerState      = rememberDrawerState(DrawerValue.Closed)
    val scope            = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            isLoggedIn = fa.currentUser != null
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }

    val navItems = listOf(
        NavItem(Routes.DASHBOARD, Icons.Rounded.Home,          "Home"),
        NavItem(Routes.STATISTIK, Icons.Rounded.BarChart,      "Statistik"),
        NavItem(Routes.REMINDER,  Icons.Rounded.Notifications, "Notifikasi"),
        NavItem(Routes.SETTINGS,  Icons.Rounded.Settings,      "Pengaturan"),
    )

    fun goToDashboard() {
        showHamburger = true
        currentRoute  = Routes.DASHBOARD
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) authViewModel.startListeningUser(uid)
        navController.navigate(Routes.DASHBOARD) { popUpTo(0) { inclusive = true } }
    }

    fun goToCompleteProfile() {
        showHamburger = false
        currentRoute  = Routes.COMPLETE_PROFILE
        navController.navigate(Routes.COMPLETE_PROFILE) { popUpTo(0) { inclusive = true } }
    }

    // Tutup drawer saat navigasi
    fun navigateTo(route: String) {
        scope.launch { drawerState.close() }
        currentRoute = route
        navController.navigate(route)
    }

    // ── ModalNavigationDrawer wrapping semua konten ───────────────────────────
    ModalNavigationDrawer(
        drawerState   = drawerState,
        gesturesEnabled = showHamburger,
        drawerContent = {
            DrinkUpDrawer(
                navItems     = navItems,
                currentRoute = currentRoute,
                authViewModel = authViewModel,
                onItemClick  = { route -> navigateTo(route) },
                onLogout     = {
                    scope.launch { drawerState.close() }
                    authViewModel.logout()
                    showHamburger = false
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onClose      = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        // ── Scaffold konten utama ─────────────────────────────────────────────
        Scaffold(
            containerColor = Color(0xFF09122A),
            topBar = {
                if (showHamburger) {
                    DrinkUpTopBar(
                        currentRoute = currentRoute,
                        onHamburgerClick = { scope.launch { drawerState.open() } }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = Routes.SPLASH,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Routes.SPLASH) {
                    showHamburger = false
                    SplashScreen {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            authViewModel.startListeningUser(user.uid)
                            showHamburger = true
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    }
                }

                composable(Routes.WELCOME) {
                    showHamburger = false
                    WelcomeScreen(
                        onLoginClick    = { navController.navigate(Routes.LOGIN) },
                        onRegisterClick = { navController.navigate(Routes.REGISTER) }
                    )
                }

                composable(Routes.LOGIN) {
                    showHamburger = false
                    LoginScreen(
                        authViewModel     = authViewModel,
                        initialTab        = "login",
                        onLoginSuccess    = { goToDashboard() },
                        onRegisterSuccess = { navController.navigate(Routes.LOGIN) },
                        onGoogleNewUser   = { goToCompleteProfile() },
                        onGoogleOldUser   = { goToDashboard() }
                    )
                }

                composable(Routes.REGISTER) {
                    showHamburger = false
                    LoginScreen(
                        authViewModel     = authViewModel,
                        initialTab        = "register",
                        onLoginSuccess    = { goToDashboard() },
                        onRegisterSuccess = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.REGISTER) { inclusive = true }
                            }
                        },
                        onGoogleNewUser   = { goToCompleteProfile() },
                        onGoogleOldUser   = { goToDashboard() }
                    )
                }

                composable(Routes.DASHBOARD) {
                    showHamburger = true
                    currentRoute  = Routes.DASHBOARD
                    DashboardScreen(
                        onShowTambah = {
                            showHamburger = false
                            currentRoute  = Routes.TAMBAH
                            navController.navigate(Routes.TAMBAH)
                        },
                        onNavigateToWeeklyGoal = {
                            showHamburger = false
                            currentRoute  = Routes.WEEKLY_GOAL
                            navController.navigate(Routes.WEEKLY_GOAL)
                        },
                        onNavigateToStreak = {
                            showHamburger = false
                            currentRoute  = Routes.STREAK
                            navController.navigate(Routes.STREAK)
                        },
                        intakeViewModel = intakeViewModel
                    )
                }

                composable(Routes.WEEKLY_GOAL) {
                    showHamburger = false
                    currentRoute  = Routes.WEEKLY_GOAL
                    WeeklyGoalScreen(
                        intakeViewModel = intakeViewModel,
                        onBack = {
                            showHamburger = true
                            currentRoute  = Routes.DASHBOARD
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.STREAK) {
                    showHamburger = false
                    currentRoute  = Routes.STREAK
                    StreakScreen(
                        intakeViewModel = intakeViewModel,
                        onBack = {
                            showHamburger = true
                            currentRoute  = Routes.DASHBOARD
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.STATISTIK) {
                    showHamburger = true
                    currentRoute  = Routes.STATISTIK
                    StatistikScreen(
                        intakeViewModel = intakeViewModel,
                        onReadMore      = {
                            showHamburger = false
                            currentRoute  = Routes.ARTICLE
                            navController.navigate(Routes.ARTICLE)
                        }
                    )
                }

                composable(Routes.REMINDER) {
                    showHamburger = true
                    currentRoute  = Routes.REMINDER
                    ReminderScreen()
                }

                composable(Routes.SETTINGS) {
                    showHamburger = true
                    currentRoute  = Routes.SETTINGS
                    SettingsScreen(
                        themeViewModel = themeViewModel,
                        onLogout = {
                            authViewModel.logout()
                            showHamburger = false
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToEditProfile = {
                            navController.navigate(Routes.EDIT_PROFILE)
                        }
                    )
                }

                composable(Routes.TAMBAH) {
                    showHamburger = false
                    currentRoute  = Routes.TAMBAH
                    TambahScreen(
                        onTambah = { amount ->
                            intakeViewModel.addIntake(amount)
                            showHamburger = true
                            currentRoute  = Routes.DASHBOARD
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        },
                        onBatalkan = {
                            showHamburger = true
                            currentRoute  = Routes.DASHBOARD
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.EDIT_PROFILE) {
                    showHamburger = false
                    currentRoute  = Routes.EDIT_PROFILE
                    EditProfileScreen(
                        onNavigateBack = {
                            showHamburger = true
                            currentRoute  = Routes.SETTINGS
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.ARTICLE) {
                    showHamburger = false
                    currentRoute  = Routes.ARTICLE
                    ArticleScreen(
                        onBack  = {
                            showHamburger = true
                            currentRoute  = Routes.STATISTIK
                            navController.popBackStack()
                        },
                        onDrink = {
                            showHamburger = false
                            currentRoute  = Routes.TAMBAH
                            navController.navigate(Routes.TAMBAH)
                        }
                    )
                }

                composable(Routes.COMPLETE_PROFILE) {
                    showHamburger = false
                    currentRoute  = Routes.COMPLETE_PROFILE
                    CompleteProfileScreen(
                        authViewModel     = authViewModel,
                        onProfileComplete = { goToDashboard() }
                    )
                }
            }
        }
    }
}

// ── Top Bar dengan Hamburger Button ───────────────────────────────────────────
@Composable
fun DrinkUpTopBar(
    currentRoute     : String,
    onHamburgerClick : () -> Unit
) {
    val title = when (currentRoute) {
        Routes.DASHBOARD -> ""
        Routes.STATISTIK -> "Statistik"
        Routes.REMINDER  -> "Reminder"
        Routes.SETTINGS  -> "Pengaturan"
        else             -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF08112A), Color(0xFF0C1835))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Hamburger button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F2040))
                .clickable { onHamburgerClick() }
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint     = Color(0xFF00D4AA),
                modifier = Modifier.size(22.dp)
            )
        }

        // Title dengan accent bar teal
        if (title.isNotEmpty()) {
            Row(
                modifier          = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF00D4AA))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Brand kanan
        Row(
            modifier          = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💧", fontSize = 13.sp)
            Spacer(Modifier.width(3.dp))
            Text(
                "DrinkUp",
                style = MaterialTheme.typography.labelMedium.copy(
                    color      = Color(0xFF00D4AA),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// ── Side Drawer ───────────────────────────────────────────────────────────────
@Composable
fun DrinkUpDrawer(
    navItems      : List<NavItem>,
    currentRoute  : String,
    authViewModel : AuthViewModel,
    onItemClick   : (String) -> Unit,
    onLogout      : () -> Unit,
    onClose       : () -> Unit
) {
    val userData by authViewModel.userData.collectAsState()
    val userName  = userData.namaLengkap.ifBlank { "DrinkUp User" }
    val userEmail = userData.email.ifBlank { "" }

    ModalDrawerSheet(
        modifier             = Modifier.width(300.dp),
        drawerShape          = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = DrawerBg,
        drawerTonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF08112A), Color(0xFF0A1535), Color(0xFF0D1A3E))
                    )
                )
        ) {

            // ── Header — gradient teal subtle ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                DrawerActive.copy(alpha = 0.18f),
                                DrawerCyan.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 32.dp)
            ) {
                // Tombol close
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DrawerSurface)
                        .border(1.dp, DrawerBorder, CircleShape)
                        .clickable { onClose() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close, null,
                        tint     = DrawerSubText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    // Avatar dengan teal ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(DrawerActive.copy(0.28f), Color(0xFF132550))
                                )
                            )
                            .border(2.dp, DrawerActive.copy(0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color      = DrawerActive,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color      = DrawerText,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    if (userEmail.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DrawerSubText
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Nav Items ─────────────────────────────────────────────────
            navItems.forEach { item ->
                val isActive = currentRoute == item.route
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isActive)
                                Brush.horizontalGradient(
                                    listOf(DrawerActive.copy(0.20f), DrawerCyan.copy(0.08f))
                                )
                            else
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .border(
                            1.dp,
                            if (isActive) DrawerActive.copy(0.35f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onItemClick(item.route) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Icon circle
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isActive) DrawerActive.copy(alpha = 0.18f)
                                    else DrawerSurface
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon, null,
                                tint     = if (isActive) DrawerActive else DrawerSubText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color      = if (isActive) DrawerText else DrawerSubText,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isActive) {
                            Icon(
                                Icons.Rounded.ChevronRight, null,
                                tint     = DrawerActive,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 20.dp),
                color     = DrawerDivider,
                thickness = 1.dp
            )
            Spacer(Modifier.height(8.dp))

            // ── Versi ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DrawerSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Info, null,
                        tint     = DrawerMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Versi",
                    style    = MaterialTheme.typography.bodyMedium.copy(color = DrawerSubText),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "5.0.0",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color      = DrawerActive,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 20.dp),
                color     = DrawerDivider,
                thickness = 1.dp
            )

            // ── Tombol Keluar — warna DrawerDanger senada SettingsScreen ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DrawerDanger.copy(alpha = 0.08f))
                    .border(1.dp, DrawerDanger.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clickable { onLogout() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DrawerDanger.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Logout, null,
                            tint     = DrawerDanger,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "Keluar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color      = DrawerDanger,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
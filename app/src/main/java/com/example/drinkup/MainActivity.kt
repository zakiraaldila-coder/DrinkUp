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

// ── Warna nav drawer (sesuai tema navy) ───────────────────────────────────────
private val DrawerBg       = Color(0xFF0D2B6B)
private val DrawerSurface  = Color(0xFF112870)
private val DrawerBorder   = Color(0xFF1E3FA0)
private val DrawerText     = Color(0xFFFFFFFF)
private val DrawerSubText  = Color(0xFFB0C4E8)
private val DrawerActive   = Color(0xFF4FC3F7)
private val DrawerDivider  = Color(0xFF1E3FA0)

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
            containerColor = Color(0xFF0A1F5C),
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
            .background(Color(0xFF0A1F5C))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF112870))
                .clickable { onHamburgerClick() }
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint     = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        if (title.isNotEmpty()) {
            Text(
                title,
                style    = MaterialTheme.typography.titleMedium.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            "💧 DrinkUp",
            style    = MaterialTheme.typography.labelMedium.copy(
                color      = Color(0xFF4FC3F7),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.align(Alignment.CenterEnd)
        )
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
        modifier      = Modifier.width(300.dp),
        drawerShape   = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = DrawerBg,
        drawerTonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A1F5C), Color(0xFF0D3B8E))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onClose() }
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }

                Column {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF3A85E0), Color(0xFF1565C0))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color      = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color      = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    if (userEmail.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFB0C4E8)
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            navItems.forEach { item ->
                val isActive = currentRoute == item.route
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isActive) Color(0xFF1E3FA0) else Color.Transparent
                        )
                        .clickable { onItemClick(item.route) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (isActive) Color(0xFF4FC3F7).copy(alpha = 0.2f)
                                    else Color.White.copy(alpha = 0.07f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon, null,
                                tint     = if (isActive) Color(0xFF4FC3F7) else Color(0xFFB0C4E8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            item.label,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color      = if (isActive) Color.White else Color(0xFFB0C4E8),
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (isActive) {
                            Icon(
                                Icons.Rounded.ChevronRight, null,
                                tint     = Color(0xFF4FC3F7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 24.dp),
                color     = DrawerDivider,
                thickness = 1.dp
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Info, null,
                    tint     = DrawerSubText.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Versi",
                    style    = MaterialTheme.typography.bodyMedium.copy(color = DrawerSubText),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "1.0.0",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color      = DrawerSubText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 24.dp),
                color     = DrawerDivider,
                thickness = 1.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFF6D00).copy(alpha = 0.12f))
                    .clickable { onLogout() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFF6D00).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Logout, null,
                            tint     = Color(0xFFFF6D00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "Keluar",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color      = Color(0xFFFF6D00),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
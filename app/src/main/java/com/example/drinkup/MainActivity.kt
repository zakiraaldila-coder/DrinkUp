package com.example.drinkup

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
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

private val MintPrimary = Color(0xFF4ECDC4)
private val TextGray    = Color(0xFF9E9E9E)

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

    // ── Runtime permission launcher ────────────────────────────────────────
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

    // ── Buat Notification Channel ──────────────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // ✅ Hapus channel lama yang pakai sound default
            manager.deleteNotificationChannel("drink_channel")

            // Kalau channel baru sudah ada, skip
            if (manager.getNotificationChannel(ReminderReceiver.CHANNEL_ID) != null) return

            val soundUri: Uri = try {
                Uri.parse("android.resource://${packageName}/${R.raw.drink_reminder}")
            } catch (e: Exception) {
                android.media.RingtoneManager.getDefaultUri(
                    android.media.RingtoneManager.TYPE_NOTIFICATION
                )
            }

            val audioAttr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                ReminderReceiver.CHANNEL_ID,   // ✅ pakai konstanta dari ReminderReceiver
                "Drink Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description          = "Reminder minum air harian DrinkUp"
                enableVibration(true)
                vibrationPattern     = longArrayOf(0, 400, 200, 400)
                setSound(soundUri, audioAttr)  // ✅ sound custom
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            manager.createNotificationChannel(channel)
        }
    }

    // ── Minta permission notifikasi (Android 13+) ──────────────────────────
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        createNotificationChannel()   // ✅ buat channel dengan sound custom
        askNotificationPermission()   // ✅ minta izin notifikasi ke user

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            DrinkUpTheme(darkTheme = themeViewModel.isDarkMode) {
                DrinkUpApp(authViewModel, themeViewModel)
            }
        }
    }
}

// ── App Composable ─────────────────────────────────────────────────────────────
@Composable
fun DrinkUpApp(
    authViewModel  : AuthViewModel,
    themeViewModel : ThemeViewModel
) {
    val navController   = rememberNavController()
    var currentRoute    by remember { mutableStateOf(Routes.DASHBOARD) }
    var showNavBar      by remember { mutableStateOf(false) }
    val intakeViewModel : IntakeViewModel = viewModel()

    var splashDone  by remember { mutableStateOf(false) }
    var isLoggedIn  by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

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
        NavItem(Routes.REMINDER,  Icons.Rounded.Notifications, "Reminder"),
        NavItem(Routes.SETTINGS,  Icons.Rounded.Person,        "Settings"),
    )

    fun goToDashboard() {
        showNavBar   = true
        currentRoute = Routes.DASHBOARD
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) authViewModel.startListeningUser(uid)
        navController.navigate(Routes.DASHBOARD) { popUpTo(0) { inclusive = true } }
    }

    fun goToCompleteProfile() {
        showNavBar   = false
        currentRoute = Routes.COMPLETE_PROFILE
        navController.navigate(Routes.COMPLETE_PROFILE) { popUpTo(0) { inclusive = true } }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showNavBar) {
                DrinkUpNavBar(
                    items        = navItems,
                    currentRoute = currentRoute,
                    onItemClick  = { route -> currentRoute = route; navController.navigate(route) }
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
                showNavBar = false
                SplashScreen {
                    splashDone = true
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        authViewModel.startListeningUser(user.uid)
                        showNavBar = true
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
                showNavBar = false
                WelcomeScreen(
                    onLoginClick    = { navController.navigate(Routes.LOGIN) },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.LOGIN) {
                showNavBar = false
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
                showNavBar = false
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
                showNavBar   = true
                currentRoute = Routes.DASHBOARD
                DashboardScreen(
                    onShowTambah = {
                        showNavBar   = false
                        currentRoute = Routes.TAMBAH
                        navController.navigate(Routes.TAMBAH)
                    },
                    onNavigateToWeeklyGoal = {
                        showNavBar   = false
                        currentRoute = Routes.WEEKLY_GOAL
                        navController.navigate(Routes.WEEKLY_GOAL)
                    },
                    intakeViewModel = intakeViewModel
                )
            }

            composable(Routes.WEEKLY_GOAL) {
                showNavBar   = false
                currentRoute = Routes.WEEKLY_GOAL
                WeeklyGoalScreen(
                    intakeViewModel = intakeViewModel,
                    onBack = {
                        showNavBar   = true
                        currentRoute = Routes.DASHBOARD
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.STATISTIK) {
                showNavBar   = true
                currentRoute = Routes.STATISTIK
                StatistikScreen(
                    intakeViewModel = intakeViewModel,
                    onReadMore      = {
                        showNavBar   = false
                        currentRoute = Routes.ARTICLE
                        navController.navigate(Routes.ARTICLE)
                    }
                )
            }

            composable(Routes.REMINDER) {
                showNavBar   = true
                currentRoute = Routes.REMINDER
                ReminderScreen()
            }

            composable(Routes.SETTINGS) {
                showNavBar   = true
                currentRoute = Routes.SETTINGS
                SettingsScreen(
                    themeViewModel = themeViewModel,
                    onLogout = {
                        authViewModel.logout()
                        showNavBar = false
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
                showNavBar   = false
                currentRoute = Routes.TAMBAH
                TambahScreen(
                    onTambah = { amount ->
                        intakeViewModel.addIntake(amount)
                        showNavBar   = true
                        currentRoute = Routes.DASHBOARD
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    onBatalkan = {
                        showNavBar   = true
                        currentRoute = Routes.DASHBOARD
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                showNavBar   = false
                currentRoute = Routes.EDIT_PROFILE
                EditProfileScreen(
                    onNavigateBack = {
                        showNavBar   = true
                        currentRoute = Routes.SETTINGS
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.ARTICLE) {
                showNavBar   = false
                currentRoute = Routes.ARTICLE
                ArticleScreen(
                    onBack  = {
                        showNavBar   = true
                        currentRoute = Routes.STATISTIK
                        navController.popBackStack()
                    },
                    onDrink = {
                        showNavBar   = true
                        currentRoute = Routes.TAMBAH
                        navController.navigate(Routes.TAMBAH)
                    }
                )
            }

            composable(Routes.COMPLETE_PROFILE) {
                showNavBar   = false
                currentRoute = Routes.COMPLETE_PROFILE
                CompleteProfileScreen(
                    authViewModel     = authViewModel,
                    onProfileComplete = { goToDashboard() }
                )
            }
        }
    }
}

// ── Bottom Navigation Bar ──────────────────────────────────────────────────────
@Composable
fun DrinkUpNavBar(
    items        : List<NavItem>,
    currentRoute : String,
    onItemClick  : (String) -> Unit
) {
    val navBg = MaterialTheme.colorScheme.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(navBg)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    NavBarItem(item, currentRoute == item.route) { onItemClick(item.route) }
                }
            }
        }
    }
}

@Composable
fun NavBarItem(item: NavItem, isActive: Boolean, onClick: () -> Unit) {
    val animOffset by animateFloatAsState(if (isActive) -18f else 0f, label = "off")
    val animScale  by animateFloatAsState(if (isActive) 1f else 0.85f,  label = "sc")
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .graphicsLayer { translationY = animOffset }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { scaleX = animScale; scaleY = animScale }
                .background(if (isActive) MintPrimary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = if (isActive) Color.White else inactiveColor,
                modifier           = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            item.label,
            fontSize   = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color      = if (isActive) MintPrimary else inactiveColor
        )
    }
}
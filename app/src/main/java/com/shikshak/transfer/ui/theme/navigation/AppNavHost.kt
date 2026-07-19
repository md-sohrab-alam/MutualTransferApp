package com.shikshak.transfer.ui.theme.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.shikshak.transfer.data.Prefs
import com.shikshak.transfer.di.AppEntryPoint
import com.shikshak.transfer.ui.theme.about.AboutPrivacyScreen
import com.shikshak.transfer.ui.theme.about.PrivacyPolicyScreen
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.disclaimer.DisclaimerDialog
import com.shikshak.transfer.ui.theme.home.HomeScreen
import com.shikshak.transfer.ui.theme.language.LanguageSelectorScreen
import com.shikshak.transfer.ui.theme.login.OtpVerificationScreen
import com.shikshak.transfer.ui.theme.login.PhoneAuthViewModel
import com.shikshak.transfer.ui.theme.login.PhoneNumberInputScreen
import com.shikshak.transfer.ui.theme.more.MoreScreen
import com.shikshak.transfer.ui.theme.profile.TeacherProfileScreen
import com.shikshak.transfer.ui.theme.request.EditRequestScreen
import com.shikshak.transfer.ui.theme.request.RequestScreen
import com.shikshak.transfer.ui.theme.splash.SplashScreen
import com.shikshak.transfer.ui.theme.updates.NotificationDetailScreen
import com.shikshak.transfer.ui.theme.updates.UpdatesScreen
import com.shikshak.transfer.ui.theme.updates.UpdatesViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import timber.log.Timber

private val MainTabRoutes = setOf(
    Routes.Home,
    Routes.Profile,
    Routes.Request,
    Routes.Updates,
    Routes.More
)

@Composable
fun AppNavHost(
    sharedViewModel: SharedViewModel = hiltViewModel(),
    initialNotificationType: String? = null,
    initialNotificationAction: String? = null
) {
    val context = LocalContext.current
    val prefs: Prefs = EntryPointAccessors.fromApplication(
        context.applicationContext,
        AppEntryPoint::class.java
    ).prefs()
    val navController = rememberNavController()
    val activity = LocalActivity.current

    var appReady by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.Splash) }

    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var disclaimerAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        disclaimerAccepted = prefs.isDisclaimerAccepted()
        if (!disclaimerAccepted) {
            showDisclaimerDialog = true
        }
    }

    val handleDisclaimerAccept: () -> Unit = {
        disclaimerAccepted = true
        showDisclaimerDialog = false
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            prefs.setDisclaimerAccepted(true)
        }
    }

    val handleDisclaimerExit: () -> Unit = {
        activity?.finish()
    }

    fun hasLanguageBeenSelected(): Boolean {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPrefs.contains("language_code")
    }

    fun navigateToMain(destination: String) {
        navController.navigate(destination) {
            // Clear language/splash/auth stack so Back won't return to login/OTP
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun handlePostLoginNavigation() {
        sharedViewModel.loadCurrentTeacher(
            onLoaded = {
                val destination = if (sharedViewModel.isProfileComplete()) {
                    Routes.Home
                } else {
                    Routes.Profile
                }
                Timber.d("Post-login navigation -> $destination (profileComplete=${sharedViewModel.isProfileComplete()})")
                navigateToMain(destination)
            },
            onError = { error ->
                Timber.e("Post-login profile load failed: $error")
                navigateToMain(Routes.Profile)
            }
        )
    }

    // Cold start: decide where authenticated users should land
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            sharedViewModel.loadCurrentTeacher(
                onLoaded = {
                    startDestination = if (sharedViewModel.isProfileComplete()) {
                        Routes.Home
                    } else {
                        Routes.Profile
                    }
                    appReady = true
                },
                onError = {
                    startDestination = Routes.Splash
                    appReady = true
                }
            )
        } else {
            startDestination = Routes.Splash
            appReady = true
        }
    }

    LaunchedEffect(initialNotificationType, initialNotificationAction, appReady) {
        if (!appReady || initialNotificationType == null) return@LaunchedEffect
        if (!sharedViewModel.isProfileComplete()) return@LaunchedEffect

        when (initialNotificationAction) {
            "navigate_to_home" -> navigateToMain(Routes.Home)
            "navigate_to_request" -> navigateToMain(Routes.Request)
            "navigate_to_updates" -> navigateToMain(Routes.Updates)
            else -> navigateToMain(Routes.Updates)
        }
    }

    if (!appReady) {
        SplashScreen(onSplashComplete = {})
        return
    }

    val actualStartDestination = if (hasLanguageBeenSelected()) {
        startDestination
    } else {
        Routes.LanguageSelector
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profileComplete = sharedViewModel.isProfileComplete()
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val showBottomBar = isLoggedIn && profileComplete && currentRoute in MainTabRoutes

    // First-time profile setup: back should leave the app, not bounce through auth screens
    val isForcedProfileSetup = isLoggedIn && !profileComplete && currentRoute == Routes.Profile
    BackHandler(enabled = isForcedProfileSetup) {
        activity?.finish()
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = actualStartDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.LanguageSelector) {
                LanguageSelectorScreen(
                    onLanguageSelected = {
                        val next = if (FirebaseAuth.getInstance().currentUser != null) {
                            if (sharedViewModel.isProfileComplete()) Routes.Home else Routes.Profile
                        } else {
                            Routes.Splash
                        }
                        navController.navigate(next) {
                            popUpTo(Routes.LanguageSelector) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Splash) {
                SplashScreen(
                    onSplashComplete = {
                        val user = FirebaseAuth.getInstance().currentUser
                        when {
                            user == null -> {
                                navController.navigate("auth_flow") {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                }
                            }
                            sharedViewModel.isProfileComplete() -> {
                                navigateToMain(Routes.Home)
                            }
                            else -> {
                                // Teacher may not be loaded yet after logout/login restart
                                sharedViewModel.loadCurrentTeacher(
                                    onLoaded = {
                                        navigateToMain(
                                            if (sharedViewModel.isProfileComplete()) Routes.Home
                                            else Routes.Profile
                                        )
                                    },
                                    onError = { navigateToMain(Routes.Profile) }
                                )
                            }
                        }
                    }
                )
            }

            navigation(startDestination = Routes.PhoneInput, route = "auth_flow") {
                composable(Routes.PhoneInput) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry("auth_flow")
                    }
                    val viewModel = hiltViewModel<PhoneAuthViewModel>(parentEntry)
                    val hostActivity = activity
                    if (hostActivity != null) {
                        PhoneNumberInputScreen(
                            viewModel = viewModel,
                            activity = hostActivity,
                            navController = navController,
                            onLoginSuccess = { handlePostLoginNavigation() }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                composable(Routes.OtpVerification) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry("auth_flow")
                    }
                    val viewModel = hiltViewModel<PhoneAuthViewModel>(parentEntry)
                    OtpVerificationScreen(
                        viewModel = viewModel,
                        navController = navController,
                        onLoginSuccess = { handlePostLoginNavigation() }
                    )
                }
            }

            composable(Routes.Home) {
                HomeScreen(navController)
            }

            composable(Routes.Profile) {
                TeacherProfileScreen(
                    onProfileSaved = { teacher ->
                        sharedViewModel.setTeacher(teacher)
                        // Enter main app with bottom bar; clear auth/setup stack
                        navController.navigate(Routes.Home) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToTeacherInput = {},
                    isFirstLogin = !sharedViewModel.isProfileComplete()
                )
            }

            composable(Routes.Request) {
                RequestScreen(
                    onNavigateToCreateRequest = {
                        navController.navigate(Routes.CreateRequest)
                    },
                    onNavigateToEditRequest = {
                        navController.navigate(Routes.EditRequest)
                    }
                )
            }

            composable(Routes.Updates) {
                UpdatesScreen(
                    onNotificationDetailClick = { notification ->
                        navController.navigate("${Routes.NotificationDetail}/${notification.id}")
                    }
                )
            }

            composable(route = "${Routes.NotificationDetail}/{notificationId}") { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
                val viewModel = hiltViewModel<UpdatesViewModel>()
                val notifications by viewModel.notifications.collectAsState()
                val notification = notifications.find { it.id == notificationId }

                if (notification != null) {
                    NotificationDetailScreen(
                        notification = notification,
                        onBackClick = { navController.popBackStack() },
                        onMarkAsRead = { id -> viewModel.markAsRead(id) }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            composable(Routes.More) {
                MoreScreen(
                    onNavigateToLanguageSelector = {
                        navController.navigate(Routes.LanguageSelector)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.Splash) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToAboutPrivacy = {
                        navController.navigate(Routes.AboutPrivacy)
                    }
                )
            }

            composable(Routes.AboutPrivacy) {
                AboutPrivacyScreen(
                    onBackClick = { navController.popBackStack() },
                    onOpenPrivacyPolicy = {
                        navController.navigate(Routes.PrivacyPolicy)
                    }
                )
            }

            composable(Routes.PrivacyPolicy) {
                PrivacyPolicyScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.CreateRequest) {
                val currentTeacher = sharedViewModel.currentTeacher
                if (currentTeacher != null) {
                    val teacher = currentTeacher
                    EditRequestScreen(
                        transferRequest = TransferRequest(
                            teacherId = teacher.uid,
                            teacherName = teacher.name,
                            currentDistrict = teacher.district,
                            currentSchool = teacher.schoolName,
                            preferredDistricts = emptyList(),
                            preferredBlocks = emptyList(),
                            post = teacher.post,
                            designation = teacher.designation,
                            subject = teacher.subject,
                            status = "PENDING",
                            submittedDate = java.time.LocalDate.now().toString(),
                            contactPreference = teacher.contactPreference,
                            notes = ""
                        ),
                        onRequestUpdated = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            composable(Routes.EditRequest) {
                val transferRequest = TransferRequest(
                    teacherId = "",
                    teacherName = "",
                    currentDistrict = "",
                    currentSchool = "",
                    preferredDistricts = emptyList(),
                    preferredBlocks = emptyList(),
                    post = "Secondary",
                    designation = "",
                    subject = "",
                    status = "PENDING",
                    submittedDate = java.time.LocalDate.now().toString(),
                    contactPreference = true,
                    notes = ""
                )
                EditRequestScreen(
                    transferRequest = transferRequest,
                    onRequestUpdated = {
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(route = "${Routes.ProfileDetail}/{teacherId}") { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
                if (teacherId.isNotEmpty()) {
                    com.shikshak.transfer.ui.theme.profile.ProfileDetailScreen(
                        teacherId = teacherId,
                        navController = navController
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (showDisclaimerDialog) {
        DisclaimerDialog(
            show = true,
            onAccept = handleDisclaimerAccept,
            onExit = handleDisclaimerExit
        )
    }
}

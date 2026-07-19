package com.shikshak.transfer.ui.theme.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import dagger.hilt.android.EntryPointAccessors
import com.shikshak.transfer.di.AppEntryPoint
import com.shikshak.transfer.ui.theme.home.HomeScreen
import com.shikshak.transfer.ui.theme.login.OtpVerificationScreen
import com.shikshak.transfer.ui.theme.login.PhoneAuthViewModel
import com.shikshak.transfer.ui.theme.login.PhoneNumberInputScreen
import com.shikshak.transfer.ui.theme.profile.TeacherProfileScreen
import com.shikshak.transfer.ui.theme.request.RequestScreen
import com.shikshak.transfer.ui.theme.request.RequestViewModel
import com.shikshak.transfer.ui.theme.request.EditRequestScreen
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.navigation.SharedViewModel
import com.shikshak.transfer.ui.theme.splash.SplashScreen
import com.shikshak.transfer.ui.theme.language.LanguageSelectorScreen
import com.shikshak.transfer.ui.theme.more.MoreScreen
import com.shikshak.transfer.ui.theme.updates.UpdatesScreen
import com.shikshak.transfer.ui.theme.updates.NotificationDetailScreen
import com.shikshak.transfer.ui.theme.updates.UpdatesViewModel
import com.shikshak.transfer.ui.theme.about.AboutPrivacyScreen
import com.shikshak.transfer.ui.theme.about.PrivacyPolicyScreen
import com.shikshak.transfer.ui.theme.disclaimer.DisclaimerDialog
import com.shikshak.transfer.data.Prefs
import android.content.Context
import timber.log.Timber

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

    // 👇 States to track loading teacher and decide destination
    var appReady by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.Splash) }
    var showBottomNavigation by remember { mutableStateOf(false) }
    
    // Disclaimer state
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var disclaimerAccepted by remember { mutableStateOf(false) }
    
    // Check disclaimer acceptance
    LaunchedEffect(Unit) {
        disclaimerAccepted = prefs.isDisclaimerAccepted()
        if (!disclaimerAccepted) {
            showDisclaimerDialog = true
        }
    }
    
    // Handle disclaimer acceptance
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
    
    // Handle notification navigation
    LaunchedEffect(initialNotificationType, initialNotificationAction) {
        if (initialNotificationType != null && sharedViewModel.isProfileComplete()) {
            Timber.d("=== NOTIFICATION NAVIGATION ===")
            Timber.d("Notification type: $initialNotificationType")
            Timber.d("Notification action: $initialNotificationAction")
            Timber.d("Profile complete: ${sharedViewModel.isProfileComplete()}")
            
            // Navigate based on notification action
            when (initialNotificationAction) {
                "navigate_to_home" -> {
                    Timber.d("Navigating to Home")
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = false }
                    }
                }
                "navigate_to_request" -> {
                    Timber.d("Navigating to Request")
                    navController.navigate(Routes.Request) {
                        popUpTo(Routes.Home) { inclusive = false }
                    }
                }
                "navigate_to_updates" -> {
                    Timber.d("Navigating to Updates")
                    navController.navigate(Routes.Updates) {
                        popUpTo(Routes.Home) { inclusive = false }
                    }
                }
                else -> {
                    // Default to Updates tab
                    Timber.d("Default navigation to Updates")
                    navController.navigate(Routes.Updates) {
                        popUpTo(Routes.Home) { inclusive = false }
                    }
                }
            }
        } else {
            Timber.d("Notification navigation skipped - type: $initialNotificationType, profile complete: ${sharedViewModel.isProfileComplete()}")
        }
    }

    // ✅ Check if language has been selected
    fun hasLanguageBeenSelected(): Boolean {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return sharedPrefs.contains("language_code")
    }

    // ✅ Auto-login check: run once
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            sharedViewModel.loadCurrentTeacher(onLoaded = {
                startDestination = if (sharedViewModel.isProfileComplete()) {
                    Routes.Home
                } else {
                    Routes.Profile
                }
                appReady = true
            }, onError = {
                startDestination = Routes.Splash
                appReady = true
            })
        } else {
            startDestination = Routes.Splash
            appReady = true
        }
    }

    // ⏳ Wait for the state to be determined
    if (!appReady) {
        // Show splash screen while determining start destination
        SplashScreen(
            onSplashComplete = {
                // This will be handled by the LaunchedEffect above
                // The splash screen will show until appReady becomes true
            }
        )
        return
    }

    // Determine the actual start destination based on language selection
    val actualStartDestination = if (hasLanguageBeenSelected()) {
        startDestination
    } else {
        Routes.LanguageSelector
    }

    // Show bottom navigation for authenticated users with complete profiles
    val shouldShowBottomNavigation = sharedViewModel.isProfileComplete() && 
        FirebaseAuth.getInstance().currentUser != null

    if (shouldShowBottomNavigation) {
        // Main app with bottom navigation
        Scaffold(
            bottomBar = { BottomNavigation(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = actualStartDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Routes.Home) {
                    HomeScreen(navController)
                }
                
                composable(Routes.Profile) {
                    TeacherProfileScreen(
                        onProfileSaved = { teacher ->
                            sharedViewModel.setTeacher(teacher)
                            // Navigate to Splash to restart the app flow with bottom navigation
                            navController.navigate(Routes.Splash) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToTeacherInput = {
                            // This will be handled by the ProfileViewModel for deleting transfer request
                        },
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
                
                composable(
                    route = "${Routes.NotificationDetail}/{notificationId}"
                ) { backStackEntry ->
                    val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
                    val viewModel = hiltViewModel<UpdatesViewModel>()
                    val notifications by viewModel.notifications.collectAsState()
                    
                    val notification = notifications.find { it.id == notificationId }
                    
                    if (notification != null) {
                        NotificationDetailScreen(
                            notification = notification,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onMarkAsRead = { id ->
                                viewModel.markAsRead(id)
                            }
                        )
                    } else {
                        // Show loading or error state
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
                            // Navigate to Splash to restart the app flow properly
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
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onOpenPrivacyPolicy = {
                            navController.navigate(Routes.PrivacyPolicy)
                        }
                    )
                }
                
                composable(Routes.PrivacyPolicy) {
                    PrivacyPolicyScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Routes.CreateRequest) {
                    val viewModel = hiltViewModel<RequestViewModel>()
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
                            onRequestUpdated = { updatedRequest ->
                                // Navigate to Home screen after successful update to see matches
                                navController.navigate(Routes.Home) {
                                    // Clear the entire back stack and start fresh from Home
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    } else {
                        // Show loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                composable(Routes.EditRequest) {
                    // Create a TransferRequest with empty data - EditRequestScreen will load real data
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
                        onRequestUpdated = { updatedRequest ->
                            // Navigate to Home screen after successful update to see matches
                            navController.navigate(Routes.Home) {
                                // Clear the entire back stack and start fresh from Home
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Routes.LanguageSelector) {
                    LanguageSelectorScreen(
                        onLanguageSelected = {
                            navController.navigate(Routes.More) {
                                popUpTo(Routes.LanguageSelector) { inclusive = true }
                            }
                        }
                    )
                }
                
                // Profile Detail Route
                composable(
                    route = "${Routes.ProfileDetail}/{teacherId}"
                ) { backStackEntry ->
                    val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
                    if (teacherId.isNotEmpty()) {
                        com.shikshak.transfer.ui.theme.profile.ProfileDetailScreen(
                            teacherId = teacherId,
                            navController = navController
                        )
                    } else {
                        // Show error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                // Auth routes should not be in the main navigation with bottom bar
                // They are handled in the else block below

                // Splash route for logout functionality
                composable(Routes.Splash) {
                    SplashScreen(
                        onSplashComplete = {
                            // In main navigation, we only handle authenticated users
                            // Unauthenticated users should be handled by the auth flow
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                if (sharedViewModel.isProfileComplete()) {
                                    navController.navigate(Routes.Home) {
                                        popUpTo(Routes.Splash) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Routes.Profile) {
                                        popUpTo(Routes.Splash) { inclusive = true }
                                    }
                                }
                            } else {
                                // If somehow we get here without a user, just go to Home
                                // The auth flow should handle unauthenticated users
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    } else {
        // Auth flow without bottom navigation
        NavHost(navController = navController, startDestination = actualStartDestination) {
            composable(Routes.LanguageSelector) {
                LanguageSelectorScreen(
                    onLanguageSelected = {
                        navController.navigate(Routes.Splash) {
                            popUpTo(Routes.LanguageSelector) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Routes.Splash) {
                SplashScreen(
                    onSplashComplete = {
                        // Navigate to the appropriate screen based on authentication state
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            if (sharedViewModel.isProfileComplete()) {
                                navController.navigate(Routes.Home) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Routes.Profile) {
                                    popUpTo(Routes.Splash) { inclusive = true }
                                }
                            }
                        } else {
                            // Navigate to PhoneInput for authentication
                            navController.navigate("auth_flow") {
                                popUpTo(Routes.Splash) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Create a navigation graph for auth flow to share ViewModel
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

                composable(Routes.OtpVerification) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry("auth_flow")
                    }
                    val viewModel = hiltViewModel<PhoneAuthViewModel>(parentEntry)
                    OtpVerificationScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
            
            composable(Routes.Profile) {
                TeacherProfileScreen(
                    onProfileSaved = { teacher ->
                        sharedViewModel.setTeacher(teacher)
                        // Navigate to Splash to restart the app flow with bottom navigation
                        navController.navigate(Routes.Splash) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToTeacherInput = {
                        // This will be handled by the ProfileViewModel for deleting transfer request
                    },
                    isFirstLogin = !sharedViewModel.isProfileComplete()
                )
            }
            
            // Home route for auth flow
            composable(Routes.Home) {
                HomeScreen(navController)
            }
        }
    }
    
    // Show disclaimer dialog if needed
    if (showDisclaimerDialog) {
        DisclaimerDialog(
            show = true,
            onAccept = handleDisclaimerAccept,
            onExit = handleDisclaimerExit
        )
    }
}

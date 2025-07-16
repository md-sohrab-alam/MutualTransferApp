package com.shikshak.transfer.ui.theme.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import com.shikshak.transfer.ui.theme.filter.TeacherFilterListScreen
import com.shikshak.transfer.ui.theme.home.HomeScreen
import com.shikshak.transfer.ui.theme.login.LoginScreen
import com.shikshak.transfer.ui.theme.login.OtpVerificationScreen
import com.shikshak.transfer.ui.theme.login.PhoneAuthViewModel
import com.shikshak.transfer.ui.theme.login.PhoneNumberInputScreen
import com.shikshak.transfer.ui.theme.matchprofile.MatchListScreen
import com.shikshak.transfer.ui.theme.profile.TeacherProfileScreen
import com.shikshak.transfer.ui.theme.profile.TeacherInputScreen
import com.shikshak.transfer.ui.theme.request.RequestScreen
import com.shikshak.transfer.ui.theme.request.CreateRequestScreen
import com.shikshak.transfer.ui.theme.request.EditRequestScreen
import com.shikshak.transfer.ui.theme.request.RequestViewModel
import com.shikshak.transfer.ui.theme.register.RegisterScreen
import com.shikshak.transfer.ui.theme.splash.SplashScreen
import com.shikshak.transfer.ui.theme.language.LanguageSelectorScreen
import android.content.Context

@Composable
fun AppNavHost(
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activity = LocalActivity.current!!
    val context = LocalContext.current

    // 👇 States to track loading teacher and decide destination
    var appReady by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.Splash) }
    var showBottomNavigation by remember { mutableStateOf(false) }

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
                showBottomNavigation = sharedViewModel.isProfileComplete()
                appReady = true
            }, onError = {
                startDestination = Routes.Auth
                appReady = true
            })
        } else {
            startDestination = Routes.Auth
            appReady = true
        }
    }

    // ⏳ Wait for the state to be determined
    if (!appReady) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Determine the actual start destination based on language selection
    val actualStartDestination = if (hasLanguageBeenSelected()) {
        startDestination
    } else {
        Routes.LanguageSelector
    }

    if (showBottomNavigation) {
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
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Profile) { inclusive = true }
                            }
                        },
                        onNavigateToTeacherInput = {
                            navController.navigate(Routes.TeacherInput)
                        }
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
                    // TODO: Implement Updates screen
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                composable(Routes.More) {
                    // TODO: Implement More screen
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                
                composable(Routes.TeacherInput) {
                    TeacherInputScreen(
                        onFormSubmitted = { teacher ->
                            sharedViewModel.setTeacher(teacher)
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.TeacherInput) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Routes.MatchList) {
                    sharedViewModel.currentTeacher?.let { teacher ->
                        MatchListScreen(currentTeacher = teacher,
                            onGoToTeacherList = { navController.navigate(Routes.TeacherList) })
                    } ?: CircularProgressIndicator()
                }
                
                composable(Routes.TeacherList) {
                    TeacherFilterListScreen()
                }
                
                composable(Routes.CreateRequest) {
                    CreateRequestScreen(
                        onRequestSubmitted = { transferRequest ->
                            navController.navigate(Routes.Request) {
                                popUpTo(Routes.CreateRequest) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Routes.EditRequest) {
                    // For now, navigate back to Request screen
                    // The EditRequest functionality will be handled within the RequestScreen
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.Request) {
                            popUpTo(Routes.EditRequest) { inclusive = true }
                        }
                    }
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
                            navController.navigate(Routes.Auth) {
                                popUpTo(Routes.Splash) { inclusive = true }
                            }
                        }
                    }
                )
            }

            navigation(startDestination = Routes.PhoneInput, Routes.Auth) {
                composable(Routes.PhoneInput) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry(Routes.Auth)
                    }
                    val viewModel = hiltViewModel<PhoneAuthViewModel>(parentEntry)
                    PhoneNumberInputScreen(
                        viewModel = viewModel, activity = activity, navController = navController
                    )
                }
                composable(Routes.OtpVerification) {
                    val parentEntry = remember(it) {
                        navController.getBackStackEntry(Routes.Auth)
                    }
                    val viewModel = hiltViewModel<PhoneAuthViewModel>(parentEntry)
                    OtpVerificationScreen(viewModel = viewModel, navController = navController)
                }
            }
            
            composable(Routes.Login) {
                LoginScreen(navController, onLoginSuccess = {
                    sharedViewModel.loadCurrentTeacher(onLoaded = {
                        if (sharedViewModel.isProfileComplete()) {
                            navController.navigate(Routes.Home)
                        } else {
                            navController.navigate(Routes.Profile)
                        }
                    }, onError = { navController.navigate(Routes.Profile) })
                })
            }
            
            composable(Routes.Register) {
                RegisterScreen(onRegisterSuccess = {
                    navController.navigate(Routes.Login)
                }, onNavigateToLogin = { navController.navigate(Routes.Login) })
            }

            composable(Routes.Profile) {
                TeacherProfileScreen(
                    onProfileSaved = { teacher ->
                        sharedViewModel.setTeacher(teacher)
                        navController.navigate(Routes.Home)
                    },
                    onNavigateToTeacherInput = {
                        navController.navigate(Routes.TeacherInput)
                    }
                )
            }

            composable(Routes.TeacherInput) {
                TeacherInputScreen(
                    onFormSubmitted = { teacher ->
                        sharedViewModel.setTeacher(teacher)
                        navController.navigate(Routes.Home) {
                            popUpTo(Routes.Profile) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

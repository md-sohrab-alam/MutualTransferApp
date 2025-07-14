package com.shikshak.transfer.ui.theme.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.shikshak.transfer.ui.theme.filter.TeacherFilterListScreen
import com.shikshak.transfer.ui.theme.login.LoginScreen
import com.shikshak.transfer.ui.theme.login.OtpVerificationScreen
import com.shikshak.transfer.ui.theme.login.PhoneAuthViewModel
import com.shikshak.transfer.ui.theme.login.PhoneNumberInputScreen
import com.shikshak.transfer.ui.theme.matchprofile.MatchListScreen
import com.shikshak.transfer.ui.theme.profile.TeacherProfileScreen
import com.shikshak.transfer.ui.theme.register.RegisterScreen

@Composable
fun AppNavHost(
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val activity = LocalActivity.current!!

    // 👇 States to track loading teacher and decide destination
    var appReady by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf(Routes.Auth) }

    // ✅ Auto-login check: run once
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            sharedViewModel.loadCurrentTeacher(onLoaded = {
                startDestination = if (sharedViewModel.isProfileComplete()) {
                    Routes.MatchList
                } else {
                    Routes.Profile
                }
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


    NavHost(navController = navController, startDestination = startDestination) {
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
                        navController.navigate(Routes.MatchList)
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
            TeacherProfileScreen(onProfileSaved = { teacher ->
                sharedViewModel.setTeacher(teacher)
                navController.navigate(Routes.MatchList)
            })
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
    }
}

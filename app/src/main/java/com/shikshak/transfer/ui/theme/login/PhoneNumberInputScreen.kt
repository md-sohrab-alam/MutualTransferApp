package com.shikshak.transfer.ui.theme.login

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun PhoneNumberInputScreen(
    viewModel: PhoneAuthViewModel,
    activity: Activity,
    navController: NavController
) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Enter Mobile Number") })

        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    viewModel.sendOtp(phoneNumber, activity)
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Send OTP")
            }
        }

        viewModel.run {
            if (showErrorDialog.value && errorMessage.value != null) {
                ErrorAlertDialog(
                    showDialog = showErrorDialog,
                    message = errorMessage.value!!, // Non-null asserted because we check errorMessage.value != null
                    onDismiss = {
                        showErrorDialog.value = false
                        errorMessage.value = null // Optionally clear the error message on dismiss
                    }
                )
            }
        }


        // ✅ Automatically navigate to OTP screen if required
        if (viewModel.otpSent.value) {
            LaunchedEffect(Unit) {
                navController.navigate(Routes.OtpVerification)
            }
        }

        // ✅ Automatically navigate to profile/dashboard if auto-login success
        if (viewModel.isOtpVerified.value) {
            LaunchedEffect(Unit) {
                navController.navigate(Routes.Profile) {
                    popUpTo(Routes.PhoneInput) { inclusive = true }
                }
            }
        }
    }
}

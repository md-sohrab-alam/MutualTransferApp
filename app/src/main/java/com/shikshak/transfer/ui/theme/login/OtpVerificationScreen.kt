package com.shikshak.transfer.ui.theme.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes

@Composable
fun OtpVerificationScreen(viewModel: PhoneAuthViewModel, navController: NavController) {
    var otpCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Center
    ) {
        TextField(value = otpCode, onValueChange = { otpCode = it }, label = { Text("Enter OTP") })

        Button(
            onClick = {
                viewModel.verifyOtp(otpCode)
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Verify OTP")
        }

        // ✅ Navigation triggered when OTP is verified
        if (viewModel.isOtpVerified.value) {
            LaunchedEffect(Unit) {
                navController.navigate(Routes.Profile) {
                    popUpTo("otp_screen") { inclusive = true }
                }
            }
        }
    }
}

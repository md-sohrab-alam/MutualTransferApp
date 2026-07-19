package com.shikshak.transfer.ui.theme.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.shikshak.transfer.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes
import com.shikshak.transfer.ui.theme.utils.CommonErrorDialog
import com.shikshak.transfer.ui.theme.utils.FullScreenLoader
import com.shikshak.transfer.ui.theme.utils.InlineError
import androidx.compose.runtime.collectAsState

@Composable
fun OtpVerificationScreen(
    viewModel: PhoneAuthViewModel, 
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }
    var isOtpValid by remember { mutableStateOf(true) }
    var otpError by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    // Get string resources in Composable context
    val otpRequiredText = stringResource(R.string.otp_required)
    val otpMustBe6DigitsText = stringResource(R.string.otp_must_be_6_digits)
    val otpMustContainOnlyDigitsText = stringResource(R.string.otp_must_contain_only_digits)

    // Function to validate OTP
    fun validateOtp(otp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                otpError = otpRequiredText
                false
            }
            otp.length != 6 -> {
                otpError = otpMustBe6DigitsText
                false
            }
            !otp.all { it.isDigit() } -> {
                otpError = otpMustContainOnlyDigitsText
                false
            }
            else -> {
                otpError = ""
                true
            }
        }
    }

    // Function to handle OTP input
    fun onOtpChange(newValue: String) {
        // Only allow digits and limit to 6 characters
        val filteredValue = newValue.filter { it.isDigit() }.take(6)
        otpCode = filteredValue
        
        // Clear error when user starts typing
        if (otpError.isNotEmpty()) {
            isOtpValid = validateOtp(filteredValue)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = stringResource(R.string.otp_verification),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle
            Text(
                text = stringResource(R.string.enter_6_digit_code_sent_to_your_phone),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // OTP Input Field
            OutlinedTextField(
                value = otpCode,
                onValueChange = { onOtpChange(it) },
                placeholder = { Text(stringResource(R.string.enter_6_digit_otp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isOtpValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE), // Light beige or light red
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = if (isOtpValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE),
                    focusedContainerColor = if (isOtpValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE),
                    unfocusedIndicatorColor = if (isOtpValid) Color.Transparent else Color(0xFFE57373),
                    focusedIndicatorColor = if (isOtpValid) Color.Transparent else Color(0xFFE57373),
                    unfocusedPlaceholderColor = Color(0xFF666666),
                    focusedPlaceholderColor = Color(0xFF666666),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                isError = !isOtpValid
            )
            
            // Error message using common component
            if (!isOtpValid && otpError.isNotEmpty()) {
                InlineError(message = otpError)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Verify OTP Button
            Button(
                onClick = {
                    if (validateOtp(otpCode)) {
                        isOtpValid = true
                        viewModel.verifyOtp(otpCode)
                    } else {
                        isOtpValid = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (otpCode.length == 6 && isOtpValid) 
                        Color(0xFFFFEB3B) else Color(0xFFCCCCCC) // Light yellow or gray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = otpCode.length == 6 && isOtpValid && !isLoading
            ) {
                Text(
                    text = stringResource(R.string.verify_otp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (otpCode.length == 6 && isOtpValid) Color.Black else Color(0xFF666666)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Resend OTP option
            Text(
                text = stringResource(R.string.didnt_receive_otp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.you_can_request_new_otp_after_30_seconds),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF999999),
                textAlign = TextAlign.Center
            )
        }

        // Full screen loader
        if (isLoading) {
            FullScreenLoader(message = stringResource(R.string.verifying_otp))
        }
    }

    // Common Error Dialog
    viewModel.run {
        if (showErrorDialog.value && errorMessage.value != null) {
            CommonErrorDialog(
                showDialog = showErrorDialog.value,
                title = stringResource(R.string.verification_failed),
                message = errorMessage.value.orEmpty(),
                onDismiss = {
                    showErrorDialog.value = false
                    errorMessage.value = null
                },
                onRetry = {
                    if (validateOtp(otpCode)) {
                        viewModel.verifyOtp(otpCode)
                    }
                }
            )
        }
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

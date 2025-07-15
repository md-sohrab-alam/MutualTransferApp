package com.shikshak.transfer.ui.theme.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun OtpVerificationScreen(
    viewModel: PhoneAuthViewModel, 
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }
    var isOtpValid by remember { mutableStateOf(true) }
    var otpError by remember { mutableStateOf("") }

    // Function to validate OTP
    fun validateOtp(otp: String): Boolean {
        return when {
            otp.isEmpty() -> {
                otpError = "OTP is required"
                false
            }
            otp.length != 6 -> {
                otpError = "OTP must be 6 digits"
                false
            }
            !otp.all { it.isDigit() } -> {
                otpError = "OTP must contain only digits"
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
            text = "OTP Verification",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Enter the 6-digit code sent to your phone",
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
            placeholder = { Text("Enter 6-digit OTP") },
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
        
        // Error message
        if (!isOtpValid && otpError.isNotEmpty()) {
            Text(
                text = otpError,
                color = Color(0xFFE57373),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Verify OTP Button
        if (viewModel.isLoading.value) {
            CircularProgressIndicator(
                color = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        } else {
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
                enabled = otpCode.length == 6 && isOtpValid
            ) {
                Text(
                    text = "Verify OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (otpCode.length == 6 && isOtpValid) Color.Black else Color(0xFF666666)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resend OTP option
        Text(
            text = "Didn't receive OTP?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You can request a new OTP after 30 seconds",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center
        )
    }

    // Error Dialog
    viewModel.run {
        if (showErrorDialog.value && errorMessage.value != null) {
            ErrorAlertDialog(
                showDialog = showErrorDialog,
                message = errorMessage.value!!,
                onDismiss = {
                    showErrorDialog.value = false
                    errorMessage.value = null
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

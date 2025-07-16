package com.shikshak.transfer.ui.theme.login

import android.app.Activity
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
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes
import com.shikshak.transfer.ui.theme.utils.CommonErrorDialog
import com.shikshak.transfer.ui.theme.utils.FullScreenLoader
import com.shikshak.transfer.ui.theme.utils.InlineError
import androidx.compose.runtime.collectAsState

@Composable
fun PhoneNumberInputScreen(
    viewModel: PhoneAuthViewModel,
    activity: Activity,
    navController: NavController
) {
    var phoneNumber by remember { mutableStateOf("") }
    var isPhoneValid by remember { mutableStateOf(true) }
    var phoneError by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    // Function to validate phone number
    fun validatePhoneNumber(phone: String): Boolean {
        return when {
            phone.isEmpty() -> {
                phoneError = "Phone number is required"
                false
            }
            phone.length != 10 -> {
                phoneError = "Phone number must be 10 digits"
                false
            }
            !phone.all { it.isDigit() } -> {
                phoneError = "Phone number must contain only digits"
                false
            }
            !phone.startsWith("6") && !phone.startsWith("7") && !phone.startsWith("8") && !phone.startsWith("9") -> {
                phoneError = "Phone number must start with 6, 7, 8, or 9"
                false
            }
            else -> {
                phoneError = ""
                true
            }
        }
    }

    // Function to handle phone number input
    fun onPhoneNumberChange(newValue: String) {
        // Only allow digits and limit to 10 characters
        val filteredValue = newValue.filter { it.isDigit() }.take(10)
        phoneNumber = filteredValue
        
        // Clear error when user starts typing
        if (phoneError.isNotEmpty()) {
            isPhoneValid = validatePhoneNumber(filteredValue)
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
                text = "Teacher Transfer",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Welcome Text
            Text(
                text = "Welcome",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Phone Number Input Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { onPhoneNumberChange(it) },
                placeholder = { Text("Enter your phone number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isPhoneValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE), // Light beige or light red
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = if (isPhoneValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE),
                    focusedContainerColor = if (isPhoneValid) Color(0xFFF5F5DC) else Color(0xFFFFEBEE),
                    unfocusedIndicatorColor = if (isPhoneValid) Color.Transparent else Color(0xFFE57373),
                    focusedIndicatorColor = if (isPhoneValid) Color.Transparent else Color(0xFFE57373),
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
                isError = !isPhoneValid
            )
            
            // Error message using common component
            if (!isPhoneValid && phoneError.isNotEmpty()) {
                InlineError(message = phoneError)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Send OTP Button
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = {
                        if (validatePhoneNumber(phoneNumber)) {
                            isPhoneValid = true
                            viewModel.sendOtp(phoneNumber, activity)
                        } else {
                            isPhoneValid = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (phoneNumber.length == 10 && isPhoneValid) 
                            Color(0xFFFFEB3B) else Color(0xFFCCCCCC) // Light yellow or gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = phoneNumber.length == 10 && isPhoneValid
                ) {
                    Text(
                        text = "Send OTP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (phoneNumber.length == 10 && isPhoneValid) Color.Black else Color(0xFF666666)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
            
            // Footer Text
            Text(
                text = "Only Bihar government teachers can access this app",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // Full screen loader
        if (isLoading) {
            FullScreenLoader(message = "Sending OTP...")
        }
    }

    // Common Error Dialog
    viewModel.run {
        if (showErrorDialog.value && errorMessage.value != null) {
            CommonErrorDialog(
                showDialog = showErrorDialog.value,
                title = "OTP Send Failed",
                message = errorMessage.value!!,
                onDismiss = {
                    showErrorDialog.value = false
                    errorMessage.value = null
                },
                onRetry = {
                    if (validatePhoneNumber(phoneNumber)) {
                        viewModel.sendOtp(phoneNumber, activity)
                    }
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

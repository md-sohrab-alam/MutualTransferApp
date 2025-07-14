package com.shikshak.transfer.ui.theme.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shikshak.transfer.ui.theme.navigation.Routes

@Composable
fun LoginScreen(navController: NavController,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.login(email.value, password.value, onLoginSuccess = {
                Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }, onError = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp)) // Add some space

        // Register Button
        OutlinedButton( // Or use Button for a filled look, or TextButton for just text
            onClick = { navController.navigate(Routes.Register) }, // Call the navigation callback
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New user? Register here")
        }
    }
}

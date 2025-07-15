package com.shikshak.transfer.ui.theme.register

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var post by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") }, visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = school,
            onValueChange = { school = it },
            label = { Text("School Name") })
        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("District") })
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") })
        OutlinedTextField(value = post, onValueChange = { post = it }, label = { Text("Post") })

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                if (email.isNotBlank() && password.length >= 6 && name.isNotBlank()) {
                    viewModel.registerUser(
                        name, email, password, school, district, subject, post,
                        onSuccess = {
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT)
                                .show()
                            onRegisterSuccess()
                        }
                    )
                } else {
                    Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Text(text = "Register")
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
    }

    // Show error dialog if there's an error
    if (viewModel.showErrorDialog.value && viewModel.errorMessage.value != null) {
        ErrorAlertDialog(
            showDialog = viewModel.showErrorDialog,
            message = viewModel.errorMessage.value!!,
            onDismiss = {
                viewModel.clearError()
            }
        )
    }
}

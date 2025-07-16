package com.shikshak.transfer.ui.theme.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MoreViewModel = hiltViewModel(),
    onNavigateToLanguageSelector: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "⚙️ More Options",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Change Language
        MenuItem(
            icon = "🌐",
            title = "Change Language",
            subtitle = "Select your preferred language",
            onClick = { onNavigateToLanguageSelector() }
        )
        
        // About
        MenuItem(
            icon = "ℹ️",
            title = "About",
            subtitle = "Learn more about the app",
            onClick = { viewModel.showAboutDialog() }
        )
        
        // Logout
        MenuItem(
            icon = "🚪",
            title = "Logout",
            subtitle = "Sign out from your account",
            onClick = { showLogoutDialog = true }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("🚪 Confirm Logout")
            },
            text = {
                Text("Are you sure you want to logout? You will need to sign in again to access the app.")
            },
            confirmButton = {
                                        Button(
                            onClick = {
                                viewModel.logout(context) {
                                    // Restart the app completely to ensure proper flow
                                    viewModel.restartApp(context)
                                }
                                showLogoutDialog = false
                            }
                        ) {
                            Text("Yes, Logout")
                        }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // About Dialog
    if (viewModel.showAboutDialog.value) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAboutDialog() },
            title = {
                Text("ℹ️ About Mutual Transfer App")
            },
            text = {
                Column {
                    Text(
                        "This app helps teachers in Bihar, India to find mutual transfer opportunities. " +
                        "Teachers can create transfer requests, view matching requests, and connect with " +
                        "other teachers for mutual transfers.\n\n" +
                        "Features:\n" +
                        "• Create transfer requests\n" +
                        "• View matching requests\n" +
                        "• Manage teacher profiles\n" +
                        "• Filter and search options\n\n" +
                        "Developed for the education community of Bihar.\n\n" +
                        "📱 App Information:\n" +
                        "Version: 1.0.0\n" +
                        "Mutual Transfer App for Bihar Teachers"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.hideAboutDialog() }
                ) {
                    Text("OK")
                }
            }
        )
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

@Composable
private fun MenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 
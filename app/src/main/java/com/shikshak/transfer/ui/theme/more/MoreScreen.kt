package com.shikshak.transfer.ui.theme.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog
import com.shikshak.transfer.R

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
            text = "⚙️ ${stringResource(R.string.more_options_title)}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Change Language
        MenuItem(
            icon = "🌐",
            title = stringResource(R.string.change_language),
            subtitle = stringResource(R.string.change_language_subtitle),
            onClick = { onNavigateToLanguageSelector() }
        )
        
        // About
        MenuItem(
            icon = "ℹ️",
            title = stringResource(R.string.about),
            subtitle = stringResource(R.string.about_subtitle),
            onClick = { viewModel.showAboutDialog() }
        )
        
        // Logout
        MenuItem(
            icon = "🚪",
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.logout_subtitle),
            onClick = { showLogoutDialog = true }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("🚪 ${stringResource(R.string.confirm_logout_title)}")
            },
            text = {
                Text(stringResource(R.string.confirm_logout_message))
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
                    Text(stringResource(R.string.yes_logout))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // About Dialog
    if (viewModel.showAboutDialog.value) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAboutDialog() },
            title = {
                Text("ℹ️ ${stringResource(R.string.about_dialog_title)}")
            },
            text = {
                Column {
                    Text(stringResource(R.string.about_dialog_content))
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.hideAboutDialog() }
                ) {
                    Text(stringResource(R.string.ok))
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
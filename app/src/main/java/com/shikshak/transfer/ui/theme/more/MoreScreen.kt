package com.shikshak.transfer.ui.theme.more

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog
import com.shikshak.transfer.ui.theme.disclaimer.DisclaimerDialog
import com.shikshak.transfer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MoreViewModel = hiltViewModel(),
    onNavigateToLanguageSelector: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAboutPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    
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
        
        // About & Privacy
        MenuItem(
            icon = "🔒",
            title = stringResource(R.string.menu_about_privacy),
            subtitle = "View app information and privacy policy",
            onClick = { onNavigateToAboutPrivacy() }
        )
        
        // View Disclaimer
        MenuItem(
            icon = "⚠️",
            title = stringResource(R.string.menu_view_disclaimer),
            subtitle = "Read the app disclaimer",
            onClick = { showDisclaimerDialog = true }
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
        
        // Rate App
        MenuItem(
            icon = "⭐",
            title = stringResource(R.string.rate_app),
            subtitle = stringResource(R.string.rate_app_subtitle),
            onClick = { viewModel.rateApp(context) }
        )
        
        // Logout
        MenuItem(
            icon = "🚪",
            title = stringResource(R.string.logout),
            subtitle = stringResource(R.string.logout_subtitle),
            onClick = { showLogoutDialog = true }
        )
        
        // Footer
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.footer_unofficial),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.powered_by),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.app_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Disclaimer Dialog (Read-only mode)
    if (showDisclaimerDialog) {
        DisclaimerDialog(
            show = true,
            onAccept = { showDisclaimerDialog = false },
            onExit = { showDisclaimerDialog = false },
            isReadOnly = true
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
                AboutDialogContent()
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
            message = viewModel.errorMessage.value.orEmpty(),
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

@Composable
private fun AboutDialogContent() {
    val context = LocalContext.current
    val content = stringResource(R.string.about_dialog_content)
    
    // Split content by lines to handle email separately
    val lines = content.split("\n")
    
    Column {
        lines.forEach { line ->
            when {
                line.contains("iamsohrabalam@gmail.com") -> {
                    // Make email clickable
                    val email = "iamsohrabalam@gmail.com"
                    val beforeEmail = line.substringBefore(email)
                    val afterEmail = line.substringAfter(email, "")
                    
                    Row {
                        if (beforeEmail.isNotEmpty()) {
                            Text(
                                text = beforeEmail,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                    putExtra(Intent.EXTRA_SUBJECT, "MutualTransfer App Support")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to copy email
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Email", email)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Email copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        if (afterEmail.isNotEmpty()) {
                            Text(
                                text = afterEmail,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 
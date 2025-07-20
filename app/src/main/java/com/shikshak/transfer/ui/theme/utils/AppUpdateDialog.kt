package com.shikshak.transfer.ui.theme.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppUpdateDialog(
    isForceUpdate: Boolean,
    currentVersion: String,
    newVersion: String,
    updateMessage: String = "",
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val icon = if (isForceUpdate) Icons.Default.Warning else Icons.Default.Info
    val iconTint = if (isForceUpdate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    
    Dialog(
        onDismissRequest = { 
            if (!isForceUpdate) onDismiss() 
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForceUpdate,
            dismissOnClickOutside = !isForceUpdate
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = if (isForceUpdate) "Force Update Warning" else "Update Available",
                    modifier = Modifier.size(48.dp),
                    tint = iconTint
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = if (isForceUpdate) "Update Required" else "Update Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Version info
                Text(
                    text = "Current Version: $currentVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "New Version: $newVersion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Message
                if (updateMessage.isNotEmpty()) {
                    Text(
                        text = updateMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // Force update warning
                if (isForceUpdate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This update is required to continue using the app. Please update now.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isForceUpdate) {
                        // Optional update - show "Later" button
                        OutlinedButton(
                            onClick = onLaterClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Later")
                        }
                    }
                    
                    // Update button
                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Update",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Update Now")
                    }
                }
            }
        }
    }
}

@Composable
fun AppUpdateManager(
    isForceUpdate: Boolean,
    currentVersion: String,
    newVersion: String,
    updateMessage: String = "",
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(true) }
    
    if (showDialog) {
        AppUpdateDialog(
            isForceUpdate = isForceUpdate,
            currentVersion = currentVersion,
            newVersion = newVersion,
            updateMessage = updateMessage,
            onUpdateClick = {
                showDialog = false
                onUpdateClick()
            },
            onLaterClick = {
                showDialog = false
                onLaterClick()
            },
            onDismiss = {
                showDialog = false
                onDismiss()
            }
        )
    }
} 
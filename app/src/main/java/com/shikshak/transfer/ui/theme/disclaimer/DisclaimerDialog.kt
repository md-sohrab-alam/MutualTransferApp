package com.shikshak.transfer.ui.theme.disclaimer

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
import com.shikshak.transfer.R

@Composable
fun DisclaimerDialog(
    show: Boolean,
    onAccept: () -> Unit,
    onExit: () -> Unit,
    isReadOnly: Boolean = false
) {
    if (show) {
        var isChecked by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { /* Non-dismissible for first run */ },
            title = {
                Text(
                    text = stringResource(R.string.disclaimer_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.disclaimer_body),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    if (!isReadOnly) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.disclaimer_checkbox),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onAccept,
                    enabled = isReadOnly || isChecked
                ) {
                    Text(stringResource(R.string.disclaimer_accept))
                }
            },
            dismissButton = {
                if (!isReadOnly) {
                    OutlinedButton(
                        onClick = onExit
                    ) {
                        Text(stringResource(R.string.disclaimer_exit))
                    }
                }
            }
        )
    }
}

package com.shikshak.transfer.ui.theme.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun ErrorAlertDialog(
    showDialog: MutableState<Boolean>, // State to control dialog visibility
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit = { showDialog.value = false } // Default dismiss action
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // This is called when the user clicks outside the dialog
                // or presses the back button.
                onDismiss()
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            }
            // You can also add a dismissButton if needed:
            // dismissButton = {
            //     TextButton(
            //         onClick = {
            //             onDismiss()
            //         }
            //     ) {
            //         Text("Cancel")
            //     }
            // }
        )
    }
}

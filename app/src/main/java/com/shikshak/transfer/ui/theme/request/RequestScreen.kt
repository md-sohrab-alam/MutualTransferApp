package com.shikshak.transfer.ui.theme.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.navigation.SharedViewModel
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun RequestScreen(
    viewModel: RequestViewModel = hiltViewModel(),
    sharedViewModel: SharedViewModel = hiltViewModel(),
    onNavigateToCreateRequest: () -> Unit = {},
    onNavigateToEditRequest: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val hasTransferRequest by viewModel.hasTransferRequest.collectAsState()
    val transferRequest by viewModel.transferRequest.collectAsState()
    val currentTeacher = sharedViewModel.currentTeacher
    
    // Load teacher profile data when screen is created
    LaunchedEffect(Unit) {
        if (currentTeacher == null) {
            sharedViewModel.loadCurrentTeacher(
                onLoaded = {
                    // Profile loaded successfully
                },
                onError = { error ->
                    // Handle error if needed
                }
            )
        }
        viewModel.checkTransferRequest()
    }
    
    LaunchedEffect(onNavigateToCreateRequest, onNavigateToEditRequest) {
        viewModel.setNavigationCallbacks(
            onNavigateToCreateRequest = onNavigateToCreateRequest,
            onNavigateToEditRequest = onNavigateToEditRequest
        )
    }
    
    // Show loader while loading teacher profile or transfer request
    if (isLoading || currentTeacher == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (hasTransferRequest) {
        transferRequest?.let { request ->
            EditRequestScreen(
                transferRequest = request,
                viewModel = viewModel
            )
        } ?: CreateRequestScreen(viewModel = viewModel)
    } else {
        CreateRequestScreen(viewModel = viewModel)
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
fun CreateRequestScreen(
    viewModel: RequestViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // How It Works Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📋 ${stringResource(R.string.how_it_works_title)}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.how_it_works_description)
                )
            }
        }
        
        // Create Request Form
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ready_to_transfer),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.navigateToCreateRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.submit_transfer_request))
                }
            }
        }
        
        // FAQ Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.faq_title),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.faq_content)
                )
            }
        }
        
        // Footer
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.footer_unofficial),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun EditRequestScreen(
    transferRequest: TransferRequest,
    viewModel: RequestViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Request Details
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.request_details),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Current Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.current_location),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${transferRequest.currentSchool}, ${transferRequest.currentDistrict}"
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Preferred Locations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.preferred_locations),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transferRequest.preferredDistricts.joinToString(", ")
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.status),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transferRequest.status,
                        color = when (transferRequest.status) {
                            "PENDING" -> Color.Blue
                            "MATCHED" -> Color.Green
                            "COMPLETED" -> Color.Gray
                            "CANCELLED" -> Color.Red
                            else -> Color.Black
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Submitted Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.submitted_date),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transferRequest.submittedDate
                    )
                }
            }
        }
        
        // Action Buttons
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.navigateToEditRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.edit_request))
                }
                
                OutlinedButton(
                    onClick = { viewModel.showCancelConfirmation() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.cancel_request))
                }
            }
        }
    }
    
    // Cancel Confirmation Dialog
    if (viewModel.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCancelConfirmation() },
            title = { Text(stringResource(R.string.cancel_request_title)) },
            text = { Text(stringResource(R.string.cancel_request_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelRequest()
                        viewModel.hideCancelConfirmation()
                    }
                ) {
                    Text(stringResource(R.string.cancel_request_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCancelConfirmation() }) {
                    Text(stringResource(R.string.cancel_request_dismiss))
                }
            }
        )
    }
} 
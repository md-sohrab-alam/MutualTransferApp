package com.shikshak.transfer.ui.theme.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.data.Blocks
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    viewModel: CreateRequestViewModel = hiltViewModel(),
    onRequestSubmitted: (TransferRequest) -> Unit
) {
    // State for form fields
    var preferredDistricts by remember { mutableStateOf(listOf<String>()) }
    var preferredBlocks by remember { mutableStateOf(listOf<String>()) }
    var contactPreference by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    
    // Function to validate form
    fun validateForm(): Boolean {
        return preferredDistricts.isNotEmpty()
    }
    
    // Function to get validation error message
    fun getValidationError(): String {
        return when {
            preferredDistricts.isEmpty() -> "Please select at least one preferred district"
            else -> ""
        }
    }
    
    // Dropdown options - Bihar Districts
    val districts = listOf(
        "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai", "Bhagalpur",
        "Bhojpur", "Buxar", "Darbhanga", "East Champaran", "Gaya", "Gopalganj",
        "Jamui", "Jehanabad", "Kaimur", "Katihar", "Khagaria", "Kishanganj",
        "Lakhisarai", "Madhepura", "Madhubani", "Munger", "Muzaffarpur", "Nalanda",
        "Nawada", "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur",
        "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan", "Supaul",
        "Vaishali", "West Champaran"
    )
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentTeacher by viewModel.currentTeacher.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadCurrentTeacher()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Title
        Text(
            text = stringResource(R.string.create_transfer_request_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Current Location Info
        currentTeacher?.let { teacher ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.current_location),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name: ${teacher.name}")
                    Text("School: ${teacher.schoolName}")
                    Text("District: ${teacher.district}")
                    Text("Block: ${teacher.block}")
                    Text("Designation: ${teacher.designation}")
                    if (teacher.subject.isNotBlank()) {
                        Text("Subject: ${teacher.subject}")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Preferred Districts (Multi-select) - Max 3
        Text(
            text = stringResource(R.string.preferred_districts_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // District Dropdown
        var districtDropdownExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = districtDropdownExpanded,
            onExpandedChange = { districtDropdownExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                readOnly = true,
                label = { Text(stringResource(R.string.select_district_hint)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = districtDropdownExpanded,
                onDismissRequest = { districtDropdownExpanded = false }
            ) {
                districts.filter { district -> !preferredDistricts.contains(district) }
                    .forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district) },
                            onClick = {
                                if (preferredDistricts.size < 3) {
                                    preferredDistricts = preferredDistricts + district
                                }
                                districtDropdownExpanded = false
                            }
                        )
                    }
            }
        }
        
        // Selected Districts Display
        if (preferredDistricts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.selected_districts),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(preferredDistricts) { district ->
                    Card(
                        modifier = Modifier.padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = district,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    preferredDistricts = preferredDistricts - district
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Preferred Blocks (Optional) - Only from selected districts
        Text(
            text = stringResource(R.string.preferred_blocks_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Get blocks only from selected districts
        val availableBlocks = if (preferredDistricts.isNotEmpty()) {
            preferredDistricts.flatMap { district ->
                Blocks.getBlocksForDistrict(district)
            }.distinct().sorted()
        } else {
            emptyList()
        }
        
        // Block Dropdown (only if districts are selected)
        if (preferredDistricts.isNotEmpty()) {
            var blockDropdownExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = blockDropdownExpanded,
                onExpandedChange = { blockDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_block_hint)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = blockDropdownExpanded,
                    onDismissRequest = { blockDropdownExpanded = false }
                ) {
                    availableBlocks.filter { block -> !preferredBlocks.contains(block) }
                        .forEach { block ->
                            DropdownMenuItem(
                                text = { Text(block) },
                                onClick = {
                                    preferredBlocks = preferredBlocks + block
                                    blockDropdownExpanded = false
                                }
                            )
                        }
                }
            }
            
            // Selected Blocks Display
            if (preferredBlocks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.selected_blocks),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(preferredBlocks) { block ->
                        Card(
                            modifier = Modifier.padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = block,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        preferredBlocks = preferredBlocks - block
                                    },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = stringResource(R.string.select_districts_first),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Contact Preference
        Text(
            text = stringResource(R.string.contact_preference_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = contactPreference,
                    onClick = { contactPreference = true }
                )
                Text(stringResource(R.string.allow_contact))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !contactPreference,
                    onClick = { contactPreference = false }
                )
                Text(stringResource(R.string.dont_allow_contact))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Notes (Optional)
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.notes_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        Button(
            onClick = {
                if (validateForm() && currentTeacher != null) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())
                    
                    val transferRequest = TransferRequest(
                        teacherId = currentUser?.uid ?: "",
                        teacherName = currentTeacher!!.name,
                        currentDistrict = currentTeacher!!.district,
                        currentSchool = currentTeacher!!.schoolName,
                        preferredDistricts = preferredDistricts,
                        preferredBlocks = preferredBlocks,
                        postLevel = currentTeacher!!.post,
                        designation = currentTeacher!!.designation,
                        subject = currentTeacher!!.subject,
                        status = "PENDING",
                        submittedDate = currentDate,
                        contactPreference = contactPreference,
                        notes = notes
                    )
                    viewModel.submitTransferRequest(transferRequest)
                    onRequestSubmitted(transferRequest)
                } else {
                    // Show error message if form is invalid
                    val errorMessage = getValidationError()
                    if (errorMessage.isNotBlank()) {
                        viewModel.errorMessage.value = errorMessage
                        viewModel.showErrorDialog.value = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading.value && validateForm()
        ) {
            if (viewModel.isLoading.value) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(stringResource(R.string.submit_transfer_request))
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
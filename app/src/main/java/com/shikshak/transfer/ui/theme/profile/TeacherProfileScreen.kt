package com.shikshak.transfer.ui.theme.profile

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext as LocalContext1
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog
import com.shikshak.transfer.ui.theme.data.Blocks
import androidx.compose.runtime.collectAsState
import com.shikshak.transfer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileSaved: (Teacher) -> Unit,
    onNavigateToTeacherInput: () -> Unit,
    isFirstLogin: Boolean = false
) {
    val context = LocalContext1.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isLoading by viewModel.isLoading.collectAsState()
    
    // String resources
    val completeProfileHeader = stringResource(R.string.complete_profile_header)
    val teacherProfileHeader = stringResource(R.string.teacher_profile_header)
    val editProfileHeader = stringResource(R.string.edit_profile_header)
    val fullNameLabel = stringResource(R.string.full_name_label)
    val mobileNumberLabel = stringResource(R.string.mobile_number_label)
    val postLevelLabel = stringResource(R.string.post_level_label)
    val designationLabel = stringResource(R.string.designation_label)
    val subjectLabel = stringResource(R.string.subject_label)
    val qualificationLabel = stringResource(R.string.qualification_label)
    val currentSchoolNameLabel = stringResource(R.string.current_school_name_label)
    val schoolDistrictLabel = stringResource(R.string.school_district_label)
    val schoolBlockLabel = stringResource(R.string.school_block_label)
    val contactPreferenceLabel = stringResource(R.string.contact_preference_label)
    val allowContact = stringResource(R.string.allow_contact)
    val dontAllowContact = stringResource(R.string.dont_allow_contact)
    val completeProfileButton = stringResource(R.string.complete_profile_button)
    val saveChangesButton = stringResource(R.string.save_changes_button)
    val cancelButton = stringResource(R.string.cancel_button)
    val editProfileButton = stringResource(R.string.edit_profile_button)
    val profileCompletedToast = stringResource(R.string.profile_completed_toast)
    val profileUpdatedToast = stringResource(R.string.profile_updated_toast)
    val notSet = stringResource(R.string.not_set)
    
    // State for editing mode
    var isEditing by remember { mutableStateOf(false) }
    
    // State for form fields
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var postLevel by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }
    var contactPreference by remember { mutableStateOf(true) }
    var mobileNumber by remember { mutableStateOf("") }
    
    // Load teacher data when component is created
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            viewModel.loadTeacherProfile(uid) { teacher ->
                name = teacher.name
                designation = teacher.designation
                subject = teacher.subject
                school = teacher.schoolName
                district = teacher.district
                block = teacher.block
                postLevel = teacher.post
                qualification = teacher.qualification
                contactPreference = teacher.contactPreference
                mobileNumber = teacher.contact.phone
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = if (isFirstLogin) completeProfileHeader else teacherProfileHeader,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Profile Information Display/Edit
            if (isEditing) {
                // Edit Mode
                Text(
                    text = if (isFirstLogin) completeProfileHeader else editProfileHeader,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(fullNameLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mobile Number (Non-editable)
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { },
                    label = { Text(mobileNumberLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Post Level Dropdown
                var postLevelExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = postLevelExpanded,
                    onExpandedChange = { postLevelExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = postLevel,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(postLevelLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = postLevelExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = postLevelExpanded,
                        onDismissRequest = { postLevelExpanded = false }
                    ) {
                        listOf(
                            stringResource(R.string.post_level_primary),
                            stringResource(R.string.post_level_upper_primary),
                            stringResource(R.string.post_level_secondary),
                            stringResource(R.string.post_level_higher_secondary)
                        ).forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    val previousPostLevel = postLevel
                                    postLevel = level
                                    postLevelExpanded = false
                                    
                                    // Clear designation if it's not valid for new post level
                                    if (previousPostLevel != level) {
                                        val validDesignations = when (level) {
                                            "Primary" -> listOf(
                                                "Assistant Teacher (सहायक शिक्षक)",
                                                "Head Teacher (प्रधानाध्यापक)",
                                                "Physical Education Teacher (PET)"
                                            )
                                            "Upper Primary" -> listOf(
                                                "Assistant Teacher (सहायक शिक्षक)",
                                                "Head Teacher (प्रधानाध्यापक)",
                                                "Physical Education Teacher (PET)",
                                                "Art / Music Teacher"
                                            )
                                            "Secondary" -> listOf(
                                                "Trained Graduate Teacher (TGT)",
                                                "Head Teacher (प्रधानाध्यापक)",
                                                "Physical Education Teacher (PET)",
                                                "Art / Music Teacher"
                                            )
                                            "Higher Secondary" -> listOf(
                                                "Post Graduate Teacher (PGT)",
                                                "Lecturer (प्रवक्ता)",
                                                "Head Teacher (प्रधानाध्यापक)",
                                                "Physical Education Teacher (PET)",
                                                "Art / Music Teacher"
                                            )
                                            else -> emptyList()
                                        }
                                        if (!validDesignations.contains(designation)) {
                                            designation = ""
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Designation Dropdown (only if post level is selected)
                if (postLevel.isNotEmpty()) {
                    var designationExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = designationExpanded,
                        onExpandedChange = { designationExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = designation,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(designationLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = designationExpanded,
                            onDismissRequest = { designationExpanded = false }
                        ) {
                            when (postLevel) {
                                "Primary" -> listOf(
                                    "Assistant Teacher (सहायक शिक्षक)",
                                    "Head Teacher (प्रधानाध्यापक)",
                                    "Physical Education Teacher (PET)"
                                ).forEach { designationName ->
                                    DropdownMenuItem(
                                        text = { Text(designationName) },
                                        onClick = {
                                            designation = designationName
                                            designationExpanded = false
                                        }
                                    )
                                }
                                "Upper Primary" -> listOf(
                                    "Assistant Teacher (सहायक शिक्षक)",
                                    "Head Teacher (प्रधानाध्यापक)",
                                    "Physical Education Teacher (PET)",
                                    "Art / Music Teacher"
                                ).forEach { designationName ->
                                    DropdownMenuItem(
                                        text = { Text(designationName) },
                                        onClick = {
                                            designation = designationName
                                            designationExpanded = false
                                        }
                                    )
                                }
                                "Secondary" -> listOf(
                                    "Trained Graduate Teacher (TGT)",
                                    "Head Teacher (प्रधानाध्यापक)",
                                    "Physical Education Teacher (PET)",
                                    "Art / Music Teacher"
                                ).forEach { designationName ->
                                    DropdownMenuItem(
                                        text = { Text(designationName) },
                                        onClick = {
                                            designation = designationName
                                            designationExpanded = false
                                        }
                                    )
                                }
                                "Higher Secondary" -> listOf(
                                    "Post Graduate Teacher (PGT)",
                                    "Lecturer (प्रवक्ता)",
                                    "Head Teacher (प्रधानाध्यापक)",
                                    "Physical Education Teacher (PET)",
                                    "Art / Music Teacher"
                                ).forEach { designationName ->
                                    DropdownMenuItem(
                                        text = { Text(designationName) },
                                        onClick = {
                                            designation = designationName
                                            designationExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(subjectLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text(qualificationLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text(currentSchoolNameLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // District Dropdown
                var districtExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(schoolDistrictLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        listOf(
                            "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai", "Bhagalpur",
                            "Bhojpur", "Buxar", "Darbhanga", "East Champaran", "Gaya", "Gopalganj",
                            "Jamui", "Jehanabad", "Kaimur", "Katihar", "Khagaria", "Kishanganj",
                            "Lakhisarai", "Madhepura", "Madhubani", "Munger", "Muzaffarpur", "Nalanda",
                            "Nawada", "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur",
                            "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan", "Supaul",
                            "Vaishali", "West Champaran"
                        ).forEach { districtName ->
                            DropdownMenuItem(
                                text = { Text(districtName) },
                                onClick = {
                                    district = districtName
                                    districtExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Block Dropdown (only if district is selected)
                if (district.isNotEmpty()) {
                    var blockExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = blockExpanded,
                        onExpandedChange = { blockExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = block,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(schoolBlockLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = blockExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = blockExpanded,
                            onDismissRequest = { blockExpanded = false }
                        ) {
                            // Get blocks for selected district
                            val blocks = Blocks.getBlocksForDistrict(district)
                            blocks.forEach { blockName ->
                                DropdownMenuItem(
                                    text = { Text(blockName) },
                                    onClick = {
                                        block = blockName
                                        blockExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact Preference
                Text(
                    text = contactPreferenceLabel,
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
                        Text(allowContact)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !contactPreference,
                            onClick = { contactPreference = false }
                        )
                        Text(dontAllowContact)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Save and Cancel buttons
                if (isFirstLogin) {
                    // For first login, only show save button
                    Button(
                        onClick = {
                            val updatedTeacher = Teacher(
                                uid = currentUser?.uid ?: "",
                                name = name,
                                designation = designation,
                                subject = subject,
                                schoolName = school,
                                district = district,
                                block = block,
                                post = postLevel,
                                qualification = qualification,
                                contactPreference = contactPreference,
                                contact = Contact(phone = mobileNumber)
                            )
                            viewModel.saveTeacherProfile(updatedTeacher) {
                                // Update the shared view model so changes are reflected immediately
                                onProfileSaved(updatedTeacher)
                                // Update local state with the saved data
                                name = updatedTeacher.name
                                designation = updatedTeacher.designation
                                subject = updatedTeacher.subject
                                school = updatedTeacher.schoolName
                                district = updatedTeacher.district
                                block = updatedTeacher.block
                                postLevel = updatedTeacher.post
                                qualification = updatedTeacher.qualification
                                contactPreference = updatedTeacher.contactPreference
                                mobileNumber = updatedTeacher.contact.phone
                            }
                            isEditing = false
                            Toast.makeText(context, profileCompletedToast, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(completeProfileButton)
                    }
                } else {
                    // For regular editing, show both save and cancel buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                val updatedTeacher = Teacher(
                                    uid = currentUser?.uid ?: "",
                                    name = name,
                                    designation = designation,
                                    subject = subject,
                                    schoolName = school,
                                    district = district,
                                    block = block,
                                    post = postLevel,
                                    qualification = qualification,
                                    contactPreference = contactPreference,
                                    contact = Contact(phone = mobileNumber)
                                )
                                viewModel.saveTeacherProfile(updatedTeacher) {
                                    // Update the shared view model so changes are reflected immediately
                                    onProfileSaved(updatedTeacher)
                                    // Update local state with the saved data
                                    name = updatedTeacher.name
                                    designation = updatedTeacher.designation
                                    subject = updatedTeacher.subject
                                    school = updatedTeacher.schoolName
                                    district = updatedTeacher.district
                                    block = updatedTeacher.block
                                    postLevel = updatedTeacher.post
                                    qualification = updatedTeacher.qualification
                                    contactPreference = updatedTeacher.contactPreference
                                    mobileNumber = updatedTeacher.contact.phone
                                }
                                isEditing = false
                                Toast.makeText(context, profileUpdatedToast, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(saveChangesButton)
                        }
                        
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cancelButton)
                        }
                    }
                }
            } else {
                
                // Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ProfileInfoRow(fullNameLabel, name.ifBlank { notSet })
                        ProfileInfoRow(mobileNumberLabel, mobileNumber.ifBlank { notSet })
                        ProfileInfoRow(currentSchoolNameLabel, school.ifBlank { notSet })
                        ProfileInfoRow(schoolDistrictLabel, district.ifBlank { notSet })
                        ProfileInfoRow(schoolBlockLabel, block.ifBlank { notSet })
                        ProfileInfoRow(postLevelLabel, postLevel.ifBlank { notSet })
                        ProfileInfoRow(subjectLabel, subject.ifBlank { notSet })
                        ProfileInfoRow(designationLabel, designation.ifBlank { notSet })
                        ProfileInfoRow(qualificationLabel, qualification.ifBlank { notSet })
                        ProfileInfoRow(contactPreferenceLabel, if (contactPreference) allowContact else dontAllowContact)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                if (isFirstLogin) {
                    // For first login, automatically start editing
                    LaunchedEffect(Unit) {
                        isEditing = true
                    }
                } else {
                    // For regular profile, show edit button
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(editProfileButton)
                    }
                }
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

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

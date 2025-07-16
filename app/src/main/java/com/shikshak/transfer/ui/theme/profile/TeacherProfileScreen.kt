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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext as LocalContext1
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog
import com.shikshak.transfer.ui.theme.data.Blocks
import androidx.compose.runtime.collectAsState

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
    
    // Function to get designations based on post level
    fun getDesignationsForPostLevel(level: String): List<String> {
        return when (level) {
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
    }
    
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
            text = if (isFirstLogin) "👨‍🏫 Complete Your Profile" else "👨‍🏫 Teacher Profile",
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
                    text = if (isFirstLogin) "✏️ Complete Your Profile" else "✏️ Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("👤 Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mobile Number (Non-editable)
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { },
                    label = { Text("📱 Mobile Number") },
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
                        label = { Text("🎓 Post Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = postLevelExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = postLevelExpanded,
                        onDismissRequest = { postLevelExpanded = false }
                    ) {
                        listOf("Primary", "Upper Primary", "Secondary", "Higher Secondary").forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    val previousPostLevel = postLevel
                                    postLevel = level
                                    postLevelExpanded = false
                                    
                                    // Clear designation if it's not valid for new post level
                                    if (previousPostLevel != level) {
                                        val validDesignations = getDesignationsForPostLevel(level)
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
                            label = { Text("👨‍🏫 Designation") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designationExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = designationExpanded,
                            onDismissRequest = { designationExpanded = false }
                        ) {
                            getDesignationsForPostLevel(postLevel).forEach { designationName ->
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("📘 Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("🎓 Qualification") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("🏫 Current School Name") },
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
                        label = { Text("📍 Your School District") },
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
                            label = { Text("🏘️ Your School Block") },
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
                    text = "📞 Contact Preference",
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
                        Text("Allow contact")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !contactPreference,
                            onClick = { contactPreference = false }
                        )
                        Text("Don't allow contact")
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
                            Toast.makeText(context, "Profile completed successfully!", Toast.LENGTH_SHORT).show()
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
                        Text("💾 Complete Profile")
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
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
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
                            Text("💾 Save Changes")
                        }
                        
                        OutlinedButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("❌ Cancel")
                        }
                    }
                }
            } else {
                // Display Mode
                Text(
                    text = if (isFirstLogin) "📋 Complete Your Profile" else "📋 Profile Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ProfileInfoRow("👤 Full Name", name.ifBlank { "Not set" })
                        ProfileInfoRow("📱 Mobile Number", mobileNumber.ifBlank { "Not set" })
                        ProfileInfoRow("🏫 Current School", school.ifBlank { "Not set" })
                        ProfileInfoRow("📍 Your School District", district.ifBlank { "Not set" })
                        ProfileInfoRow("🏘️ Your School Block", block.ifBlank { "Not set" })
                        ProfileInfoRow("🎓 Post Level", postLevel.ifBlank { "Not set" })
                        ProfileInfoRow("📘 Subject", subject.ifBlank { "Not set" })
                        ProfileInfoRow("👨‍🏫 Designation", designation.ifBlank { "Not set" })
                        ProfileInfoRow("🎓 Qualification", qualification.ifBlank { "Not set" })
                        ProfileInfoRow("📞 Contact Preference", if (contactPreference) "Allow contact" else "Don't allow contact")
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
                        Text("✏️ Edit Profile")
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

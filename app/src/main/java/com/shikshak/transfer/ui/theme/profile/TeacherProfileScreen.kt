package com.shikshak.transfer.ui.theme.profile

import Teacher
import Contact
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileSaved: (Teacher) -> Unit,
    onNavigateToTeacherInput: () -> Unit
) {
    val context = LocalContext1.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // State for editing mode
    var isEditing by remember { mutableStateOf(false) }
    
    // State for form fields
    var name by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var contactPreference by remember { mutableStateOf(true) }
    var postLevel by remember { mutableStateOf("") }
    
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
                contactPreference = teacher.contactPreference
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
            text = "👨‍🏫 Teacher Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (viewModel.isLoading.value) {
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
                    text = "✏️ Edit Profile",
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
                    value = school,
                    onValueChange = { school = it },
                    label = { Text("🏫 School Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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
                                contactPreference = contactPreference,
                                contact = Contact(phone = currentUser?.phoneNumber ?: "")
                            )
                            viewModel.saveTeacherProfile(updatedTeacher)
                            isEditing = false
                            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                            onProfileSaved(updatedTeacher)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading.value
                    ) {
                        if (viewModel.isLoading.value) {
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
            } else {
                // Display Mode
                Text(
                    text = "📋 Profile Information",
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
                        ProfileInfoRow("👤 Name", name.ifBlank { "Not set" })
                        ProfileInfoRow("👨‍🏫 Designation", designation.ifBlank { "Not set" })
                        ProfileInfoRow("📘 Subject", subject.ifBlank { "Not set" })
                        ProfileInfoRow("🏫 School", school.ifBlank { "Not set" })
                        ProfileInfoRow("📞 Contact Preference", if (contactPreference) "Allow contact" else "Don't allow contact")
                        ProfileInfoRow("📱 Phone", currentUser?.phoneNumber ?: "Not set")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Button(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✏️ Edit Profile")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onNavigateToTeacherInput,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Update Transfer Form")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

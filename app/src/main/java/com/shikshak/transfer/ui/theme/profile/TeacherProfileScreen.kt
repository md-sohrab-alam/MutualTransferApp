package com.shikshak.transfer.ui.theme.profile

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isFirstLogin) completeProfileHeader else teacherProfileHeader,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (!isFirstLogin && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Loading profile...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isEditing) {
                    // Edit Mode - Modern Form Design
                    EditProfileForm(
                        name = name,
                        onNameChange = { name = it },
                        mobileNumber = mobileNumber,
                        postLevel = postLevel,
                        onPostLevelChange = { postLevel = it },
                        designation = designation,
                        onDesignationChange = { designation = it },
                        subject = subject,
                        onSubjectChange = { subject = it },
                        qualification = qualification,
                        onQualificationChange = { qualification = it },
                        school = school,
                        onSchoolChange = { school = it },
                        district = district,
                        onDistrictChange = { district = it },
                        block = block,
                        onBlockChange = { block = it },
                        contactPreference = contactPreference,
                        onContactPreferenceChange = { contactPreference = it },
                        onSave = {
                            val teacher = Teacher(
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
                                contact = Contact(phone = mobileNumber),
                                preferredDistricts = emptyList(),
                                preferredBlocks = emptyList()
                            )
                            viewModel.saveTeacherProfile(teacher) {
                                onProfileSaved(teacher)
                                Toast.makeText(context, profileUpdatedToast, Toast.LENGTH_SHORT).show()
                                isEditing = false
                            }
                        },
                        onCancel = { isEditing = false },
                        isFirstLogin = isFirstLogin,
                        completeProfileButton = completeProfileButton,
                        saveChangesButton = saveChangesButton,
                        cancelButton = cancelButton
                    )
                } else {
                    // View Mode - Modern Card Design
                    ViewProfileCard(
                        name = name,
                        mobileNumber = mobileNumber,
                        postLevel = postLevel,
                        designation = designation,
                        subject = subject,
                        qualification = qualification,
                        school = school,
                        district = district,
                        block = block,
                        contactPreference = contactPreference,
                        onEditClick = { isEditing = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileForm(
    name: String,
    onNameChange: (String) -> Unit,
    mobileNumber: String,
    postLevel: String,
    onPostLevelChange: (String) -> Unit,
    designation: String,
    onDesignationChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    qualification: String,
    onQualificationChange: (String) -> Unit,
    school: String,
    onSchoolChange: (String) -> Unit,
    district: String,
    onDistrictChange: (String) -> Unit,
    block: String,
    onBlockChange: (String) -> Unit,
    contactPreference: Boolean,
    onContactPreferenceChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isFirstLogin: Boolean,
    completeProfileButton: String,
    saveChangesButton: String,
    cancelButton: String
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Personal Information Card
        ModernCard(
            title = "Personal Information",
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Full Name",
                    icon = Icons.Default.Person
                )
                
                ModernTextField(
                    value = mobileNumber,
                    onValueChange = { },
                    label = "Mobile Number",
                    icon = Icons.Default.Phone,
                    readOnly = true,
                    enabled = false
                )
            }
        }
        
        // Professional Information Card
        ModernCard(
            title = "Professional Information",
            icon = Icons.Default.Work
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernDropdown(
                    value = postLevel,
                    onValueChange = onPostLevelChange,
                    label = "Post Level",
                    icon = Icons.Default.School,
                    options = listOf(
                        "Primary",
                        "Upper Primary", 
                        "Secondary",
                        "Higher Secondary"
                    )
                )
                
                if (postLevel.isNotEmpty()) {
                    ModernDropdown(
                        value = designation,
                        onValueChange = onDesignationChange,
                        label = "Designation",
                        icon = Icons.Default.Badge,
                        options = getValidDesignations(postLevel)
                    )
                }
                
                ModernTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = "Subject",
                    icon = Icons.Default.Book
                )
                
                ModernTextField(
                    value = qualification,
                    onValueChange = onQualificationChange,
                    label = "Qualification",
                    icon = Icons.Default.School
                )
            }
        }
        
        // School Information Card
        ModernCard(
            title = "School Information",
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernTextField(
                    value = school,
                    onValueChange = onSchoolChange,
                    label = "Current School Name",
                    icon = Icons.Default.School
                )
                
                ModernDropdown(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = "District",
                    icon = Icons.Default.LocationOn,
                    options = getBiharDistricts()
                )
                
                if (district.isNotEmpty()) {
                    ModernDropdown(
                        value = block,
                        onValueChange = onBlockChange,
                        label = "Block",
                        icon = Icons.Default.LocationOn,
                        options = getBlocksForDistrict(district)
                    )
                }
            }
        }
        
        // Contact Preference Card
        ModernCard(
            title = "Contact Preferences",
            icon = Icons.Default.Contacts
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = contactPreference,
                        onClick = { onContactPreferenceChange(true) }
                    )
                    Text(
                        text = "Allow contact",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !contactPreference,
                        onClick = { onContactPreferenceChange(false) }
                    )
                    Text(
                        text = "Don't allow contact",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(cancelButton)
            }
            
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (isFirstLogin) completeProfileButton else saveChangesButton)
            }
        }
    }
}

@Composable
private fun ViewProfileCard(
    name: String,
    mobileNumber: String,
    postLevel: String,
    designation: String,
    subject: String,
    qualification: String,
    school: String,
    district: String,
    block: String,
    contactPreference: Boolean,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header Card
        ModernCard(
            title = "Profile Information",
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow("Name", name)
                ProfileInfoRow("Mobile", mobileNumber)
                ProfileInfoRow("Post Level", postLevel)
                ProfileInfoRow("Designation", designation)
                ProfileInfoRow("Subject", subject)
                ProfileInfoRow("Qualification", qualification)
            }
        }
        
        // School Information Card
        ModernCard(
            title = "School Information",
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow("School", school)
                ProfileInfoRow("District", district)
                ProfileInfoRow("Block", block)
            }
        }
        
        // Contact Preference Card
        ModernCard(
            title = "Contact Preferences",
            icon = Icons.Default.Contacts
        ) {
            ProfileInfoRow(
                "Contact Allowed",
                if (contactPreference) "Yes" else "No"
            )
        }
        
        // Edit Button
        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Edit Profile")
        }
    }
}

@Composable
private fun ModernCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Card Content
            content()
        }
    }
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        readOnly = readOnly,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.ifEmpty { "Not set" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )
    }
}

private fun getValidDesignations(postLevel: String): List<String> {
    return when (postLevel) {
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

private fun getBiharDistricts(): List<String> {
    return listOf(
        "Araria", "Arwal", "Aurangabad", "Banka", "Begusarai", "Bhagalpur",
        "Bhojpur", "Buxar", "Darbhanga", "East Champaran", "Gaya", "Gopalganj",
        "Jamui", "Jehanabad", "Kaimur", "Katihar", "Khagaria", "Kishanganj",
        "Lakhisarai", "Madhepura", "Madhubani", "Munger", "Muzaffarpur", "Nalanda",
        "Nawada", "Patna", "Purnia", "Rohtas", "Saharsa", "Samastipur",
        "Saran", "Sheikhpura", "Sheohar", "Sitamarhi", "Siwan", "Supaul",
        "Vaishali", "West Champaran"
    )
}

private fun getBlocksForDistrict(district: String): List<String> {
    // This would typically come from a data source
    // For now, returning a sample list
    return listOf("Block 1", "Block 2", "Block 3", "Block 4", "Block 5")
}

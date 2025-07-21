package com.shikshak.transfer.ui.theme.profile

import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.Contact
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.shikshak.transfer.ui.theme.utils.LanguageUtils

// Data classes for bilingual support
data class PostLevel(
    val english: String,
    val hindi: String,
    val designations: List<Designation>,
    val subjects: List<Subject>
)

data class Designation(
    val english: String,
    val hindi: String
)

data class Subject(
    val english: String,
    val hindi: String
)

// Bilingual data structure
private val postLevelsData = listOf(
    PostLevel(
        english = "Primary",
        hindi = "प्राथमिक (कक्षा 1 से 5)",
        designations = listOf(
            Designation("Primary Teacher (PRT)", "प्राथमिक शिक्षक (PRT)"),
            Designation("Headmaster", "प्रधानाध्यापक"),
            Designation("Special Education Teacher", "विशेष शिक्षा शिक्षक")
        ),
        subjects = listOf(
            Subject("All Subjects", "सभी विषय"),
            Subject("Hindi", "हिंदी"),
            Subject("English", "अंग्रेज़ी"),
            Subject("Mathematics", "गणित"),
            Subject("Environmental Studies", "पर्यावरण अध्ययन"),
            Subject("General Science", "सामान्य विज्ञान"),
            Subject("Social Studies", "सामाजिक अध्ययन"),
            Subject("Urdu", "उर्दू"),
            Subject("Bengali", "बंगाली"),
            Subject("Sanskrit", "संस्कृत"),
            Subject("Physical Education", "शारीरिक शिक्षा"),
            Subject("Music", "संगीत"),
            Subject("Fine Arts", "चित्रकला"),
            Subject("Dance", "नृत्य")
        )
    ),
    PostLevel(
        english = "Upper Primary",
        hindi = "उच्च प्राथमिक (कक्षा 6 से 8)",
        designations = listOf(
            Designation("Trained Graduate Teacher (TGT)", "प्रशिक्षित स्नातक शिक्षक (TGT)"),
            Designation("Headmaster", "प्रधानाध्यापक"),
            Designation("Physical Education Teacher (PT)", "शारीरिक शिक्षा शिक्षक (PT)"),
            Designation("Special Education Teacher", "विशेष शिक्षा शिक्षक")
        ),
        subjects = listOf(
            Subject("Hindi", "हिंदी"),
            Subject("English", "अंग्रेज़ी"),
            Subject("Mathematics", "गणित"),
            Subject("Science", "विज्ञान"),
            Subject("Social Science", "सामाजिक विज्ञान"),
            Subject("Urdu", "उर्दू"),
            Subject("Bengali", "बंगाली"),
            Subject("Sanskrit", "संस्कृत"),
            Subject("Physical Education", "शारीरिक शिक्षा"),
            Subject("Music", "संगीत"),
            Subject("Fine Arts", "चित्रकला"),
            Subject("Dance", "नृत्य")
        )
    ),
    PostLevel(
        english = "Secondary",
        hindi = "माध्यमिक (कक्षा 9 से 10)",
        designations = listOf(
            Designation("Trained Graduate Teacher (TGT)", "प्रशिक्षित स्नातक शिक्षक (TGT)"),
            Designation("Headmaster", "प्रधानाध्यापक"),
            Designation("Physical Education Teacher (PT)", "शारीरिक शिक्षा शिक्षक (PT)"),
            Designation("Special Education Teacher", "विशेष शिक्षा शिक्षक")
        ),
        subjects = listOf(
            Subject("Hindi", "हिंदी"),
            Subject("English", "अंग्रेज़ी"),
            Subject("Mathematics", "गणित"),
            Subject("Physics", "भौतिकी"),
            Subject("Chemistry", "रसायन विज्ञान"),
            Subject("Biology", "जीवविज्ञान"),
            Subject("History", "इतिहास"),
            Subject("Geography", "भूगोल"),
            Subject("Civics", "नागरिक शास्त्र"),
            Subject("Economics", "अर्थशास्त्र"),
            Subject("Urdu", "उर्दू"),
            Subject("Bengali", "बंगाली"),
            Subject("Sanskrit", "संस्कृत"),
            Subject("Physical Education", "शारीरिक शिक्षा"),
            Subject("Music", "संगीत"),
            Subject("Fine Arts", "चित्रकला"),
            Subject("Dance", "नृत्य")
        )
    ),
    PostLevel(
        english = "Senior Secondary",
        hindi = "उच्च माध्यमिक (+2, कक्षा 11 से 12)",
        designations = listOf(
            Designation("Post Graduate Teacher (PGT)", "स्नातकोत्तर शिक्षक (PGT)"),
            Designation("Headmaster", "प्रधानाध्यापक"),
            Designation("Physical Education Teacher (PT)", "शारीरिक शिक्षा शिक्षक (PT)"),
            Designation("Special Education Teacher", "विशेष शिक्षा शिक्षक")
        ),
        subjects = listOf(
            Subject("Hindi", "हिंदी"),
            Subject("English", "अंग्रेज़ी"),
            Subject("Mathematics", "गणित"),
            Subject("Physics", "भौतिकी"),
            Subject("Chemistry", "रसायन विज्ञान"),
            Subject("Biology", "जीवविज्ञान"),
            Subject("History", "इतिहास"),
            Subject("Political Science", "राजनीति विज्ञान"),
            Subject("Geography", "भूगोल"),
            Subject("Economics", "अर्थशास्त्र"),
            Subject("Psychology", "मनोविज्ञान"),
            Subject("Philosophy", "दर्शनशास्त्र"),
            Subject("Commerce", "वाणिज्य"),
            Subject("Computer Science", "कंप्यूटर विज्ञान"),
            Subject("Physical Education", "शारीरिक शिक्षा"),
            Subject("Music", "संगीत"),
            Subject("Fine Arts", "चित्रकला"),
            Subject("Dance", "नृत्य")
        )
    )
)

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
    var post by remember { mutableStateOf("") }
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
                post = teacher.post
                qualification = teacher.qualification
                contactPreference = teacher.contactPreference
                // Use phone number from Firebase Auth if teacher profile doesn't have it
                mobileNumber = if (teacher.contact.phone.isNotEmpty()) {
                    teacher.contact.phone
                } else {
                    currentUser?.phoneNumber ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
        Text(
                        text = stringResource(R.string.profile),
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
                        post = post,
                        onPostChange = { post = it },
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
                                name = name.trim(),
                                designation = designation.trim(),
                                subject = subject.trim(),
                                schoolName = school.trim(),
                                district = district.trim(),
                                block = block.trim(),
                                post = post.trim(),
                                qualification = qualification.trim(),
                                contactPreference = contactPreference,
                                contact = Contact(phone = mobileNumber.trim()),
                                preferredDistricts = emptyList(), // Remove preferredDistricts
                                preferredBlocks = emptyList() // Remove preferredBlocks
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
                        post = post,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileForm(
    name: String,
    onNameChange: (String) -> Unit,
    mobileNumber: String,
    post: String,
    onPostChange: (String) -> Unit,
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
    val context = LocalContext1.current
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun validateFields(): Boolean {
        return name.isNotBlank() &&
            post.isNotBlank() &&
            designation.isNotBlank() &&
            subject.isNotBlank() &&
            qualification.isNotBlank() &&
            school.isNotBlank() &&
            district.isNotBlank() &&
            block.isNotBlank()
    }

    fun getFirstError(): String {
        return when {
            name.isBlank() -> context.getString(R.string.validation_full_name_required)
            post.isBlank() -> context.getString(R.string.validation_post_level_required)
            designation.isBlank() -> context.getString(R.string.validation_designation_required)
            subject.isBlank() -> context.getString(R.string.validation_subject_required)
            qualification.isBlank() -> context.getString(R.string.validation_qualification_required)
            school.isBlank() -> context.getString(R.string.validation_school_required)
            district.isBlank() -> context.getString(R.string.validation_district_required)
            block.isBlank() -> context.getString(R.string.validation_block_required)
            else -> ""
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Personal Information Card
        ModernCard(
            title = stringResource(R.string.personal_information),
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.full_name_label),
                    icon = Icons.Default.Person
                )
                
                ModernTextField(
                    value = mobileNumber,
                    onValueChange = { },
                    label = stringResource(R.string.mobile_number_label),
                    icon = Icons.Default.Phone,
                    readOnly = true,
                    enabled = false
                )
            }
        }
        
        // Professional Information Card
        ModernCard(
            title = stringResource(R.string.professional_information),
            icon = Icons.Default.Work
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernDropdown(
                    value = post,
                    onValueChange = onPostChange,
                    label = stringResource(R.string.post_level_label),
                    icon = Icons.Default.School,
                    options = getPostLevelsShort()
                )
                
                if (post.isNotEmpty()) {
                    ModernDropdown(
                            value = designation,
                        onValueChange = onDesignationChange,
                        label = stringResource(R.string.designation_label),
                        icon = Icons.Default.Badge,
                        options = getValidDesignations(post, context)
                    )
                }
                
                ModernDropdown(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = stringResource(R.string.subject_label),
                    icon = Icons.Default.Book,
                    options = getValidSubjects(post, context)
                )
                
                ModernTextField(
                    value = qualification,
                    onValueChange = onQualificationChange,
                    label = stringResource(R.string.qualification_label),
                    icon = Icons.Default.School
                )
            }
        }
        
        // School Information Card
        ModernCard(
            title = stringResource(R.string.school_information),
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernTextField(
                    value = school,
                    onValueChange = onSchoolChange,
                    label = stringResource(R.string.current_school_name_label),
                    icon = Icons.Default.School
                )
                
                ModernDropdown(
                        value = district,
                    onValueChange = onDistrictChange,
                    label = stringResource(R.string.district_label),
                    icon = Icons.Default.LocationOn,
                    options = Blocks.getAllDistricts()
                )
                
                if (district.isNotEmpty()) {
                    val availableBlocks = Blocks.getBlocksForDistrict(district)
                    ModernDropdown(
                            value = block,
                        onValueChange = onBlockChange,
                        label = stringResource(R.string.block_label),
                        icon = Icons.Default.LocationOn,
                        options = availableBlocks
                    )
                }
            }
        }
        
        // Contact Preference Card
        ModernCard(
            title = stringResource(R.string.contact_preference_label),
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
                        text = stringResource(R.string.allow_contact),
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
                        text = stringResource(R.string.dont_allow_contact),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        // Action Buttons
        if (showError && errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
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
                onClick = {
                    if (validateFields()) {
                        showError = false
                        errorMessage = ""
                        onSave()
                    } else {
                        showError = true
                        errorMessage = getFirstError()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = validateFields()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewProfileCard(
    name: String,
    mobileNumber: String,
    post: String,
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
            title = stringResource(R.string.profile_information),
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(stringResource(R.string.name_label), name)
                ProfileInfoRow(stringResource(R.string.mobile_label), mobileNumber)
                ProfileInfoRow(stringResource(R.string.post_level_label), post)
                ProfileInfoRow(stringResource(R.string.designation_label), designation)
                ProfileInfoRow(stringResource(R.string.subject_label), subject)
                ProfileInfoRow(stringResource(R.string.qualification_label), qualification)
            }
        }
        
        // School Information Card
        ModernCard(
            title = stringResource(R.string.school_information),
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(stringResource(R.string.school_label), school)
                ProfileInfoRow(stringResource(R.string.district_label), district)
                ProfileInfoRow(stringResource(R.string.block_label), block)
            }
        }
        
        // Contact Preference Card
        ModernCard(
            title = stringResource(R.string.contact_preference_label),
            icon = Icons.Default.Contacts
        ) {
            ProfileInfoRow(
                stringResource(R.string.contact_allowed_label),
                if (contactPreference) stringResource(R.string.yes) else stringResource(R.string.no)
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
            Text(stringResource(R.string.edit_profile_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
fun ModernDropdown(
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
            text = value.ifEmpty { stringResource(R.string.not_set) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )
    }
}

private fun getValidDesignations(post: String, context: android.content.Context): List<String> {
    val isHindi = LanguageUtils.isHindi(context)
    val postLevelData = postLevelsData.find { 
        it.english == post || it.hindi == post 
    }
    
    return postLevelData?.designations?.map { designation ->
        if (isHindi) designation.hindi else designation.english
    } ?: emptyList()
}

private fun getPostLevels(context: android.content.Context): List<String> {
    val isHindi = LanguageUtils.isHindi(context)
    return postLevelsData.map { postLevel ->
        if (isHindi) postLevel.hindi else postLevel.english
    }
}

private fun getPostLevelsShort(): List<String> {
    return listOf("Primary", "Upper Primary", "Secondary", "Senior Secondary")
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

private fun getValidSubjects(post: String, context: android.content.Context): List<String> {
    val isHindi = LanguageUtils.isHindi(context)
    val postLevelData = postLevelsData.find { 
        it.english == post || it.hindi == post 
    }
    
    return postLevelData?.subjects?.map { subject ->
        if (isHindi) subject.hindi else subject.english
    } ?: emptyList()
}

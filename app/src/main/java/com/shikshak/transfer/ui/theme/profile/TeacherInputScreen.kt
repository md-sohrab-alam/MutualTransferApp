package com.shikshak.transfer.ui.theme.profile

import Teacher
import Contact
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherInputScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onFormSubmitted: (Teacher) -> Unit
) {
    // State for form fields
    var fullName by remember { mutableStateOf("") }
    var currentSchool by remember { mutableStateOf("") }
    var currentDistrict by remember { mutableStateOf("") }
    var currentBlock by remember { mutableStateOf("") }
    var postLevel by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var preferredDistricts by remember { mutableStateOf(listOf<String>()) }
    var willingToMove by remember { mutableStateOf(false) }
    var contactPreference by remember { mutableStateOf(true) }
    var designation by remember { mutableStateOf("") }
    
    // Function to validate form
    fun validateForm(): Boolean {
        return fullName.isNotBlank() && 
               currentSchool.isNotBlank() && 
               currentDistrict.isNotBlank() && 
               postLevel.isNotBlank() &&
               designation.isNotBlank() &&
               (if (postLevel == "Secondary" || postLevel == "Higher Secondary") subject.isNotBlank() else true) &&
               preferredDistricts.isNotEmpty()
    }
    
    // Function to get validation error message
    fun getValidationError(): String {
        return when {
            fullName.isBlank() -> "Full name is required"
            currentSchool.isBlank() -> "School name is required"
            currentDistrict.isBlank() -> "Current district is required"
            postLevel.isBlank() -> "Post level is required"
            designation.isBlank() -> "Designation is required"
            (postLevel == "Secondary" || postLevel == "Higher Secondary") && subject.isBlank() -> "Subject is required for Secondary and Higher Secondary"
            preferredDistricts.isEmpty() -> "Please select at least one preferred district"
            else -> ""
        }
    }
    
    // Dropdown options
    val districts = listOf(
        "Ahmedabad", "Amreli", "Anand", "Aravalli", "Banaskantha", "Bharuch", 
        "Bhavnagar", "Botad", "Chhota Udaipur", "Dahod", "Dang", "Devbhoomi Dwarka",
        "Gandhinagar", "Gir Somnath", "Jamnagar", "Junagadh", "Kheda", "Kutch",
        "Mahisagar", "Mehsana", "Morbi", "Narmada", "Navsari", "Panchmahal",
        "Patan", "Porbandar", "Rajkot", "Sabarkantha", "Surat", "Surendranagar",
        "Tapi", "Vadodara", "Valsad"
    )
    
    val blocks = listOf("Block A", "Block B", "Block C", "Block D", "Block E")
    
    val postLevels = listOf(
        "Primary" to false,
        "Upper Primary" to false,
        "Secondary" to true,
        "Higher Secondary" to true
    )
    
    // Subject lists for different post levels
    val secondarySubjects = listOf(
        "Hindi", "English", "Urdu", "Sanskrit", "Maithili",
        "Mathematics", "Science", "Social Science",
        "History", "Geography", "Civics", "Economics",
        "Home Science", "Commerce", "Music", "Physical Education",
        "Arts", "Computer Science / IT", "Arabic", "Persian"
    )
    
    val higherSecondarySubjects = listOf(
        // Compulsory
        "Hindi", "English", "Urdu",
        
        // Science
        "Physics", "Chemistry", "Mathematics", "Biology", 
        "Computer Science / IT", "Environmental Science",

        // Commerce
        "Accountancy", "Business Studies", "Economics", 
        "Entrepreneurship", "Informatics Practices",

        // Arts
        "History", "Political Science", "Geography", "Psychology", 
        "Sociology", "Philosophy", "Home Science", "Music", 
        "Hindi Literature", "Urdu Literature", "Sanskrit", 
        "Maithili", "Arabic", "Persian", "Painting / Fine Arts"
    )
    
    // Function to get subjects based on post level
    fun getSubjectsForPostLevel(level: String): List<String> {
        return when (level) {
            "Secondary" -> secondarySubjects
            "Higher Secondary" -> higherSecondarySubjects
            else -> emptyList()
        }
    }
    
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
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    val mobileNumber = currentUser?.phoneNumber ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Title
        Text(
            text = "🔄 Mutual Transfer Form",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("👤 Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Mobile Number (Read-only)
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { },
            label = { Text("📞 Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Current School Name
        OutlinedTextField(
            value = currentSchool,
            onValueChange = { currentSchool = it },
            label = { Text("🏫 Current School Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Current District Dropdown
        var currentDistrictExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = currentDistrictExpanded,
            onExpandedChange = { currentDistrictExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentDistrict,
                onValueChange = { },
                readOnly = true,
                label = { Text("📍 Current District") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currentDistrictExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = currentDistrictExpanded,
                onDismissRequest = { currentDistrictExpanded = false }
            ) {
                districts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district) },
                        onClick = {
                            currentDistrict = district
                            currentDistrictExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Current Block Dropdown
        var currentBlockExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = currentBlockExpanded,
            onExpandedChange = { currentBlockExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentBlock,
                onValueChange = { },
                readOnly = true,
                label = { Text("📍 Current Block") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currentBlockExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = currentBlockExpanded,
                onDismissRequest = { currentBlockExpanded = false }
            ) {
                blocks.forEach { block ->
                    DropdownMenuItem(
                        text = { Text(block) },
                        onClick = {
                            currentBlock = block
                            currentBlockExpanded = false
                        }
                    )
                }
            }
        }
        
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
                postLevels.forEach { (level, _) ->
                    DropdownMenuItem(
                        text = { Text(level) },
                        onClick = {
                            val previousPostLevel = postLevel
                            postLevel = level
                            postLevelExpanded = false
                            
                            // Clear subject if post level doesn't require it or if subject is not valid for new level
                            if (!postLevels.find { it.first == level }?.second!!) {
                                subject = ""
                            } else if (previousPostLevel != level) {
                                // Check if current subject is valid for new post level
                                val validSubjects = getSubjectsForPostLevel(level)
                                if (!validSubjects.contains(subject)) {
                                    subject = ""
                                }
                            }
                            
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

        // Subject Dropdown (only if post level requires it)
        if (postLevels.find { it.first == postLevel }?.second == true) {
            var subjectExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("📘 Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false }
                ) {
                    getSubjectsForPostLevel(postLevel).forEach { subjectName ->
                        DropdownMenuItem(
                            text = { Text(subjectName) },
                            onClick = {
                                subject = subjectName
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Designation Dropdown
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

        // Preferred Districts (Multi-select)
        Text(
            text = "🎯 Preferred Districts (Multi-select)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(districts.take(10)) { district ->
                FilterChip(
                    selected = preferredDistricts.contains(district),
                    onClick = {
                        preferredDistricts = if (preferredDistricts.contains(district)) {
                            preferredDistricts - district
                        } else {
                            preferredDistricts + district
                        }
                    },
                    label = { Text(district) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Willing to move radio buttons
        Text(
            text = "🔁 Willing to move to partner's school?",
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
                    selected = willingToMove,
                    onClick = { willingToMove = true }
                )
                Text("Yes")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = !willingToMove,
                    onClick = { willingToMove = false }
                )
                Text("No")
            }
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
        
        Spacer(modifier = Modifier.height(32.dp))

                    // Submit Button
            Button(
                onClick = {
                    if (validateForm()) {
                        val teacher = Teacher(
                            uid = currentUser?.uid ?: "",
                            name = fullName,
                            subject = subject,
                            post = postLevel,
                            district = currentDistrict,
                            block = currentBlock,
                            schoolName = currentSchool,
                            preferredDistrict = preferredDistricts.firstOrNull() ?: "",
                            preferredDistricts = preferredDistricts,
                            designation = designation,
                            contactPreference = contactPreference,
                            willingToMove = willingToMove,
                            contact = Contact(phone = mobileNumber),
                            timestamp = System.currentTimeMillis()
                        )
                        viewModel.saveTeacherProfile(teacher)
                        onFormSubmitted(teacher)
                    } else {
                        // Show error message if form is invalid
                        val errorMessage = getValidationError()
                        if (errorMessage.isNotBlank()) {
                            viewModel.showErrorDialog.value = true
                            viewModel.errorMessage.value = errorMessage
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
                Text("✅ Submit Form")
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
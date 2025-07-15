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
    
    val subjects = listOf(
        "Mathematics", "Science", "English", "Hindi", "Gujarati", 
        "Social Studies", "Computer Science", "Physical Education",
        "Arts", "Music", "Economics", "Commerce", "Accountancy"
    )
    
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
                            postLevel = level
                            postLevelExpanded = false
                            // Clear subject if post level doesn't require it
                            if (!postLevels.find { it.first == level }?.second!!) {
                                subject = ""
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
                    subjects.forEach { subjectName ->
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

        // Designation
        OutlinedTextField(
            value = designation,
            onValueChange = { designation = it },
            label = { Text("👨‍🏫 Designation") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
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
                if (validateForm(fullName, currentSchool, currentDistrict, postLevel)) {
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
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading.value
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

private fun validateForm(
    fullName: String,
    currentSchool: String,
    currentDistrict: String,
    postLevel: String
): Boolean {
    return fullName.isNotBlank() && 
           currentSchool.isNotBlank() && 
           currentDistrict.isNotBlank() && 
           postLevel.isNotBlank()
} 
package com.shikshak.transfer.ui.theme.profile

import Teacher
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext as LocalContext1

@Composable
fun TeacherProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onProfileSaved: (Teacher) -> Unit
) {
    val context = LocalContext1.current
    val teacher = remember { mutableStateOf(Teacher()) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = teacher.value.name,
            onValueChange = { teacher.value = teacher.value.copy(name = it) },
            label = { Text("Name") })
        OutlinedTextField(
            value = teacher.value.subject,
            onValueChange = { teacher.value = teacher.value.copy(subject = it) },
            label = { Text("Subject") })
        OutlinedTextField(
            value = teacher.value.post,
            onValueChange = { teacher.value = teacher.value.copy(post = it) },
            label = { Text("Post") })
        OutlinedTextField(
            value = teacher.value.district,
            onValueChange = { teacher.value = teacher.value.copy(district = it) },
            label = { Text("Current District") })
        OutlinedTextField(
            value = teacher.value.block,
            onValueChange = { teacher.value = teacher.value.copy(block = it) },
            label = { Text("Current Block") })
        OutlinedTextField(
            value = teacher.value.preferredDistrict,
            onValueChange = { teacher.value = teacher.value.copy(preferredDistrict = it) },
            label = { Text("Preferred District") })
        OutlinedTextField(
            value = teacher.value.preferredBlock,
            onValueChange = { teacher.value = teacher.value.copy(preferredBlock = it) },
            label = { Text("Preferred Block") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            viewModel.saveTeacherProfile(
                teacher.value.copy(
                    uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )
            )
            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
            onProfileSaved(teacher.value)
        }) {
            Text("Save Profile")
        }
    }
}

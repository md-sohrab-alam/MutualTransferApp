package com.shikshak.transfer.ui.theme.matchprofile

import Teacher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun MatchListScreen(
    currentTeacher: Teacher,
    viewModel: MatchViewModel = hiltViewModel(), 
    onGoToTeacherList: () -> Unit
) {
    val matches by remember { derivedStateOf { viewModel.matches } }

    LaunchedEffect(Unit) {
        viewModel.findMutualMatches(currentTeacher)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(matches) { teacher ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Name: ${teacher.name}")
                            Text("Subject: ${teacher.subject}")
                            Text("Post: ${teacher.post}")
                            Text("From: ${teacher.district} - ${teacher.block}")
                            Text("Wants: ${teacher.preferredDistrict} - ${teacher.preferredBlock}")
                            Text("Phone: ${FirebaseAuth.getInstance().currentUser?.phoneNumber}")
                        }
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

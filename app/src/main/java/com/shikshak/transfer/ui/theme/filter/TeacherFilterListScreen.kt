package com.shikshak.transfer.ui.theme.filter

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shikshak.transfer.ui.theme.utils.ErrorAlertDialog

@Composable
fun TeacherFilterListScreen(viewModel: FilterListViewModel = hiltViewModel()) {
    val filter = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            OutlinedTextField(
                value = filter.value,
                onValueChange = {
                    filter.value = it
                    viewModel.filterTeachers(it)
                },
                label = { Text("Filter by District/Subject/Post") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            if (viewModel.isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.filteredTeachers) { teacher ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Name: ${teacher.name}")
                                Text("Subject: ${teacher.subject}")
                                Text("Post: ${teacher.post}")
                                Text("District: ${teacher.district}")
                            }
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

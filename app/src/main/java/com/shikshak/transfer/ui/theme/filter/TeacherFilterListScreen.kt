package com.shikshak.transfer.ui.theme.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TeacherFilterListScreen(viewModel: FilterListViewModel = hiltViewModel()) {
    val filter = remember { mutableStateOf("") }

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

package com.shikshak.transfer.ui.theme.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.navigation.Routes
import com.shikshak.transfer.ui.theme.utils.MatchingService

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val teacher by viewModel.currentTeacher.collectAsState()
    val hasTransferRequest by viewModel.hasTransferRequest.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTeacherData()
        viewModel.checkTransferRequest()
    }
    
    // Refresh data when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (hasTransferRequest) {
        HomeScreenWithRequest(
            teacher = teacher,
            navController = navController,
            viewModel = viewModel
        )
    } else {
        HomeScreenFirstTime(
            teacher = teacher,
            navController = navController
        )
    }
}

@Composable
fun HomeScreenFirstTime(
    teacher: Teacher?,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "👋 ${stringResource(R.string.welcome_message)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                teacher?.let { t ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 ${t.schoolName}, ${t.district}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "🎯 ${t.designation}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // App Purpose Summary
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_purpose_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_purpose_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Primary Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ready_to_transfer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate(Routes.Request) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.submit_transfer_request))
                }
            }
        }
        
        // How It Works Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📌 ${stringResource(R.string.how_it_works_title)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.how_it_works_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // FAQ Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.faq_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.faq_content),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun HomeScreenWithRequest(
    teacher: Teacher?,
    navController: NavController,
    viewModel: HomeViewModel
) {
    val transferRequest by viewModel.transferRequest.collectAsState()
    val matches by viewModel.matches.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Back Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "👋 ${stringResource(R.string.welcome_back_message)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                teacher?.let { t ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📍 ${t.schoolName}, ${t.district}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "🎯 ${t.designation}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Transfer Request Summary
        transferRequest?.let { request ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📄 ${stringResource(R.string.your_transfer_request)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = { navController.navigate(Routes.Request) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.cancelRequest() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Cancel")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("────────────────────────────")
                    Text("| Name: ${request.teacherName}")
                    Text("| Post: ${request.designation}")
                    Text("| Subject: ${request.subject}")
                    Text("| From: ${request.currentDistrict}")
                    Text("| To: ${request.preferredDistricts.joinToString(", ")}")
                    Text("────────────────────────────")
                }
            }
        }
        
        // Matches Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🤝 ${stringResource(R.string.matching_requests)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (matches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    matches.forEach { match ->
                        MatchCard(
                            match = match,
                            onContact = { viewModel.contactMatch(match) },
                            currentRequest = transferRequest
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("😔 ${stringResource(R.string.no_matching_request_found)}")
                    Text(
                        text = stringResource(R.string.we_will_notify_you),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Teacher,
    onContact: () -> Unit,
    currentRequest: TransferRequest? = null
) {
    val matchScore = currentRequest?.let { MatchingService.calculateRelevanceScore(match, it) } ?: 0
    val matchQuality = MatchingService.getMatchQualityDescription(matchScore)
    val compatibilityDetails = currentRequest?.let { MatchingService.getMatchCompatibilityDetails(match, it) } ?: emptyList()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text("────────────────────────────")
            Text("| Name: ${match.name}")
            Text("| Post: ${match.designation}")
            Text("| Subject: ${match.subject}")
            Text("| From: ${match.district}")
            Text("| To: ${match.preferredDistricts.joinToString(", ")}")
            Text("| Match Quality: $matchQuality")
            Text("| [${stringResource(R.string.contact_now)}]")
            Text("────────────────────────────")
            
            // Show compatibility details if available
            if (compatibilityDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Compatibility:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                compatibilityDetails.forEach { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onContact,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(stringResource(R.string.contact_now))
            }
        }
    }
} 
package com.shikshak.transfer.ui.theme.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.data.Teacher
import com.shikshak.transfer.ui.theme.data.TransferRequest
import com.shikshak.transfer.ui.theme.utils.MatchingService
import com.shikshak.transfer.ui.theme.utils.ShareUtils
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    teacherId: String,
    navController: NavController,
    viewModel: ProfileDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val teacher by viewModel.teacher.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentRequest by viewModel.currentRequest.collectAsState()
    
    LaunchedEffect(teacherId) {
        viewModel.loadTeacherProfile(teacherId)
        viewModel.loadTeacherRequest(teacherId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${teacher?.name ?: ""} Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                CircularProgressIndicator()
            }
        } else {
            teacher?.let { teacherData ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    ProfileDetailContent(
                        teacher = teacherData,
                        transferRequest = currentRequest,
                        onContact = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${teacherData.contact.phone}")
                            }
                            context.startActivity(intent)
                        },
                        onShare = {
                            ShareUtils.shareTeacherProfile(context, teacherData)
                        },
                        onEmail = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${teacherData.contact.email}")
                                putExtra(Intent.EXTRA_SUBJECT, "Mutual Transfer Inquiry")
                                putExtra(Intent.EXTRA_TEXT, "Hello ${teacherData.name}, I'm interested in discussing a mutual transfer opportunity.")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileDetailContent(
    teacher: Teacher,
    transferRequest: TransferRequest?,
    onContact: () -> Unit,
    onShare: () -> Unit,
    onEmail: () -> Unit
) {
    val context = LocalContext.current
    val matchScore = transferRequest?.let { MatchingService.calculateRelevanceScore(teacher, it) } ?: 0
    val matchQuality = transferRequest?.let { MatchingService.getMatchQualityDescription(matchScore, context) } ?: ""
    val compatibilityDetails = transferRequest?.let { MatchingService.getMatchCompatibilityDetails(teacher, it) } ?: emptyList()
    
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header Card
        ModernCard(
            title = stringResource(R.string.teacher_profile),
            icon = Icons.Default.Person
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow("Name", teacher.name)
                ProfileInfoRow("Designation", teacher.designation)
                ProfileInfoRow("Subject", teacher.subject)
                ProfileInfoRow("Qualification", teacher.qualification)
                ProfileInfoRow("Current School", teacher.schoolName)
                ProfileInfoRow("District", teacher.district)
                ProfileInfoRow("Block", teacher.block)
                ProfileInfoRow(stringResource(R.string.contact_preference), if (teacher.contactPreference) stringResource(R.string.allow_contact) else stringResource(R.string.dont_allow_contact))
            }
        }
        
        // Transfer Preferences Card
        ModernCard(
            title = stringResource(R.string.transfer_preferences),
            icon = Icons.Default.LocationOn
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(stringResource(R.string.current_location), "${teacher.district}, ${teacher.block}")
                if (transferRequest?.preferredDistricts?.isNotEmpty() == true) {
                    ProfileInfoRow(stringResource(R.string.preferred_districts), transferRequest.preferredDistricts.joinToString(", "))
                }
                if (transferRequest?.preferredBlocks?.isNotEmpty() == true) {
                    ProfileInfoRow(stringResource(R.string.preferred_blocks), transferRequest.preferredBlocks.joinToString(", "))
                }
                ProfileInfoRow(stringResource(R.string.willing_to_move), if (teacher.willingToMove) "Yes" else "No")
            }
        }
        
        // Compatibility Score Card
        if (transferRequest != null) {
            ModernCard(
                title = stringResource(R.string.match_compatibility),
                icon = Icons.Default.Star
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.compatibility_score),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = matchQuality,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    if (compatibilityDetails.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.matching_factors),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        compatibilityDetails.forEach { detail ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Contact Information Card
        ModernCard(
            title = stringResource(R.string.contact_information),
            icon = Icons.Default.Contacts
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow("Phone", teacher.contact.phone)
                if (teacher.contact.email.isNotEmpty()) {
                    ProfileInfoRow("Email", teacher.contact.email)
                }
            }
        }
        
        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contact Button
            Button(
                onClick = onContact,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.call_teacher))
            }
            
            // Email Button (if email available)
            if (teacher.contact.email.isNotEmpty()) {
                OutlinedButton(
                    onClick = onEmail,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.send_email))
                }
            }
            
            // Share Button
            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.share_profile))
            }
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
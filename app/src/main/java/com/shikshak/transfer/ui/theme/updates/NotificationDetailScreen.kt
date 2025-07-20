package com.shikshak.transfer.ui.theme.updates

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notification: NotificationItem,
    onBackClick: () -> Unit,
    onMarkAsRead: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Mark as read when screen opens
    LaunchedEffect(notification.id) {
        if (!notification.isRead) {
            onMarkAsRead(notification.id)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Image if available
            notification.imageUri?.let { imageUri ->
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Notification image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Timestamp
                Text(
                    text = formatDate(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Full content or message with clickable links
                val content = notification.fullContent ?: notification.message
                ClickableTextContent(
                    text = content,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Additional data links if available
                if (notification.data.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Additional Information:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    notification.data.forEach { (key, value) ->
                        if (isUrl(value)) {
                            LinkItem(
                                title = key.replace("_", " ").capitalize(),
                                url = value,
                                onLinkClick = { url ->
                                    openUrl(context, url)
                                }
                            )
                        }
                    }
                }
                
                // Main link button if available
                notification.linkUrl?.let { url ->
                    Spacer(modifier = Modifier.height(16.dp))
                    LinkButton(
                        url = url,
                        onLinkClick = { openUrl(context, url) }
                    )
                }

            }
        }
    }
}

@Composable
private fun ClickableTextContent(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val links = extractLinks(text)
    
    if (links.isEmpty()) {
        // No links found, show as regular text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier
        )
    } else {
        // Show text with clickable links
        Column(modifier = modifier) {
            var currentIndex = 0
            links.forEach { link ->
                // Text before the link
                if (link.start > currentIndex) {
                    Text(
                        text = text.substring(currentIndex, link.start),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                // Clickable link
                Text(
                    text = link.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        openUrl(context, link.url)
                    }
                )
                
                currentIndex = link.end
            }
            
            // Remaining text after last link
            if (currentIndex < text.length) {
                Text(
                    text = text.substring(currentIndex),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun LinkItem(
    title: String,
    url: String,
    onLinkClick: (String) -> Unit
) {
    val linkType = getLinkType(url)
    val icon = when (linkType) {
        LinkType.YOUTUBE -> Icons.Default.PlayArrow
        LinkType.FILE -> Icons.Default.Download
        else -> Icons.Default.Link
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Link icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { onLinkClick(url) }
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "Open link"
                )
            }
        }
    }
}

@Composable
private fun LinkButton(
    url: String,
    onLinkClick: (String) -> Unit
) {
    val linkType = getLinkType(url)
    val buttonText = when (linkType) {
        LinkType.YOUTUBE -> "Watch on YouTube"
        LinkType.FILE -> "Download File"
        else -> "Open Link"
    }
    
    val icon = when (linkType) {
        LinkType.YOUTUBE -> Icons.Default.PlayArrow
        LinkType.FILE -> Icons.Default.Download
        else -> Icons.Default.OpenInNew
    }
    
    Button(
        onClick = { onLinkClick(url) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = "Link icon",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(buttonText)
    }
}

private data class LinkInfo(
    val text: String,
    val url: String,
    val start: Int,
    val end: Int
)

private enum class LinkType {
    YOUTUBE, FILE, GENERAL
}

private fun extractLinks(text: String): List<LinkInfo> {
    val links = mutableListOf<LinkInfo>()
    
    // URL pattern
    val urlPattern = Pattern.compile(
        "https?://[\\w\\d\\-._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=.%]+",
        Pattern.CASE_INSENSITIVE
    )
    
    val matcher = urlPattern.matcher(text)
    while (matcher.find()) {
        val url = matcher.group()
        links.add(
            LinkInfo(
                text = url,
                url = url,
                start = matcher.start(),
                end = matcher.end()
            )
        )
    }
    
    return links
}

private fun isUrl(text: String): Boolean {
    return text.startsWith("http://") || text.startsWith("https://")
}

private fun getLinkType(url: String): LinkType {
    return when {
        url.contains("youtube.com") || url.contains("youtu.be") -> LinkType.YOUTUBE
        url.contains(".pdf") || url.contains(".doc") || url.contains(".xls") || 
        url.contains(".ppt") || url.contains(".zip") || url.contains(".rar") -> LinkType.FILE
        else -> LinkType.GENERAL
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle invalid URL or no app to handle the URL
        android.widget.Toast.makeText(
            context,
            "Cannot open link: $url",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatDate(timestamp: String?): String {
    val timestampLong = timestamp?.toLongOrNull() ?: System.currentTimeMillis()
    val date = Date(timestampLong)
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
} 
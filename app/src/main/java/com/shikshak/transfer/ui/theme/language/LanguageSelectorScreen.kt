package com.shikshak.transfer.ui.theme.language

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.utils.LanguageUtils
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorScreen(
    onLanguageSelected: () -> Unit
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(LanguageUtils.getCurrentLanguage(context)) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon
        Text(
            text = "🎓",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Title
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Subtitle
        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Language Selection Title
        Text(
            text = "Choose Language / भाषा चुनें",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Language Options
        LanguageUtils.getSupportedLanguages().forEach { language ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedLanguage == language.code) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                onClick = {
                    selectedLanguage = language.code
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Flag/Icon
                    Text(
                        text = when (language.code) {
                            "en" -> "🇺🇸"
                            "hi" -> "🇮🇳"
                            else -> "🌐"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    
                    // Language Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = language.nativeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = language.englishName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Selection Indicator
                    if (selectedLanguage == language.code) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Continue Button
        Button(
            onClick = {
                // Save language preference
                val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("language_code", selectedLanguage).apply()
                
                // Set the selected language
                LanguageUtils.setLocale(context, selectedLanguage)
                onLanguageSelected()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Continue / जारी रखें",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer
        Text(
            text = stringResource(R.string.app_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 32.dp)
        )
    }
} 
package com.shikshak.transfer

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.shikshak.transfer.ui.theme.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val locale = getLocaleFromPreferences(newBase)
        val context = updateLocale(newBase, locale)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity created with locale: ${getLocaleFromPreferences(this).language}")
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
    
    private fun getLocaleFromPreferences(context: Context): Locale {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "en") ?: "en"
        Timber.d("Loading locale from preferences in MainActivity: $languageCode")
        
        // Debug: Check if the preference actually exists
        val allPrefs = sharedPrefs.all
        Timber.d("MainActivity - All preferences: $allPrefs")
        
        return when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "en" -> Locale("en", "US")
            else -> Locale(languageCode)
        }
    }
    
    private fun updateLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

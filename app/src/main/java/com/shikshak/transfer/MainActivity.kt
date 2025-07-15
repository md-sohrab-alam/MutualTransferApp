package com.shikshak.transfer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.shikshak.transfer.ui.theme.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        // Apply saved language preference
        val sharedPrefs = newBase?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs?.getString("language_code", "en") ?: "en"
        if (newBase != null) {
            com.shikshak.transfer.ui.theme.utils.LanguageUtils.setLocale(newBase, languageCode)
        }
    }
}

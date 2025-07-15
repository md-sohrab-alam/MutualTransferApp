package com.shikshak.transfer.ui.theme.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LanguageUtils {
    
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return context.createConfigurationContext(config)
    }
    
    fun getCurrentLanguage(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return locale.language
    }
    
    fun isHindi(context: Context): Boolean {
        return getCurrentLanguage(context) == "hi"
    }
    
    fun isEnglish(context: Context): Boolean {
        return getCurrentLanguage(context) == "en"
    }
    
    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "English"),
            Language("hi", "हिंदी", "Hindi")
        )
    }
}

data class Language(
    val code: String,
    val nativeName: String,
    val englishName: String
) 
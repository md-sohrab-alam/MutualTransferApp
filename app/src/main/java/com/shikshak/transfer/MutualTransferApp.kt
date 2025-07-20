package com.shikshak.transfer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.*

@HiltAndroidApp
class MutualTransferApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Timber initialization for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized in DEBUG mode")
        }
        updateLocale()
        Timber.d("App created with locale: ${getLocaleFromPreferences(this).language}")
    }

    override fun attachBaseContext(base: Context) {
        val locale = getLocaleFromPreferences(base)
        val context = updateLocale(base, locale)
        super.attachBaseContext(context)
    }

    override fun getResources(): android.content.res.Resources {
        val locale = getLocaleFromPreferences(this)
        val config = Configuration(super.getResources().configuration)
        config.setLocale(locale)
        return createConfigurationContext(config).resources
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val locale = getLocaleFromPreferences(this)
        val config = Configuration(overrideConfiguration)
        config.setLocale(locale)
        return super.createConfigurationContext(config)
    }

    private fun getLocaleFromPreferences(context: Context): Locale {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "en") ?: "en"
        Timber.d("Loading locale from preferences: $languageCode")
        
        // Debug: Check if the preference actually exists
        val allPrefs = sharedPrefs.all
        Timber.d("All preferences: $allPrefs")
        
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

    fun updateLocale() {
        val locale = getLocaleFromPreferences(this)
        Timber.d("Updating locale to: ${locale.language}")
        Locale.setDefault(locale)
        
        // Use the modern approach for updating configuration
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
    }
}
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

    private fun getLocaleFromPreferences(context: Context): Locale {
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "en") ?: "en"
        Timber.d("Loading locale from preferences: $languageCode")
        return Locale(languageCode)
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
        
        // Create a new context with the updated configuration
        val newContext = createConfigurationContext(config)
        
        // Update the base context
        try {
            val baseContextField = Context::class.java.getDeclaredField("mBase")
            baseContextField.isAccessible = true
            baseContextField.set(this, newContext)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update base context")
        }
    }
}
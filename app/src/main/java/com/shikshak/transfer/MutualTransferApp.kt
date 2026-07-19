package com.shikshak.transfer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.Locale

@HiltAndroidApp
class MutualTransferApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun attachBaseContext(base: Context) {
        // Only wrap here — do NOT override getResources()/createConfigurationContext()
        // (that pattern causes StackOverflowError on modern Android).
        super.attachBaseContext(wrapWithLocale(base))
    }

    companion object {
        fun wrapWithLocale(context: Context): Context {
            val languageCode = context
                .getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .getString("language_code", "en") ?: "en"
            val locale = when (languageCode) {
                "hi" -> Locale("hi", "IN")
                "en" -> Locale("en", "US")
                else -> Locale(languageCode)
            }
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        }
    }
}

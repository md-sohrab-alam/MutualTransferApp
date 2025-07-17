package com.shikshak.transfer

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.messaging.FirebaseMessaging
import com.shikshak.transfer.ui.theme.navigation.AppNavHost
import com.shikshak.transfer.ui.theme.navigation.Routes
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
        
        // Initialize FCM
        initializeFCM()
        
        // Check if this activity was launched from a notification
        val notificationType = intent.getStringExtra("notification_type")
        val notificationAction = intent.getStringExtra("notification_action")
        if (notificationType != null) {
            Timber.d("MainActivity launched from notification with type: $notificationType, action: $notificationAction")
        }
        
        // Check for pending notifications to save
        checkForPendingNotifications()
        
        setContent {
            MaterialTheme {
                AppNavHost(
                    initialNotificationType = notificationType,
                    initialNotificationAction = notificationAction
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Handle notification tap when app is already running
        val notificationType = intent.getStringExtra("notification_type")
        val notificationAction = intent.getStringExtra("notification_action")
        if (notificationType != null) {
            Timber.d("Notification tapped while app running, type: $notificationType, action: $notificationAction")
            // The AppNavHost will handle this through the shared state
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

    private fun initializeFCM() {
        Timber.d("Initializing FCM...")
        
        // Get current token
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Timber.d("FCM Token obtained in MainActivity: $token")
            
            // Force token refresh to ensure we have the latest token
            FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
                Timber.d("Old FCM token deleted, requesting new token...")
                FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
                    Timber.d("New FCM token after refresh: $newToken")
                    saveTokenToFirestore(newToken)
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to get new FCM token after refresh")
                }
            }.addOnFailureListener { exception ->
                Timber.e(exception, "Failed to delete old FCM token")
            }
        }.addOnFailureListener { exception ->
            Timber.e(exception, "Failed to get FCM token in MainActivity")
        }
    }
    
    private fun saveTokenToFirestore(token: String) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Timber.d("Saving FCM token to Firestore for user: $userId")
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Timber.d("FCM token saved to Firestore successfully")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to save FCM token to Firestore")
                }
        } else {
            Timber.w("Cannot save FCM token - no user ID available")
        }
    }

    private fun checkForPendingNotifications() {
        // This is a fallback mechanism to save notifications that might have been missed
        // when the app was closed
        Timber.d("Checking for pending notifications...")
        
        // You could implement a local storage mechanism to save notifications
        // when the FCM service can't run, and then process them here
    }
}

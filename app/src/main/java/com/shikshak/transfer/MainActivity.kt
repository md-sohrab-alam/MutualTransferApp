package com.shikshak.transfer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shikshak.transfer.services.FCMService
import com.shikshak.transfer.ui.theme.navigation.AppNavHost
import com.shikshak.transfer.ui.theme.utils.AppUpdateDialog
import com.shikshak.transfer.ui.theme.utils.AppUpdateUtils
import com.shikshak.transfer.utils.TopicManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "APP_UPDATE_AVAILABLE") {
                val isForceUpdate = intent.getBooleanExtra("is_force_update", false)
                val currentVersion = intent.getStringExtra("current_version") ?: ""
                val newVersion = intent.getStringExtra("new_version") ?: ""
                val updateMessage = intent.getStringExtra("update_message") ?: ""
                
                // Store update data in SharedPreferences for the Composable to read
                val sharedPrefs = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().apply {
                    putBoolean("show_update_dialog", true)
                    putBoolean("is_force_update", isForceUpdate)
                    putString("current_version", currentVersion)
                    putString("new_version", newVersion)
                    putString("update_message", updateMessage)
                    putLong("update_timestamp", System.currentTimeMillis())
                }.apply()
                
                Timber.d("App update dialog triggered - Force: $isForceUpdate, Current: $currentVersion, New: $newVersion")
            }
        }
    }
    
    override fun attachBaseContext(newBase: Context) {
        val locale = getLocaleFromPreferences(newBase)
        val context = updateLocale(newBase, locale)
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity created with locale: ${getLocaleFromPreferences(this).language}")
        
        // Register broadcast receiver for app updates
        registerReceiver(updateReceiver, IntentFilter("APP_UPDATE_AVAILABLE"), Context.RECEIVER_NOT_EXPORTED)
        
        // Initialize FCM
        initializeFCM()

        // Handle notification data from intent (background notifications)
        handleNotificationFromIntent(intent)
        
        setContent {
            MaterialTheme {
                // App update dialog state
                var showUpdateDialog by remember { mutableStateOf(false) }
                var updateDialogData by remember { mutableStateOf<UpdateDialogData?>(null) }
                
                // Check for app update dialog on each recomposition
                LaunchedEffect(Unit) {
                    checkForPendingAppUpdate { data ->
                        updateDialogData = data
                        showUpdateDialog = true
                    }
                }
                
                // App update dialog
                updateDialogData?.let { data ->
                    if (showUpdateDialog) {
                        AppUpdateDialog(
                            isForceUpdate = data.isForceUpdate,
                            currentVersion = data.currentVersion,
                            newVersion = data.newVersion,
                            updateMessage = data.updateMessage,
                            onUpdateClick = {
                                showUpdateDialog = false
                                AppUpdateUtils.openAppStore(this@MainActivity)
                            },
                            onLaterClick = {
                                showUpdateDialog = false
                                // Save that user chose "Later" to avoid showing again soon
                                saveUpdateLaterPreference()
                            },
                            onDismiss = {
                                showUpdateDialog = false
                            }
                        )
                    }
                }
                
                AppNavHost(
                    initialNotificationType = intent.getStringExtra("notification_type"),
                    initialNotificationAction = intent.getStringExtra("notification_action")
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateReceiver)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Handle notification tap when app is already running
        handleNotificationFromIntent(intent)
    }
    
    private fun checkForPendingAppUpdate(onUpdateFound: (UpdateDialogData) -> Unit) {
        val sharedPrefs = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        val showDialog = sharedPrefs.getBoolean("show_update_dialog", false)
        
        if (showDialog) {
            val isForceUpdate = sharedPrefs.getBoolean("is_force_update", false)
            val currentVersion = sharedPrefs.getString("current_version", "") ?: ""
            val newVersion = sharedPrefs.getString("new_version", "") ?: ""
            val updateMessage = sharedPrefs.getString("update_message", "") ?: ""
            val updateTimestamp = sharedPrefs.getLong("update_timestamp", 0L)
            
            // Check if the update info is recent (within 1 hour)
            val isRecent = (System.currentTimeMillis() - updateTimestamp) < 60 * 60 * 1000
            
            if (isRecent && currentVersion.isNotEmpty() && newVersion.isNotEmpty()) {
                val updateData = UpdateDialogData(
                    isForceUpdate = isForceUpdate,
                    currentVersion = currentVersion,
                    newVersion = newVersion,
                    updateMessage = updateMessage
                )
                
                // Clear the pending update flag
                sharedPrefs.edit().remove("show_update_dialog").apply()
                
                Timber.d("Showing pending app update dialog")
                onUpdateFound(updateData)
            } else {
                // Clear old update data
                sharedPrefs.edit().clear().apply()
            }
        }
    }
    
    private fun saveUpdateLaterPreference() {
        val sharedPrefs = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("update_later_timestamp", System.currentTimeMillis()).apply()
        Timber.d("User chose 'Later' for app update")
    }

    private fun handleNotificationFromIntent(intent: Intent) {
        Timber.d("=== HANDLING NOTIFICATION FROM INTENT ===")
        Timber.d("Intent action: ${intent.action}")
        Timber.d("Intent data: ${intent.data}")
        Timber.d("Intent extras: ${intent.extras}")

        // Check if this is a notification tap
        val bundle = intent.extras

        if (bundle != null) {
            // Check for app update notification
            val isAppUpdate = intent.getBooleanExtra("isAppUpdate", false)
            val isForceUpdate = intent.getBooleanExtra("isForceUpdate", false)
            val newVersion = intent.getStringExtra("version") ?: ""
            
            if (isAppUpdate && newVersion.isNotEmpty()) {
                handleAppUpdateFromIntent(isForceUpdate, newVersion, intent)
                return
            }
            
            // Extract all notification data from intent extras
            val title = intent.getStringExtra("title")
            if (title.isNullOrEmpty()) return
            
            val message = intent.getStringExtra("body")
            if (message.isNullOrEmpty()) return
            
            val notificationType = intent.getStringExtra("messageType") ?: "general"
            val imageUri = intent.getStringExtra("imageUrl")
            val linkUrl = intent.getStringExtra("linkUrl")
            val fullContent = intent.getStringExtra("fullContent")
            val timestamp = intent.getStringExtra("timestamp") ?: System.currentTimeMillis().toString()

            // Get user ID
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid

            if (userId != null) {
                Timber.d("Processing background notification for user: $userId")
                
                // Create data map from intent extras
                val data = mutableMapOf<String, String>()
                bundle.keySet().forEach { key ->
                    bundle.getString(key)?.let { value ->
                        data[key] = value
                    }
                }
                
                // Use FCMService method to save notification (includes duplicate checking)
                val fcmService = FCMService()
                fcmService.saveNotificationToFirestore(title, message, notificationType, data)
                
            } else {
                Timber.w("Cannot save background notification - no user ID available")
            }
        } else {
            Timber.d("No notification data found in intent")
        }
    }
    
    private fun handleAppUpdateFromIntent(isForceUpdate: Boolean, newVersion: String, intent: Intent) {
        val currentVersion = AppUpdateUtils.getCurrentVersion(this)
        val updateMessage = intent.getStringExtra("body") ?: "A new version of the app is available."
        
        Timber.d("Handling app update from intent - Force: $isForceUpdate, Current: $currentVersion, New: $newVersion")
        
        if (AppUpdateUtils.isUpdateNeeded(currentVersion, newVersion)) {
            // Store update data in SharedPreferences for the Composable to read
            val sharedPrefs = getSharedPreferences("app_update_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                putBoolean("show_update_dialog", true)
                putBoolean("is_force_update", isForceUpdate)
                putString("current_version", currentVersion)
                putString("new_version", newVersion)
                putString("update_message", updateMessage)
                putLong("update_timestamp", System.currentTimeMillis())
            }.apply()
            
            Timber.d("App update data saved to SharedPreferences")
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

                    // Initialize topic subscriptions after token is ready
                    initializeTopicSubscriptions()
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

    private fun initializeTopicSubscriptions() {
        Timber.d("Initializing topic subscriptions...")

        // Subscribe to default topics
        TopicManager.subscribeToDefaultTopics()

        // You can also subscribe to user-specific topics based on preferences
        // For example, if user has transfer requests, subscribe to transfer_requests topic
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Check if user has transfer requests and subscribe accordingly
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("transfer_requests")
                .whereEqualTo("teacherId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Timber.d("User has transfer requests, subscribing to transfer_requests topic")
                        TopicManager.subscribeToTopic(TopicManager.TOPIC_TRANSFER_REQUESTS)
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to check user transfer requests")
                }
        }
    }
    
    private fun saveTokenToFirestore(token: String) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        
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
}

data class UpdateDialogData(
    val isForceUpdate: Boolean,
    val currentVersion: String,
    val newVersion: String,
    val updateMessage: String
)

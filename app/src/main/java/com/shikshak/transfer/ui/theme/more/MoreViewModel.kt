package com.shikshak.transfer.ui.theme.more

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shikshak.transfer.ui.theme.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor() : BaseViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    
    // State for about dialog
    private val _showAboutDialog = mutableStateOf(false)
    val showAboutDialog: State<Boolean> = _showAboutDialog
    
    fun showAboutDialog() {
        _showAboutDialog.value = true
    }
    
    fun hideAboutDialog() {
        _showAboutDialog.value = false
    }
    
    fun logout(context: Context, onSuccess: () -> Unit) {
        updateLoadingState(true)
        
        // Clear all user-specific data
        clearUserData(context)
        
        // Sign out from Firebase Auth
        auth.signOut()
        updateLoadingState(false)
        onSuccess()
    }
    
    fun restartApp(context: Context) {
        // This will trigger a complete app restart
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    private fun clearUserData(context: Context) {
        // Get current language setting before clearing
        val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentLanguage = sharedPrefs.getString("language_code", "en")

        // Clear all SharedPreferences
        sharedPrefs.edit().clear().commit() // Use commit to ensure it's written immediately

        // Restore language setting immediately
        sharedPrefs.edit().putString("language_code", currentLanguage).commit()

        // Debug log
        android.util.Log.d("MoreViewModel", "Restored language_code after clearing: $currentLanguage")
        // You can add more cleanup here as needed
    }
} 
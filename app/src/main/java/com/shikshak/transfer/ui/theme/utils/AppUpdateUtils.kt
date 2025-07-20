package com.shikshak.transfer.ui.theme.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import timber.log.Timber

object AppUpdateUtils {
    
    /**
     * Get current app version name
     */
    fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            Timber.e(e, "Error getting current app version")
            "1.0.0"
        }
    }
    
    /**
     * Get current app version code
     */
    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: Exception) {
            Timber.e(e, "Error getting current app version code")
            1
        }
    }
    
    /**
     * Compare version strings (e.g., "1.2.3" vs "1.2.4")
     * Returns: -1 if current < new, 0 if equal, 1 if current > new
     */
    fun compareVersions(currentVersion: String, newVersion: String): Int {
        return try {
            val currentParts = currentVersion.split(".").map { it.toInt() }
            val newParts = newVersion.split(".").map { it.toInt() }
            
            val maxLength = maxOf(currentParts.size, newParts.size)
            
            for (i in 0 until maxLength) {
                val current = if (i < currentParts.size) currentParts[i] else 0
                val new = if (i < newParts.size) newParts[i] else 0
                
                when {
                    current < new -> return -1
                    current > new -> return 1
                }
            }
            0 // Versions are equal
        } catch (e: Exception) {
            Timber.e(e, "Error comparing versions: $currentVersion vs $newVersion")
            0
        }
    }
    
    /**
     * Check if update is needed based on version comparison
     */
    fun isUpdateNeeded(currentVersion: String, newVersion: String): Boolean {
        return compareVersions(currentVersion, newVersion) < 0
    }
    
    /**
     * Check if force update is required for current version
     */
    fun isForceUpdateRequired(
        currentVersion: String, 
        newVersion: String, 
        forceUpdateVersion: String
    ): Boolean {
        // If current version is <= forceUpdateVersion, force update is required
        return compareVersions(currentVersion, forceUpdateVersion) <= 0
    }
    
    /**
     * Open app store for update
     */
    fun openAppStore(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening app store")
            // Fallback to Play Store web URL
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Timber.e(e2, "Error opening Play Store web URL")
                Toast.makeText(
                    context, 
                    "Please update the app from Google Play Store", 
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    /**
     * Parse version string to extract major, minor, patch
     */
    fun parseVersion(version: String): Triple<Int, Int, Int> {
        return try {
            val parts = version.split(".").map { it.toInt() }
            Triple(
                parts.getOrNull(0) ?: 0,
                parts.getOrNull(1) ?: 0,
                parts.getOrNull(2) ?: 0
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing version: $version")
            Triple(0, 0, 0)
        }
    }
    
    /**
     * Format version for display
     */
    fun formatVersion(version: String): String {
        return "v$version"
    }
} 
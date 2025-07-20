package com.shikshak.transfer.ui.theme.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.shikshak.transfer.R
import com.shikshak.transfer.ui.theme.data.Teacher
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    
    fun shareTeacherProfile(context: Context, teacher: Teacher) {
        try {
            // Create deep link for the teacher profile
            val deepLink = createDeepLink(teacher.uid)
            
            // Create share message
            val shareMessage = createShareMessage(teacher, deepLink)
            
            // Create screenshot of the profile (simplified for now)
            // In a real implementation, you would capture the actual profile view
            val screenshotUri = createProfileScreenshot(context, teacher)
            
            // Share via WhatsApp
            shareViaWhatsApp(context, shareMessage, screenshotUri)
            
        } catch (e: Exception) {
            Timber.e(e, "Error sharing teacher profile")
            // Fallback to simple text sharing
            shareSimpleText(context, teacher)
        }
    }
    
    private fun createDeepLink(teacherId: String): String {
        // In a real implementation, you would use Firebase Dynamic Links
        // For now, we'll use a simple deep link structure that will:
        // 1. Try to open the app with the profile
        // 2. If app is not installed, redirect to Play Store
        return "https://mutualtransfer.app/profile?uid=$teacherId"
    }
    
    private fun getPlayStoreLink(): String {
        return "https://play.google.com/store/apps/details?id=com.shikshak.transfer"
    }
    
    private fun createShareMessage(teacher: Teacher, deepLink: String): String {
        val playStoreLink = getPlayStoreLink()
        return """
            🤝 Mutual Transfer Match Found! 📚
            
            👤 ${teacher.name}
            🏫 ${teacher.schoolName}
            📍 ${teacher.district}
            📚 ${teacher.subject}
            🎯 ${teacher.designation}
            
            View this teacher's profile on Mutual Transfer App:
            $deepLink
            
            📱 Download from Play Store: $playStoreLink
            
            #MutualTransfer #TeacherTransfer
        """.trimIndent()
    }
    
    private fun createProfileScreenshot(context: Context, teacher: Teacher): Uri? {
        return try {
            // Create a simple profile card bitmap
            val bitmap = createProfileCardBitmap(context, teacher)
            
            // Save bitmap to file
            val file = File(context.cacheDir, "profile_${teacher.uid}.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            // Get content URI for sharing
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating profile screenshot")
            null
        }
    }
    
    private fun createProfileCardBitmap(context: Context, teacher: Teacher): Bitmap {
        // Create a simple bitmap with profile information
        // In a real implementation, you would render the actual Compose UI
        val width = 400
        val height = 600
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Draw background
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFFFFF")
            style = android.graphics.Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Draw text
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#000000")
            textSize = 24f
            isAntiAlias = true
        }
        
        val titlePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#1976D2")
            textSize = 32f
            isAntiAlias = true
            isFakeBoldText = true
        }
        
        // Draw title
        canvas.drawText(context.getString(R.string.teacher_profile), 20f, 50f, titlePaint)
        
        // Draw profile information
        var yPosition = 100f
        val lineHeight = 40f
        
        canvas.drawText("${context.getString(R.string.name_label)}: ${teacher.name}", 20f, yPosition, textPaint)
        yPosition += lineHeight
        
        canvas.drawText("${context.getString(R.string.school_label)}: ${teacher.schoolName}", 20f, yPosition, textPaint)
        yPosition += lineHeight
        
        canvas.drawText("${context.getString(R.string.district_label)}: ${teacher.district}", 20f, yPosition, textPaint)
        yPosition += lineHeight
        
        canvas.drawText("${context.getString(R.string.subject_label)}: ${teacher.subject}", 20f, yPosition, textPaint)
        yPosition += lineHeight
        
        canvas.drawText("${context.getString(R.string.designation_label)}: ${teacher.designation}", 20f, yPosition, textPaint)
        yPosition += lineHeight
        
        // Draw footer
        val footerPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#666666")
            textSize = 18f
            isAntiAlias = true
        }
        
        canvas.drawText(context.getString(R.string.shared_via_app), 20f, height - 50f, footerPaint)
        
        return bitmap
    }
    
    private fun shareViaWhatsApp(context: Context, message: String, imageUri: Uri?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, message)
            imageUri?.let { putExtra(Intent.EXTRA_STREAM, it) }
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.mutual_transfer_match))
            
            // Try to open WhatsApp specifically
            setPackage("com.whatsapp")
            
            // Add flags for sharing
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Check if WhatsApp is available
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to general sharing
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
        }
    }
    
    private fun shareSimpleText(context: Context, teacher: Teacher) {
        val playStoreLink = getPlayStoreLink()
        val message = """
            🤝 Mutual Transfer Match Found! 📚
            
            👤 ${teacher.name}
            🏫 ${teacher.schoolName}
            📍 ${teacher.district}
            📚 ${teacher.subject}
            🎯 ${teacher.designation}
            
            View this teacher's profile on Mutual Transfer App:
            https://mutualtransfer.app/profile?uid=${teacher.uid}
            
            📱 Download from Play Store: $playStoreLink
            
            #MutualTransfer #TeacherTransfer
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.mutual_transfer_match))
            setPackage("com.whatsapp")
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
        }
    }
} 
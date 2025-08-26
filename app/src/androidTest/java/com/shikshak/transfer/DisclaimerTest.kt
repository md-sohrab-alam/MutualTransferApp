package com.shikshak.transfer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shikshak.transfer.data.Prefs
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DisclaimerTest {

    @Test
    fun testDisclaimerAcceptance() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = Prefs(context)
        
        // Test initial state
        assertFalse(prefs.isDisclaimerAccepted())
        
        // Test setting acceptance
        prefs.setDisclaimerAccepted(true)
        assertTrue(prefs.isDisclaimerAccepted())
        
        // Test setting back to false
        prefs.setDisclaimerAccepted(false)
        assertFalse(prefs.isDisclaimerAccepted())
    }
}

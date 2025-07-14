package com.shikshak.transfer // Or your actual package name

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MutualTransferApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber
        // Option 1: Simple setup (logs only in debug builds)

        if (BuildConfig.DEBUG) { // BuildConfig.DEBUG is automatically generated
            Timber.plant(Timber.DebugTree())
        } else {
            // For release builds, you might plant a tree that logs to your crash reporting tool
            // Example for Firebase Crashlytics (make sure you have Crashlytics set up)
            // Timber.plant(CrashlyticsTree())
            // Or plant no tree if you don't want logs in release:
            // Timber.plant(ReleaseTree()) // See ReleaseTree example below
        }

        // Option 2: More advanced setup with clickable logs (see notes below)
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(object : Timber.DebugTree() {
        //         override fun createStackElementTag(element: StackTraceElement): String? {
        //             return String.format(
        //                 "[(%s:%s)#%s]",
        //                 element.fileName,
        //                 element.lineNumber,
        //                 element.methodName
        //             )
        //         }
        //     })
        // } else {
        //     Timber.plant(CrashlyticsTree()) // Or your release tree
        // }

        Timber.d("Timber initialized")
    }
}

// Optional: A custom tree for release builds that logs to Crashlytics
// class CrashlyticsTree : Timber.Tree() {
//     override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//         if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
//             return // Don't send verbose, debug, or info logs to Crashlytics
//         }
//
//         FirebaseCrashlytics.getInstance().log(message)
//
//         if (t != null) {
//             if (priority == Log.ERROR || priority == Log.WARN) {
//                 FirebaseCrashlytics.getInstance().recordException(t)
//             }
//         }
//     }
// }

// Optional: A custom tree that does nothing for release builds
// class ReleaseTree : Timber.Tree() {
//    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        // Do nothing for release builds if you don't want any logs
//    }
// }
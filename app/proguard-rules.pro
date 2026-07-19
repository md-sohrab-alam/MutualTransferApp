# Keep line numbers for Play Console / Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Kotlin metadata (needed for reflection / default constructors)
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Firestore models: CustomClassMapper needs original class + member names
-keep class com.shikshak.transfer.ui.theme.data.** { *; }
-keepclassmembers class com.shikshak.transfer.ui.theme.data.** { *; }

# Notification model used with Firestore
-keep class com.shikshak.transfer.ui.theme.updates.NotificationItem { *; }
-keepclassmembers class com.shikshak.transfer.ui.theme.updates.NotificationItem { *; }

# Firebase Firestore annotations
-keepclassmembers class * {
  @com.google.firebase.firestore.PropertyName <methods>;
  @com.google.firebase.firestore.PropertyName <fields>;
  @com.google.firebase.firestore.ServerTimestamp <fields>;
  @com.google.firebase.firestore.DocumentId <fields>;
  @com.google.firebase.firestore.IgnoreExtraProperties *;
  @com.google.firebase.firestore.Exclude *;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
    @javax.inject.* <init>(...);
}

# Keep Application & entry points
-keep class com.shikshak.transfer.MutualTransferApp { *; }
-keep class com.shikshak.transfer.MainActivity { *; }
-keep class com.shikshak.transfer.services.FCMService { *; }
-keep class com.shikshak.transfer.services.NotificationStore { *; }
-keep class com.shikshak.transfer.data.Prefs { *; }
-keep class com.shikshak.transfer.di.** { *; }

# Enums
-keepclassmembers enum * { *; }

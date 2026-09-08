plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    id("org.jetbrains.kotlin.kapt")
//    id("kotlin-kapt")
}

import java.io.File
import java.util.Properties

android {
    namespace = "com.shikshak.transfer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shikshak.transfer"
        minSdk = 26
        targetSdk = 36
        versionCode = 7
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val secretPropsFile = rootProject.file("signing-secret/keystore.properties")
            val rootPropsFile = rootProject.file("keystore.properties")
            val keystorePropsFile = when {
                secretPropsFile.exists() -> secretPropsFile
                rootPropsFile.exists() -> rootPropsFile
                else -> null
            }
            val envStoreFile = System.getenv("SIGNING_STORE_FILE")
            when {
                keystorePropsFile != null -> {
                    val keystoreProps = Properties().apply {
                        keystorePropsFile.inputStream().use { load(it) }
                    }
                    val storePath = keystoreProps["storeFile"] as String
                    storeFile = if (File(storePath).isAbsolute) {
                        file(storePath)
                    } else {
                        keystorePropsFile.parentFile.resolve(storePath)
                    }
                    storePassword = keystoreProps["storePassword"] as String
                    keyAlias = keystoreProps["keyAlias"] as String
                    keyPassword = keystoreProps["keyPassword"] as String
                }
                !envStoreFile.isNullOrBlank() -> {
                    storeFile = file(envStoreFile)
                    storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                    keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                    keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig =  true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging.ktx)


    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.transport.api)
    debugImplementation("androidx.compose.ui:ui-tooling")


    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    // ✅ Hilt + Jetpack Compose Integration
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.navigation.compose)

    implementation(libs.jakewharton.timber)
    
    // Accompanist for SwipeRefresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Material Icons Extended for Compose
    implementation("androidx.compose.material:material-icons-extended")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
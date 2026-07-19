pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Resolve Android/Google plugins via the same Maven modules Dispatch-Automation uses.
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application",
                "com.android.library" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
                "com.google.gms.google-services" -> {
                    useModule("com.google.gms:google-services:${requested.version}")
                }
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MutualTransferApp"
include(":app")

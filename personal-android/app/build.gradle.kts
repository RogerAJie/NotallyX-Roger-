plugins {
    id("com.android.application")
}

android {
    namespace = "com.rogerajie.mynotes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rogerajie.mynotes"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

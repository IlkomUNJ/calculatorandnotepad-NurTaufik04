// App module build.gradle (Kotlin DSL or Groovy; contoh Groovy)
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.example.superapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion "1.5.4" // sesuaikan dengan versi Compose di project Anda
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.activity:activity-compose:1.8.0"
    implementation "androidx.compose.ui:ui:1.5.0"
    implementation "androidx.compose.material:material:1.5.0"
    implementation "androidx.compose.ui:ui-tooling-preview:1.5.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.1"
    // Optional: icons
    implementation "androidx.compose.material:material-icons-extended:1.5.0"
}

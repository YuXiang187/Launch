plugins {
    id("com.android.application")
}

android {
    namespace = "com.yuxiang.launch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yuxiang.launch"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    compileSdk = Android.compileSdk
    defaultConfig {
        applicationId = Android.appId
        minSdk = Android.minSdk
        targetSdk = Android.targetSdk
        versionCode = Android.versionCode
        versionName = Android.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"))
        }
    }
    buildFeatures {
        viewBinding = true
        //dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildToolsVersion("30.0.3")
}

dependencies {
    implementation(AndroidX.Core.core)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.material)
    implementation(AndroidX.constraintLayout)

     implementation(project(path = Modules.libx))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation(AndroidX.Lifecycle.livedata)
    //ktx
    implementation(AndroidX.Lifecycle.viewmodel)
    implementation(AndroidX.Lifecycle.fragment)
    implementation(AndroidX.Lifecycle.activity)
}

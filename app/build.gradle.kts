plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    id("kotlin-parcelize")
}

android {
    namespace = "com.fida.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fida.app"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.play.services)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Google Generative AI (Gemini) - Using REST API approach
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:18.0.2")

    // JSON
    implementation("com.google.code.gson:gson:2.11.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // Third party
    implementation("nl.joery.timerangepicker:timerangepicker:1.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
    outputToConsole.set(true)
}

detekt {
    config.setFrom(files("$rootDir/config/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

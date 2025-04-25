import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Đọc key từ local.properties
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        load(localFile.inputStream())
    }
}
val googleMapsApiKey: String = localProperties["GOOGLE_MAPS_API_KEY"] as String? ?: ""

android {
    namespace = "com.example.weathernow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weathernow"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Gán biến vào manifest
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.guava)



    // Room (Local DB)
    annotationProcessor(libs.room.compiler)

    // Retrofit & GSON (Weather API)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Firebase modules
    implementation(libs.google.firebase.firestore)

    implementation (libs.play.services.location)

    implementation(libs.secrets.gradle.plugin)

    implementation(libs.play.services.maps)
    implementation (libs.play.services.base)
}
apply(plugin = "com.google.gms.google-services")
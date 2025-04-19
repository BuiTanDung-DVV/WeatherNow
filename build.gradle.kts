plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath("com.android.tools.build:gradle:8.8.0")
        classpath("com.google.gms:google-services:4.3.15")
    }
}

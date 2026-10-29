// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Archivo de Gradle a nivel de proyecto (Project: Tejisur_App)

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false


}
// Top-level build file where you can add configuration options common to all sub-modules/subprojects.
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    alias(libs.plugins.google.gms.google.services) apply false
}


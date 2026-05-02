// Top-level build file where you can add configuration options common to all sub-projects/modules.


tasks.register("printAapt2Version") {
    doLast {
        println("Using AAPT2")
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id("com.google.dagger.hilt.android") version "2.51" apply false
}
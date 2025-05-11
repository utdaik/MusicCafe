plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.cafemusicchange"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cafemusicchange"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "JAMENDO_API_KEY", "\"${project.properties["JAMENDO_API_KEY"] ?: ""}\"")
        }
        release {
            buildConfigField("String", "JAMENDO_API_KEY", "\"${project.properties["JAMENDO_API_KEY"] ?: ""}\"")
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
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.animation.core.lint)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.accessibility.test.framework)
    implementation(libs.accessibility.test.framework)
    implementation(libs.material)
    testImplementation(libs.junit)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.animation)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    debugImplementation(libs.ui.tooling)
    implementation(libs.androidx.navigation.compose)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.coil.compose)
    implementation (libs.androidx.material.icons.extended)
    implementation(libs.converter.gson)
    kapt(libs.androidx.room.compiler)





    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

kapt {
    correctErrorTypes = true
}
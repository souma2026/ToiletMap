plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.toiletmap"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.toiletmap"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Coil
    implementation(
        "io.coil-kt.coil3:coil-compose:3.3.0"
    )
    implementation(
        "io.coil-kt.coil3:coil-network-okhttp:3.3.0"
    )

    // MapLibre
    implementation(
        "org.maplibre.gl:android-sdk:13.4.1"
    )

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )

    // =========================================
    // Supabase
    // 3.2.0ではなく3.2.1を使用
    // =========================================

    implementation(
        platform(
            "io.github.jan-tennert.supabase:bom:3.2.1"
        )
    )

    implementation(
        "io.github.jan-tennert.supabase:auth-kt"
    )

    implementation(
        "io.github.jan-tennert.supabase:postgrest-kt"
    )

    implementation(
        "io.github.jan-tennert.supabase:storage-kt"
    )

    // Ktor 3.2.0にはAndroidビルドの不具合があるため3.2.1
    implementation(
        "io.ktor:ktor-client-android:3.2.1"
    )

    // Test
    testImplementation(libs.junit)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
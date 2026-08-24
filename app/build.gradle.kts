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

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}


dependencies {

    // =========================================
    // Jetpack Compose
    // =========================================

    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )


    // =========================================
    // Material Icons
    //
    // Icons.Filled.Add
    // Icons.Filled.Person
    // Icons.Filled.Place
    // などを使用するため
    // =========================================

    implementation(
        "androidx.compose.material:material-icons-extended"
    )


    // =========================================
    // Coil
    //
    // プロフィール画像表示
    // =========================================

    implementation(
        "io.coil-kt.coil3:coil-compose:3.3.0"
    )

    implementation(
        "io.coil-kt.coil3:coil-network-okhttp:3.3.0"
    )


    // =========================================
    // MapLibre
    // =========================================

    implementation(
        "org.maplibre.gl:android-sdk:13.4.1"
    )


    // =========================================
    // OkHttp
    // =========================================

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )


    // =========================================
    // Supabase
    // =========================================

    implementation(
        platform(
            "io.github.jan-tennert.supabase:bom:3.2.1"
        )
    )


    // Supabase Auth
    implementation(
        "io.github.jan-tennert.supabase:auth-kt"
    )


    // Supabase Database
    implementation(
        "io.github.jan-tennert.supabase:postgrest-kt"
    )


    // Supabase Storage
    implementation(
        "io.github.jan-tennert.supabase:storage-kt"
    )


    // =========================================
    // Ktor
    //
    // Supabaseの通信で使用
    // =========================================

    implementation(
        "io.ktor:ktor-client-android:3.2.1"
    )


    // =========================================
    // Unit Test
    // =========================================

    testImplementation(
        libs.junit
    )


    // =========================================
    // Android Test
    // =========================================

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
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


    // =========================================
    // Debug
    // =========================================

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
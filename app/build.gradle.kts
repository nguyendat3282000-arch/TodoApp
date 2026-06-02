plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)         // Firebase
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.example.todoapp"
    compileSdk  = 36

    defaultConfig {
        applicationId  = "com.example.todoapp"
        minSdk         = 26          // AlarmManager.setExactAndAllowWhileIdle requires API 23+; USE_EXACT_ALARM needs API 33+
        targetSdk      = 36
        versionCode    = 1
        versionName    = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Web Client ID from Firebase Console (OAuth 2.0)
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"109419331279-1tko299tttmgueuqnrpcbr81lmrn4oge.apps.googleusercontent.com\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose     = true
        buildConfig = true      // Needed for GOOGLE_WEB_CLIENT_ID constant
        aidl        = false
        shaders     = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    // ─── Compose BOM ──────────────────────────────────────────────────────────
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ─── Core AndroidX ────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // ─── Compose UI ───────────────────────────────────────────────────────────
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.ext)
    implementation(libs.compose.google.fonts)

    // ─── Lifecycle / ViewModel ────────────────────────────────────────────────
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ─── Navigation 3 ─────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // ─── Firebase BoM + products ───────────────────────────────────────────────
    // Pin the BoM as a string literal to prevent transitive version bumps.
    // NOTE: Since BoM 32.0.0, -ktx suffix is merged into base artifacts.
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // ─── Credential Manager (Google Sign-In) ──────────────────────────────────
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // ─── Glance (App Widget) ──────────────────────────────────────────────────
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // ─── DataStore ────────────────────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ─── Kotlinx Serialization ────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ─── Coroutines ───────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ─── Room Database ─────────────────────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ─── WorkManager ──────────────────────────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ─── Debug / Tooling ──────────────────────────────────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ─── Unit Tests ───────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // ─── Instrumented Tests ───────────────────────────────────────────────────
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}


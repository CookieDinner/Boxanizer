plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.cookiedinner.boxanizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cookiedinner.boxanizer"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "v1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

sqldelight {
    databases {
        create("BoxanizerDb") {
            packageName.set("com.cookiedinner.boxanizer.database")
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}

dependencies {
    // Project libraries
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.datastore)
    implementation(libs.accompanist)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.sqldelight.android.driver)

    implementation(libs.coil)

    implementation(libs.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)

    // Test libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
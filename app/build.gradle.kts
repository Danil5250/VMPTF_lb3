plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.vmptf.mobile"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.vmptf.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //using retrofit for network requests
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //convertor from json (choosed gson, also possible moshi)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    //for async working (essential in api uploading to not stop)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    }
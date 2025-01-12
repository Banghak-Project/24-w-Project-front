plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.moneychanger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moneychanger"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.camera.viewfinder:viewfinder-core:1.4.0-alpha11")
    implementation("androidx.databinding:databinding-runtime:8.8.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-android:2.8.7")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

        // Camerax
    val cameraxVersion = "1.3.0" // CameraX 최신 버전 (2025년 1월 기준)

    // CameraX 기본 라이브러리
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")

    // 라이프사이클과 View를 CameraX에 연결
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:1.0.0-alpha14")

    // CameraX 확장(선택 사항, 예: HDR, 나이트 모드)
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("github.hongbeomi:macgyver:1.0.0")
}
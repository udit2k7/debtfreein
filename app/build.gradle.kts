import java.util.Properties
import java.io.File

val patchFile = File(projectDir.parentFile, "patch_counter.txt")
val patchVersion: Int = if (patchFile.exists()) {
    val count = try { patchFile.readText().trim().toInt() } catch (e: Exception) { 0 }
    val newCount = count + 1
    try { patchFile.writeText(newCount.toString()) } catch (e: Exception) {}
    newCount
} else {
    try { patchFile.writeText("1") } catch (e: Exception) {}
    1
}
val semanticVersionName = "1.1.3"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: "\"YOUR_KEY_HERE\""
val marketDataApiKey = localProperties.getProperty("MARKET_DATA_API_KEY") ?: "\"YOUR_KEY_HERE\""

android {
    namespace = "com.debtfreein.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.udittandon.debtfree"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = semanticVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "GEMINI_API_KEY", geminiApiKey)
        buildConfigField("String", "MARKET_DATA_API_KEY", marketDataApiKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.configureEach {
        val variant = this
        outputs.configureEach {
            val apkOutput = this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val buildTypeName = variant.buildType.name.substring(0, 1).uppercase() + variant.buildType.name.substring(1)
            apkOutput?.outputFileName = "DebtFreeIn-${buildTypeName}-v${variant.versionName}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-metadata-version-check")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // compatible with Kotlin 1.9.22
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gemini generative AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore:24.11.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")

    // Biometric & Process Lifecycle dependencies
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.biometric:biometric-compose:1.4.0-alpha06")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.configureEach {
    if (name.contains("AarMetadata")) {
        enabled = false
    }
}

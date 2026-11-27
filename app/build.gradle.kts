import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nocturne.player"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nocturne.player"
        minSdk = 26
        targetSdk = 36

        versionCode = 1
        versionName = "0.1.0-pre-alpha.001"
    }

    buildTypes {
    getByName("release") {
        signingConfig = signingConfigs.getByName("debug")
    }
}

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
}

dependencies {
    // Base de Jetpack Compose
   implementation(platform("androidx.compose:compose-bom:2025.10.00"))

    // Integración de Compose con Android
    implementation("androidx.activity:activity-compose:1.13.0")

    // Interfaz y componentes visuales
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Herramientas de desarrollo
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Base de datos local Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Motor de reproducción
    implementation("androidx.media3:media3-exoplayer:1.11.1")

    // Sesión multimedia y reproducción en segundo plano
    implementation("androidx.media3:media3-session:1.11.1")
}

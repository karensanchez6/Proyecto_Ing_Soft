plugins {
    id("com.android.application")
    kotlin("android")
    jacoco
}

android {
    namespace = "com.example.pfinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pfinal"
        minSdk = 24
        targetSdk = 35
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

    // Java/Kotlin 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // Permite usar recursos en unit tests
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

/**
 * Reporte de cobertura para SonarQube (Gradle 8.x / AGP 8.x)
 * Genera: app/build/reports/jacoco/testDebugUnitTestReport/testDebugUnitTestReport.xml
 */
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    // Clases compiladas (Kotlin + Java) en Debug
    val kotlinClasses = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude("**/R.class", "**/R$*.class", "**/*\$ViewBinder*.*", "**/BuildConfig.*", "**/*\$Companion.*")
    }
    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") {
        exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*")
    }
    classDirectories.setFrom(files(kotlinClasses, javaClasses))

    // Directorios de fuentes
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    // Ficheros .exec que dejan los unit tests de Debug
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)

        // Ubicaciones de salida (Gradle 8+: usar outputLocation)
        xml.outputLocation.set(file("$buildDir/reports/jacoco/testDebugUnitTestReport/testDebugUnitTestReport.xml"))
        html.outputLocation.set(file("$buildDir/reports/jacoco/testDebugUnitTestReport/html"))
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

        id("org.sonarqube") version "6.3.1.5724"
    
}

sonarqube {
    properties {
        property("sonar.projectKey", "vota_plus")
        property("sonar.projectName", "Vota+")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", System.getenv("SONAR_TOKEN"))

        // Código y tests del módulo app (ajusta si tu módulo tiene otro nombre)
        property("sonar.sources", "app/src/main/java,app/src/main/kotlin")
        property("sonar.tests", "app/src/test/java,app/src/test/kotlin")

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.kotlin.jvm.version", "17")

        // XML que generaremos con la tarea jacocoTestReport del módulo app
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "app/build/reports/jacoco/testDebugUnitTestReport/testDebugUnitTestReport.xml"
        )
    }
}

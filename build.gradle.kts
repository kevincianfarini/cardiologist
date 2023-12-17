plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.publish)
}

kotlin {

    explicitApi()

    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "5s"
                }
            }
        }
    }
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.atomicfu)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

tasks.withType<AbstractTestTask> {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
        showStackTraces = true
    }
}
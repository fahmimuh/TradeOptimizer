plugins {
    id("org.jetbrains.kotlin.jvm")
}


dependencies {
    implementation(project(":core:core-common"))
    implementation(kotlin("stdlib"))

    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}
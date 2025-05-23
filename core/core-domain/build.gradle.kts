plugins {
    id("org.jetbrains.kotlin.jvm")
}


dependencies {
    implementation(project(":core:core-common"))
    implementation(kotlin("stdlib"))

    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
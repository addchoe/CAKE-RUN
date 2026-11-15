plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.cakerun.app"
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    defaultConfig { applicationId = "com.cakerun.app"; minSdk = 28; targetSdk = 34; versionCode = 1; versionName = "0.1.0" }
    flavorDimensions += "source"
    productFlavors {
        create("sim") { dimension = "source"; applicationIdSuffix = ".sim" }
        create("pleos") { dimension = "source" }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    "pleosImplementation"("ai.pleos.playground:Vehicle:2.3.2")
    "pleosImplementation"("ai.pleos.playground:NaviHelper:3.0.5")
    testImplementation("junit:junit:4.13.2")
}

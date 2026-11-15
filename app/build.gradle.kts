plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.cakerun.app"
    compileSdk = 35
    buildToolsVersion = "35.0.0"
    defaultConfig { applicationId = "com.cakerun.app"; minSdk = 28; targetSdk = 34; versionCode = 2; versionName = "0.2.0" }
    flavorDimensions += "source"
    productFlavors {
        create("sim") { dimension = "source"; applicationIdSuffix = ".sim" }
        create("pleos") { dimension = "source" }
    }
    val releaseStore = System.getenv("CAKERUN_STORE_FILE")
    signingConfigs {
        if (!releaseStore.isNullOrBlank()) {
            create("submission") {
                storeFile = file(releaseStore)
                storePassword = System.getenv("CAKERUN_STORE_PASSWORD")
                keyAlias = System.getenv("CAKERUN_KEY_ALIAS")
                keyPassword = System.getenv("CAKERUN_KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            if (!releaseStore.isNullOrBlank()) signingConfig = signingConfigs.getByName("submission")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    "pleosImplementation"("ai.pleos.playground:Vehicle:2.3.2")
    "pleosImplementation"("ai.pleos.playground:NaviHelper:3.0.5")
    testImplementation("junit:junit:4.13.2")
}

plugins { id("com.android.application") }
android {
    namespace = "com.aliaygor.trainpop"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.aliaygor.trainpop"; minSdk = 24; targetSdk = 36; versionCode = 3; versionName = "0.3.0"
        manifestPlaceholders["admobAppId"] = providers.gradleProperty("TRAINPOP_ADMOB_APP_ID").getOrElse("ca-app-pub-3940256099942544~3347511713")
        buildConfigField("String", "REWARDED_AD_ID", "\"${providers.gradleProperty("TRAINPOP_REWARDED_AD_ID").getOrElse("")}\"")
        buildConfigField("String", "BILLING_PUBLIC_KEY", "\"${providers.gradleProperty("TRAINPOP_BILLING_PUBLIC_KEY").getOrElse("")}\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.android.billingclient:billing:8.3.0")
    testImplementation("junit:junit:4.13.2")
}

plugins { id("com.android.application") }
android {
    namespace = "com.axioapps.trainpop"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.axioapps.trainpop"; minSdk = 24; targetSdk = 36; versionCode = 12; versionName = "0.12.0"
        manifestPlaceholders["admobAppId"] = providers.gradleProperty("TRAINPOP_ADMOB_APP_ID").getOrElse("ca-app-pub-3940256099942544~3347511713")
        buildConfigField("String", "INTERSTITIAL_AD_ID", "\"${providers.gradleProperty("TRAINPOP_INTERSTITIAL_AD_ID").getOrElse("")}\"")
        buildConfigField("String", "REWARDED_AD_ID", "\"${providers.gradleProperty("TRAINPOP_REWARDED_AD_ID").getOrElse("")}\"")
        buildConfigField("String", "BILLING_PUBLIC_KEY", "\"${providers.gradleProperty("TRAINPOP_BILLING_PUBLIC_KEY").getOrElse("")}\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("com.android.billingclient:billing:8.3.0")
    testImplementation("junit:junit:4.13.2")
}

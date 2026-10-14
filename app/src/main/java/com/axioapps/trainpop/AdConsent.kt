package com.axioapps.trainpop

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.*

class AdConsent(private val activity:Activity) {
    private val info=UserMessagingPlatform.getConsentInformation(activity)
    private val callbacks=mutableListOf<()->Unit>()
    private var initialized=false
    private var ready=false
    val canRequestAds get()=ready && (BuildConfig.DEBUG || info.canRequestAds())
    val privacyRequired get()=!BuildConfig.DEBUG && info.privacyOptionsRequirementStatus==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    init {
        if(BuildConfig.DEBUG) initialize()
        else if(BuildConfig.REWARDED_AD_ID.isNotEmpty() || BuildConfig.INTERSTITIAL_AD_ID.isNotEmpty()) {
            info.requestConsentInfoUpdate(activity,ConsentRequestParameters.Builder().build(),{
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { if(info.canRequestAds()) initialize() }
            },{ if(info.canRequestAds()) initialize() })
            if(info.canRequestAds()) initialize()
        }
    }
    private fun initialize() {
        if(initialized) return
        initialized=true
        MobileAds.initialize(activity) { activity.runOnUiThread { ready=true;callbacks.toList().forEach { it() };callbacks.clear() } }
    }
    fun whenReady(block:()->Unit) { if(canRequestAds) block() else callbacks+=block }
    fun privacyOptions() { UserMessagingPlatform.showPrivacyOptionsForm(activity) { } }
}

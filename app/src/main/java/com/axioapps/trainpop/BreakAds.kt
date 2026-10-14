package com.axioapps.trainpop

import android.app.Activity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.*

class BreakAds(private val activity:Activity,private val economy:Economy,private val consent:AdConsent) {
    private val prefs=activity.getSharedPreferences("trainpop",0)
    private val unit=if(BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else BuildConfig.INTERSTITIAL_AD_ID
    private var ad:InterstitialAd?=null
    private var loadedAt=0L
    private var loading=false
    private var released=false
    private var continuation:(()->Unit)?=null
    var showing=false;private set
    init { consent.whenReady { load() } }
    private fun load() {
        if(released || loading || unit.isEmpty() || !consent.canRequestAds || economy.noAds) return
        loading=true
        InterstitialAd.load(activity,unit,AdRequest.Builder().build(),object:InterstitialAdLoadCallback() {
            override fun onAdLoaded(value:InterstitialAd) { loading=false;if(!released) { ad=value;loadedAt=System.currentTimeMillis() } }
            override fun onAdFailedToLoad(error:LoadAdError) { loading=false;ad=null }
        })
    }
    fun betweenLevels(level:Int,next:()->Unit) {
        if(showing) return
        val now=System.currentTimeMillis()
        val eligible=BreakAdPolicy.eligible(level,economy.noAds,prefs.getInt("break.lastLevel",0),prefs.getLong("break.lastShown",0L),now)
        val current=ad
        if(!eligible || current==null || now-loadedAt>3_300_000 || !consent.canRequestAds) { if(current==null || now-loadedAt>3_300_000) { ad=null;load() };next();return }
        ad=null;showing=true;continuation=next
        current.fullScreenContentCallback=object:FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { prefs.edit().putInt("break.lastLevel",level).putLong("break.lastShown",System.currentTimeMillis()).apply() }
            override fun onAdDismissedFullScreenContent() { finish() }
            override fun onAdFailedToShowFullScreenContent(error:AdError) { finish() }
        }
        current.show(activity)
    }
    private fun finish() { showing=false;val next=continuation;continuation=null;if(!released) { next?.invoke();load() } }
    fun release() { released=true;ad=null;continuation=null }
}

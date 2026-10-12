package com.aliaygor.trainpop

import android.app.Activity
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.AudioAttributes
import android.util.Base64
import com.android.billingclient.api.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.*
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.security.MessageDigest

class PreferenceLedger(private val prefs: SharedPreferences): Ledger {
    override fun int(key: String, default: Int) = prefs.getInt(key, default)
    override fun string(key: String, default: String) = prefs.getString(key, default) ?: default
    override fun write(ints: Map<String, Int>, strings: Map<String, String>) {
        check(prefs.edit().also { e -> ints.forEach { (k,v) -> e.putInt(k,v) }; strings.forEach { (k,v) -> e.putString(k,v) } }.commit())
    }
}

class Audio(private val activity: Activity) {
    private val pool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val sounds = mapOf("tap" to pool.load(activity,R.raw.tap,1), "board" to pool.load(activity,R.raw.board,1), "depart" to pool.load(activity,R.raw.depart,1), "win" to pool.load(activity,R.raw.win,1), "error" to pool.load(activity,R.raw.error,1))
    private var player: MediaPlayer? = MediaPlayer.create(activity,R.raw.music)?.apply { isLooping=true;setVolume(.15f,.15f) }
    var effects=true
    fun play(name:String) { if(effects) sounds[name]?.let { pool.play(it,.55f,.55f,1,0,1f) } }
    fun music(enabled:Boolean) { if(enabled) player?.start() else player?.pause() }
    fun release() { player?.release();player=null;pool.release() }
}

/** No ad reward is delivered on dismissal or load failure. Debug always uses Google's test unit. */
class RewardAds(private val activity:Activity,private val status:(String)->Unit) {
    private var ad:RewardedAd?=null
    private var loading=false
    private var pending:(()->Unit)?=null
    private val handler=android.os.Handler(android.os.Looper.getMainLooper())
    val preparing get()=pending!=null
    private val timeout=Runnable { if(pending!=null) { pending=null;showing=false;status("Reklam y�klenemedi. Tekrar deneyebilirsin.") } }
    fun release() { pending=null;handler.removeCallbacks(timeout) }
    var showing=false;private set
    private val unit = if(BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else BuildConfig.REWARDED_AD_ID
    init { if(BuildConfig.DEBUG) MobileAds.initialize(activity) { activity.runOnUiThread { load() } } }
    private fun load() {
        if(unit.isEmpty() || loading || !BuildConfig.DEBUG) return // Release awaits consent configuration.
        loading=true
        RewardedAd.load(activity,unit,AdRequest.Builder().build(),object:RewardedAdLoadCallback() {
            override fun onAdLoaded(value:RewardedAd) { loading=false;ad=value;pending?.let { pending=null;showing=false;handler.removeCallbacks(timeout);show(it) } }
            override fun onAdFailedToLoad(error:LoadAdError) { loading=false;ad=null;if(pending!=null) { pending=null;showing=false;handler.removeCallbacks(timeout);status("Reklam y�klenemedi; haklar�n korunuyor.") } }
        })
    }
    fun show(reward:()->Unit) {
        if(showing) return
        if(!BuildConfig.DEBUG) { status("Canlı reklamlar henüz etkin değil.");return }
        val current=ad
        if(current==null) { load();status("Reklam hazırlanıyor. Biraz sonra tekrar dene.");return }
        showing=true;ad=null;var granted=false
        current.fullScreenContentCallback=object:FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { showing=false;load() }
            override fun onAdFailedToShowFullScreenContent(error:AdError) { showing=false;status("Reklam açılamadı; hakların korunuyor.");load() }
        }
        current.show(activity) { if(!granted) { granted=true;reward() } }
    }
}

/** Prices come from Play. Entitlements require a signed PURCHASED receipt, never a UI button. */
class Store(private val activity:Activity,private val economy:Economy,private val status:(String)->Unit) : PurchasesUpdatedListener {
    val ids=listOf("starter_pack","remove_ads","ad_free_bundle","coins_500","coins_3000")
    private val products=mutableMapOf<String,ProductDetails>()
    private var fetching=false
    private val client=BillingClient.newBuilder(activity).setListener(this).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).enableAutoServiceReconnection().build()
    init { connect() }
    fun connect() {
        if(fetching) return
        fetching=true
        client.startConnection(object:BillingClientStateListener {
            override fun onBillingSetupFinished(result:BillingResult) { fetching=false;if(result.responseCode==BillingClient.BillingResponseCode.OK) { query();restore(false) } }
            override fun onBillingServiceDisconnected() { fetching=false }
        })
    }
    private fun query() {
        val items=ids.map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build() }
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(items).build()) { result, details ->
            if(result.responseCode==BillingClient.BillingResponseCode.OK) { synchronized(products) { products.clear();details.productDetailsList.forEach { products[it.productId]=it } } }
        }
    }
    fun price(id:String):String = synchronized(products) { products[id]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "Yakında" }
    fun available(id:String):Boolean = BuildConfig.BILLING_PUBLIC_KEY.isNotEmpty() && synchronized(products) { products.containsKey(id) }
    fun buy(id:String) {
        if(BuildConfig.BILLING_PUBLIC_KEY.isEmpty()) { status("Satın alma Play Console kurulumu bekliyor.");return }
        val details=synchronized(products) { products[id] }
        if(details==null) { if(client.isReady) query() else connect();status("Ürün şu anda kullanılamıyor.");return }
        val product=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()
        val result=client.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(product)).build())
        if(result.responseCode!=BillingClient.BillingResponseCode.OK) status("Satın alma başlatılamadı; ücret alınmadı.")
    }
    fun restore(manual:Boolean=true) {
        if(!client.isReady) { connect();if(manual) status("Google Play bağlantısı hazırlanıyor.");return }
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { result,purchases ->
            if(result.responseCode==BillingClient.BillingResponseCode.OK) purchases.forEach { handle(it) }
            if(manual) activity.runOnUiThread { status(if(result.responseCode==0) "Satın almalar kontrol edildi." else "Google Play bağlantısı kurulamadı.") }
        }
    }
    override fun onPurchasesUpdated(result:BillingResult,purchases:MutableList<Purchase>?) {
        if(result.responseCode==BillingClient.BillingResponseCode.OK) purchases?.forEach { handle(it) }
        else if(result.responseCode!=BillingClient.BillingResponseCode.USER_CANCELED) status("Satın alma tamamlanamadı.")
    }
    private fun verified(p:Purchase):Boolean = try {
        val key=KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(Base64.decode(BuildConfig.BILLING_PUBLIC_KEY,Base64.DEFAULT)))
        Signature.getInstance("SHA1withRSA").run { initVerify(key);update(p.originalJson.toByteArray(Charsets.UTF_8));verify(Base64.decode(p.signature,Base64.DEFAULT)) }
    } catch(_:Exception) { false }
    private fun handle(p:Purchase) {
        if(p.purchaseState!=Purchase.PurchaseState.PURCHASED || !verified(p)) return
        val id=p.products.singleOrNull() ?: return
        if(id !in ids) return
        activity.runOnUiThread {
            val hash=MessageDigest.getInstance("SHA-256").digest(p.purchaseToken.toByteArray()).joinToString("") { "%02x".format(it) }
            if(economy.grantPurchase(id,hash)) status("Satın alma tamamlandı!")
            if(id=="coins_500" || id=="coins_3000") client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { _,_-> }
            else if(!p.isAcknowledged) client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { }
        }
    }
    fun release() { client.endConnection() }
}

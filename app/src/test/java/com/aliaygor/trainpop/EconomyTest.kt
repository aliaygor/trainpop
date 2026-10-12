package com.aliaygor.trainpop
import org.junit.Assert.*
import org.junit.Test
private class MemoryLedger:Ledger {
    val ints=mutableMapOf<String,Int>();val strings=mutableMapOf<String,String>()
    override fun int(key:String,default:Int)=ints[key]?:default
    override fun string(key:String,default:String)=strings[key]?:default
    override fun write(ints:Map<String,Int>,strings:Map<String,String>) { this.ints.putAll(ints);this.strings.putAll(strings) }
}
class EconomyTest {
    @Test fun dailyIsOncePerDayAndCyclesSevenRewards() { val e=Economy(MemoryLedger());repeat(14) { n->val day="day$n";assertEquals(e.dailyRewards[n%7],e.claimDaily(day));assertNull(e.claimDaily(day)) };assertEquals(0,e.dailyIndex()) }
    @Test fun supplyEnforcesOrderAndResetsNextDay() {
        val e=Economy(MemoryLedger());assertNull(e.claimSupplyAd("a",1));assertEquals(15,e.claimSupply("a")!!.coins);assertNull(e.claimSupply("a"));assertNull(e.claimSupplyAd("a",3))
        for(i in 1..4) { assertNotNull(e.claimSupplyAd("a",i));assertNull(e.claimSupplyAd("a",i)) };assertEquals(0,e.supplyStep("b"));assertNotNull(e.claimSupply("b"))
    }
    @Test fun spinSurvivesRestartAndCannotPayTwice() { val l=MemoryLedger();val a=Economy(l);assertTrue(a.beginSpin("a",4));assertFalse(a.beginSpin("a",1));val b=Economy(l);assertEquals(Reward(500),b.finishSpin());assertEquals(700,b.coins);assertNull(b.finishSpin());assertEquals(0,b.spins("a")) }
    @Test fun adSpinLimitResetsAcrossDays() { val e=Economy(MemoryLedger());repeat(9){assertTrue(e.grantSpin("a"))};assertFalse(e.grantSpin("a"));assertEquals(10,e.spins("a"));assertTrue(e.beginSpin("b",0));e.finishSpin();assertEquals(9,e.adSpinsLeft("b"));assertTrue(e.grantSpin("b")) }
    @Test fun unsuccessfulActionsDoNotCharge() { val e=Economy(MemoryLedger());assertFalse(e.consume("vip"){false});assertEquals(2,e.inventory("vip"));assertFalse(e.spend(900){true});assertFalse(e.spend(50){false});assertEquals(200,e.coins) }
    @Test fun winsAndReceiptsAreIdempotent() { val e=Economy(MemoryLedger());assertTrue(e.win(1));assertFalse(e.win(1));assertEquals(250,e.coins);assertTrue(e.grantPurchase("starter_pack","receipt"));assertFalse(e.grantPurchase("starter_pack","receipt"));assertEquals(1250,e.coins);assertTrue(e.starterOwned);assertFalse(e.grantPurchase("unknown","other"));assertTrue(e.grantPurchase("remove_ads","ads"));assertTrue(e.noAds) }
}

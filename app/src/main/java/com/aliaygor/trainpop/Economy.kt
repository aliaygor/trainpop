package com.aliaygor.trainpop

interface Ledger {
    fun int(key: String, default: Int = 0): Int
    fun string(key: String, default: String = ""): String
    fun write(ints: Map<String, Int> = emptyMap(), strings: Map<String, String> = emptyMap())
}
data class Reward(val coins: Int = 0, val vip: Int = 0, val arrange: Int = 0, val shuffle: Int = 0) {
    fun label(): String = buildList {
        if (coins > 0) add("$coins altın"); if (vip > 0) add("$vip VIP"); if (arrange > 0) add("$arrange Düzenle"); if (shuffle > 0) add("$shuffle Karıştır")
    }.joinToString(" + ")
}
class Economy(private val store: Ledger) {
    val dailyRewards = listOf(Reward(50), Reward(shuffle = 1), Reward(80), Reward(vip = 1), Reward(150), Reward(arrange = 1), Reward(400, arrange = 1))
    val wheelRewards = listOf(Reward(150), Reward(shuffle = 1), Reward(30), Reward(vip = 1), Reward(500), Reward(arrange = 1))
    val coins get() = store.int("coins", 200)
    val level get() = store.int("level", 1)
    fun inventory(key: String) = store.int(key, 2)
    fun dailyAvailable(day: String) = store.string("daily.date") != day
    fun dailyIndex() = store.int("daily.index") % 7
    fun supplyStep(day: String) = if (store.string("supply.date") == day) store.int("supply.step") else 0
    fun spins(day: String) = if (store.string("wheel.date") == day) store.int("wheel.spins", 1) else 1
    private fun amounts(r: Reward) = mapOf("coins" to coins + r.coins, "vip" to inventory("vip") + r.vip, "arrange" to inventory("arrange") + r.arrange, "shuffle" to inventory("shuffle") + r.shuffle)
    fun claimDaily(day: String): Reward? {
        if (!dailyAvailable(day)) return null
        val r = dailyRewards[dailyIndex()]
        store.write(amounts(r) + ("daily.index" to store.int("daily.index") + 1), mapOf("daily.date" to day)); return r
    }
    fun claimSupply(day: String): Reward? {
        if (supplyStep(day) != 0) return null
        val r = Reward(15); store.write(amounts(r) + ("supply.step" to 1), mapOf("supply.date" to day)); return r
    }
    fun claimSupplyAd(day: String, expectedStep: Int): Reward? {
        val values = listOf(15, 20, 50, 70, 100)
        if (expectedStep !in 1..4 || supplyStep(day) != expectedStep) return null
        val r = Reward(values[expectedStep]); store.write(amounts(r) + ("supply.step" to expectedStep + 1), mapOf("supply.date" to day)); return r
    }
    fun grantAdCoins(): Reward { val r = Reward(50); store.write(amounts(r)); return r }
    fun grantSpin(day: String): Boolean {
        val used = if (store.string("wheel.date") == day) store.int("wheel.adCount") else 0
        if (used >= 9) return false
        store.write(mapOf("wheel.spins" to spins(day) + 1, "wheel.adCount" to used + 1), mapOf("wheel.date" to day)); return true
    }
    fun adSpinsLeft(day: String) = 9 - if (store.string("wheel.date") == day) store.int("wheel.adCount") else 0
    val noAds get() = store.int("noAds") == 1
    val starterOwned get() = store.int("starterOwned") == 1
    fun grantPurchase(product: String, tokenHash: String): Boolean {
        if (store.int("purchase.$tokenHash") == 1) return false
        val reward = when (product) {
            "coins_500" -> Reward(500); "coins_3000" -> Reward(3000)
            "starter_pack" -> Reward(1000, 2, 2, 2)
            "ad_free_bundle" -> Reward(1500, 1, 1, 1)
            "remove_ads" -> Reward(); else -> return false
        }
        val flags = mutableMapOf("purchase.$tokenHash" to 1)
        if (product == "starter_pack") flags["starterOwned"] = 1
        if (product == "remove_ads" || product == "ad_free_bundle") flags["noAds"] = 1
        store.write(amounts(reward) + flags); return true
    }
    fun beginSpin(day: String, index: Int): Boolean {
        if (index !in wheelRewards.indices || spins(day) <= 0 || store.int("wheel.pending", -1) >= 0) return false
        val ads = if(store.string("wheel.date") == day) store.int("wheel.adCount") else 0
        store.write(mapOf("wheel.spins" to spins(day) - 1, "wheel.pending" to index, "wheel.adCount" to ads), mapOf("wheel.date" to day)); return true
    }
    fun finishSpin(): Reward? {
        val index = store.int("wheel.pending", -1)
        if (index !in wheelRewards.indices) return null
        val r = wheelRewards[index]; store.write(amounts(r) + ("wheel.pending" to -1)); return r
    }
    fun consume(key: String, apply: () -> Boolean): Boolean {
        val n = inventory(key)
        if (n <= 0 || !apply()) return false
        store.write(mapOf(key to n - 1)); return true
    }
    fun spend(amount: Int, apply: () -> Boolean): Boolean {
        if (amount <= 0 || coins < amount || !apply()) return false
        store.write(mapOf("coins" to coins - amount)); return true
    }
    fun win(currentLevel: Int): Boolean {
        if (currentLevel != level) return false
        store.write(mapOf("level" to level + 1, "coins" to coins + 50)); return true
    }
}

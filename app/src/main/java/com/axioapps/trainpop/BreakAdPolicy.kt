package com.axioapps.trainpop

/** Only natural level transitions, after the first ten introductory levels. */
object BreakAdPolicy {
    fun eligible(level:Int,noAds:Boolean,lastLevel:Int,lastShownMs:Long,nowMs:Long):Boolean =
        !noAds && level>10 && (level-10)%3==0 && level>lastLevel &&
            (lastShownMs==0L || nowMs-lastShownMs>=120_000L)
}

package com.axioapps.trainpop

data class Difficulty(val trains:Int,val colors:Int,val blockedExits:Int) {
    companion object {
        fun forLevel(level:Int):Difficulty {
            val n=level.coerceAtLeast(1)
            val count=(8+(n-1)/3).coerceAtMost(24)
            val colors=when { n<9 -> 3; n<23 -> 4; n<41 -> 5; else -> 6 }
            val blocked=(count*(.12f+((n-1)/100f).coerceAtMost(.48f))).toInt()
            return Difficulty(count,colors,(blocked-if(n%7==0) 1 else 0).coerceAtLeast(1))
        }
    }
}

package com.aliaygor.trainpop
import org.junit.Assert.*
import org.junit.Test
class GameTest {
    @Test fun blockedTrainStaysInYard() { val g=Game(1); assertEquals(Result.BLOCKED,g.dispatch(1)); assertEquals(16,g.yard.size) }
    @Test fun fullPlatformsDoNotLoseTrain() { val g=Game(1); listOf(0,3,4,7).forEach { assertEquals(Result.MOVED,g.dispatch(it)) }; assertEquals(Result.FULL,g.dispatch(8)); assertEquals(12,g.yard.size) }
    @Test fun allLevelsHaveUnassistedSolution() { for(level in 1..30) { val g=Game(level); while(g.yard.isNotEmpty()) {
        val t=g.yard.first { g.canExit(it) }; assertEquals(Result.MOVED,g.dispatch(t.id)); repeat(g.total*4) { g.tick() }
    }; assertTrue(g.completed); assertEquals(g.total,g.boarded) } }
    @Test fun rescueConservesPassengers() { val g=Game(1); g.dispatch(0); g.tick(); assertTrue(g.rescue()); assertEquals(g.total,g.robots.size+g.boarded) }
}

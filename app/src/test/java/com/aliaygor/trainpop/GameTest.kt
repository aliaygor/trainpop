package com.aliaygor.trainpop
import org.junit.Assert.*
import org.junit.Test
class GameTest {
    private fun advance(g:Game,seconds:Float) { repeat((seconds/.02f).toInt()) { g.update(.02f) } }
    @Test fun twoHundredLevelsHaveUnassistedSolutionAndNoOverlaps() {
        for(level in 1..200) {
            val g=Game(level);assertTrue(g.yard.size>=8)
            val cells=g.yard.flatMap { it.cells() };assertEquals(cells.size,cells.toSet().size)
            for(id in g.solution) { assertEquals("Level $level train $id",Result.MOVED,g.dispatch(id));advance(g,3f);assertFalse(g.failed) }
            advance(g,2f);assertTrue("Level $level",g.completed);assertEquals(g.total,g.boarded)
        }
    }
    @Test fun blockingUsesFullTrainFootprint() {
        val g=Game(1);g.yard.clear();g.yard+=Train(1,0,0,0,Direction.RIGHT,2,8);g.yard+=Train(2,3,0,1,Direction.DOWN,2,8)
        assertEquals(Result.BLOCKED,g.dispatch(1));assertEquals(Result.MOVED,g.dispatch(2));assertEquals(Result.MOVED,g.dispatch(1))
    }
    @Test fun arrivalMustFinishBeforeBoarding() {
        val g=Game(1);assertEquals(Result.MOVED,g.dispatch(g.solution.first()));advance(g,.5f);assertEquals(0,g.boarded);advance(g,.5f);assertTrue(g.boarded>0)
    }
    @Test fun fourWrongColorsCauseFailureAndRescueRestoresPlay() {
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(4){0}+List(16){1}
        for(i in 0..3) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        for(i in 0..3) assertEquals(Result.MOVED,g.dispatch(i))
        advance(g,3f);assertTrue(g.failed);assertEquals(0,g.boarded);assertTrue(g.rescue());assertFalse(g.failed);assertEquals(5,g.platformCount);assertEquals(4,g.robots.size)
    }
    @Test fun matchingFullPlatformsKeepBoarding() {
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(16){0}
        for(i in 0..3) g.yard+=Train(i,i,0,0,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};advance(g,5f);assertFalse(g.failed);assertTrue(g.completed)
    }
    @Test fun fullDispatchDoesNotLoseVehicle() {
        val g=Game(1);g.yard.clear();for(i in 0..4) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};assertEquals(Result.FULL,g.dispatch(4));assertEquals(1,g.yard.size)
    }
    @Test fun boostersConservePassengerDemand() {
        val g=Game(12)
        fun checkDemand() { assertEquals(g.yard.sumOf { it.remaining }+g.platforms.filterNotNull().sumOf { it.remaining },g.robots.size);assertEquals(g.total,g.boarded+g.robots.size) }
        g.dispatch(g.solution.first());advance(g,1f);checkDemand();assertTrue(g.arrange());checkDemand();assertTrue(g.shuffle());checkDemand();assertTrue(g.rescue());checkDemand();if(g.vip()) checkDemand()
    }
    @Test fun checkpointRestoresMovesInventoryAndDemand() {
        val g=Game(9);g.dispatch(g.solution.first());advance(g,1f)
        val restored=Game.restore(g.checkpoint())!!
        assertEquals(g.checkpoint(),restored.checkpoint());assertEquals(g.robots,restored.robots)
        advance(restored,3f);assertFalse(restored.failed)
        assertNull(Game.restore("broken"))
    }
}

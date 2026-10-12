package com.aliaygor.trainpop
import org.junit.Assert.*
import org.junit.Test
class GameTest {
    private fun advance(g:Game,seconds:Float) { repeat((seconds/.02f).toInt()) { g.update(.02f) } }
    @Test fun twoHundredLevelsHaveUnassistedSolutionAndNoOverlaps() {
        for(level in 1..200) {
            val g=Game(level);assertTrue(g.yard.size>=8)
            val cells=g.yard.flatMap { it.cells() };assertEquals(cells.size,cells.toSet().size)
            for(id in g.solution) { assertEquals("Level $level train $id",Result.MOVED,g.dispatch(id));advance(g,12f);assertFalse(g.failed) }
            advance(g,2f);assertTrue("Level $level",g.completed);assertEquals(g.total,g.boarded)
        }
    }
    @Test fun blockingUsesFullTrainFootprint() {
        val g=Game(1);g.yard.clear();g.yard+=Train(1,0,0,0,Direction.RIGHT,2,8);g.yard+=Train(2,3,0,1,Direction.DOWN,2,8)
        assertEquals(Result.BLOCKED,g.dispatch(1));assertEquals(Result.MOVED,g.dispatch(2));assertEquals(Result.MOVED,g.dispatch(1))
    }
    @Test fun arrivalMustFinishBeforeBoarding() {
        val g=Game(1);assertEquals(Result.MOVED,g.dispatch(g.solution.first()));advance(g,.5f);assertEquals(0,g.boarded);advance(g,6f);assertTrue(g.boarded>0)
    }
    @Test fun fourWrongColorsCauseFailureAndRescueRestoresPlay() {
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(64){0}+List(16){1}
        for(i in 0..3) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        for(i in 0..3) assertEquals(Result.MOVED,g.dispatch(i))
        advance(g,3f);assertTrue(g.failed);assertEquals(0,g.boarded);assertTrue(g.rescue());assertFalse(g.failed);assertEquals(5,g.platformCount);assertEquals(64,g.robots.size)
    }
    @Test fun matchingFullPlatformsKeepBoarding() {
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(16){0}
        for(i in 0..3) g.yard+=Train(i,i,0,0,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};advance(g,8f);assertFalse(g.failed);assertTrue(g.completed)
    }
    @Test fun fullDispatchDoesNotLoseVehicle() {
        val g=Game(1);g.yard.clear();for(i in 0..4) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};assertEquals(Result.FULL,g.dispatch(4));assertEquals(1,g.yard.size)
    }
    @Test fun boostersConservePassengerDemand() {
        val g=Game(12)
        fun checkDemand() { assertEquals(g.yard.sumOf { it.remaining }+g.platforms.filterNotNull().sumOf { it.remaining },g.robots.size);assertEquals(g.total,g.boarded+g.robots.size) }
        g.dispatch(g.solution.first());advance(g,.9f);checkDemand();assertTrue(g.arrange());checkDemand();assertTrue(g.shuffle());checkDemand();assertTrue(g.rescue());checkDemand();if(g.vip()) checkDemand()
    }
    @Test fun unmatchedRobotsCirculateWithoutChangingIdentity() {
        val g=Game(2);val robot=g.passengers.first();val phase=robot.phase
        advance(g,.4f);assertSame(robot,g.passengers.first());assertTrue(robot.phase>phase);assertEquals(0,g.boarded)
        advance(g,6f);assertSame(robot,g.passengers.first());assertEquals(g.total,g.robots.size);assertFalse(g.failed)
    }
    @Test fun onlyMatchingGateCrossingsBoardAndOtherPositionsStayContinuous() {
        val g=Game(1);val id=g.solution.first();val color=g.yard.first { it.id==id }.color;g.dispatch(id)
        val other=g.passengers[15];val previous=other.phase;advance(g,.9f)
        assertTrue(g.boarded==0);assertSame(other,g.passengers[15]);assertTrue(other.phase!=previous)
        advance(g,12f);assertTrue(g.boarded>0);assertTrue(g.boarding.all { it.color==color } || g.boarding.isEmpty())
    }
    @Test fun incomingRobotsLeaveSpaceAtTheirMergePoint() {
        val g=Game(40)
        repeat(1000) {
            g.update(.02f)
            for(lane in 0..3) {
                val phases=g.passengers.filter { it.lane==lane && it.phase>=0f }.map { it.phase }.sorted()
                phases.zipWithNext().forEach { (a,b) -> assertTrue(b-a>=.049f) }
                if(phases.size>1) assertTrue(1f-phases.last()+phases.first()>=.049f)
            }
        }
    }
    @Test fun oldCheckpointMigratesWithoutLosingPassengers() {
        val g=Game(7);g.dispatch(g.solution.first());advance(g,.9f)
        val old=g.checkpoint().split('|').take(11).toMutableList();old[0]="v1"
        val restored=Game.restore(old.joinToString("|"))!!
        assertEquals(g.boarded,restored.boarded);assertEquals(g.robots,restored.robots)
        assertEquals(g.robots.size,restored.passengers.size);assertTrue(restored.checkpoint().startsWith("v2|"))
    }
    @Test fun rotationSpeedIsIndependentOfUpdateFrequency() {
        val a=Game(2);val b=Game(2)
        repeat(100) { a.update(.01f) };repeat(50) { b.update(.02f) }
        a.passengers.zip(b.passengers).forEach { (x,y) -> assertEquals(x.phase,y.phase,.004f) }
    }
    @Test fun checkpointRestoresMovesInventoryAndDemand() {
        val g=Game(9);g.dispatch(g.solution.first());advance(g,1f)
        val restored=Game.restore(g.checkpoint())!!
        assertEquals(g.checkpoint(),restored.checkpoint());assertEquals(g.robots,restored.robots)
        advance(restored,3f);assertFalse(restored.failed)
        assertNull(Game.restore("broken"))
    }
}

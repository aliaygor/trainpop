package com.axioapps.trainpop
import org.junit.Assert.*
import org.junit.Test
class GameTest {
    private fun advance(g:Game,seconds:Float) { repeat((seconds/.02f).toInt()) { g.update(.02f) } }
    @Test fun twoHundredLevelsHaveUnassistedSolutionAndNoOverlaps() {
        for(level in 1..200) {
            val g=Game(level);assertTrue(g.yard.size>=8)
            val cells=g.yard.flatMap { it.cells() };assertEquals(cells.size,cells.toSet().size)
            for(id in g.solution) {
                assertEquals("Level $level train $id",Result.MOVED,g.dispatch(id))
                var waited=0f
                while(waited<45f && (g.platforms.any { it?.id==id } || g.departing.any { it.id==id })) { g.update(.02f);waited+=.02f }
                assertFalse(g.failed)
                assertTrue("Level $level train $id did not finish boarding",g.platforms.none { it?.id==id })
            }
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
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(176){0}+List(16){1}
        for(i in 0..3) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        for(i in 0..3) assertEquals(Result.MOVED,g.dispatch(i))
        advance(g,3f);assertTrue(g.failed);assertEquals(0,g.boarded);assertTrue(g.rescue());assertFalse(g.failed);assertEquals(5,g.platformCount);assertEquals(176,g.robots.size)
    }
    @Test fun matchingFullPlatformsKeepBoarding() {
        val g=Game(1);g.yard.clear();g.robots.clear();g.robots+=List(16){0}
        for(i in 0..3) g.yard+=Train(i,i,0,0,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};advance(g,24f);assertFalse(g.failed);assertTrue(g.completed)
    }
    @Test fun fullDispatchDoesNotLoseVehicle() {
        val g=Game(1);g.yard.clear();for(i in 0..4) g.yard+=Train(i,i,0,1,Direction.DOWN,1,4)
        repeat(4){g.dispatch(it)};assertEquals(Result.FULL,g.dispatch(4));assertEquals(1,g.yard.size)
    }
    @Test fun boostersConservePassengerDemand() {
        val g=Game(12)
        fun checkDemand() { assertEquals(g.yard.sumOf { it.remaining }+g.platforms.filterNotNull().sumOf { it.remaining },g.robots.size);assertEquals(g.total,g.boarded+g.robots.size) }
        g.dispatch(g.solution.first());advance(g,1.1f);checkDemand();assertTrue(g.arrange());checkDemand();assertTrue(g.shuffle());checkDemand();assertTrue(g.rescue());checkDemand();if(g.vip()) checkDemand()
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
                val phases=g.passengers.filter { it.lane%4==lane && it.phase>=0f }.map { it.phase }.sorted()
                phases.zipWithNext().forEach { (a,b) -> assertTrue(b-a>=.0215f) }
                if(phases.size>1) assertTrue(1f-phases.last()+phases.first()>=.0215f)
            }
        }
    }
    @Test fun oldCheckpointMigratesWithoutLosingPassengers() {
        val g=Game(7);g.dispatch(g.solution.first());advance(g,.9f)
        val old=g.checkpoint().split('|').take(11).toMutableList();old[0]="v1"
        val restored=Game.restore(old.joinToString("|"))!!
        assertEquals(g.boarded,restored.boarded);assertEquals(g.robots,restored.robots)
        assertEquals(g.robots.size,restored.passengers.size);assertTrue(restored.checkpoint().startsWith("v5|"))
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
    @Test fun firstFrameHasFourDenseRowsAcrossTheEntireLoop() {
        val g=Game(2);val active=g.passengers.filter { it.phase>=0 }
        assertEquals(176,active.size)
        for(lane in 0..3) {
            val phases=active.filter { it.lane%4==lane }.map { it.phase }.sorted()
            assertEquals(44,phases.size)
            phases.zipWithNext().forEach { (a,b) -> assertEquals(1f/44f,b-a,.0001f) }
            assertEquals(1f/44f,1f-phases.last()+phases.first(),.0001f)
        }
    }
    @Test fun legacyLowDensitySaveKeepsLevelAndTrainDemandWhenUpgraded() {
        val original=Game(7);val parts=original.checkpoint().split('|').toMutableList();parts[0]="v2"
        parts[7]=parts[7].split(';').joinToString(";") { row ->
            val fields=row.split(',').toMutableList();fields[6]=(fields[6].toInt()/4).toString();fields[7]=(fields[7].toInt()/4).toString();fields.joinToString(",")
        }
        val oldColors=original.robots.chunked(4).map { it.first() };parts[10]=oldColors.joinToString(",")
        parts[11]=oldColors.mapIndexed { i,color -> "$color,${if(i<32) ((.8f-i/4*.0625f)%1f+1f)%1f else -1f},${i%4}" }.joinToString(";")
        val restored=Game.restore(parts.joinToString("|"))!!
        assertEquals(7,restored.level);assertEquals(original.total,restored.total)
        assertEquals(original.robots,restored.robots)
        assertEquals(restored.total,restored.yard.sumOf { it.remaining })
        assertEquals(176,restored.passengers.count { it.phase>=0 })
    }

    @Test fun selectedRouteReservesOnlyItsOwnDock() {
        val g=Game(2);assertEquals(Result.MOVED,g.dispatch(g.solution.first(),2))
        assertNotNull(g.platforms[2]);assertNull(g.platforms[0])
        val next=g.solution[1];assertEquals(Result.FULL,g.dispatch(next,2))
        assertTrue(g.yard.any { it.id==next });assertEquals(Result.MOVED,g.dispatch(next,0))
    }

    @Test fun difficultyBuildsGraduallyAndSeparatesNewColorsFromDensity() {
        var previous=Difficulty.forLevel(1)
        for(level in 1..80) {
            val d=Difficulty.forLevel(level)
            assertTrue(d.trains-previous.trains in 0..1)
            assertTrue(d.colors-previous.colors in 0..1)
            if(d.colors>previous.colors) assertEquals(previous.trains,d.trains)
            assertTrue(d.blockedExits-previous.blockedExits<=2)
            if(level<=8) assertEquals(3,d.colors)
            previous=d
        }
        assertEquals(10,Difficulty.forLevel(9).trains)
        assertEquals(14,Difficulty.forLevel(20).trains)
        assertEquals(24,Difficulty.forLevel(49).trains)
        for(level in listOf(1,4,9,15,23,41)) {
            val g=Game(level);val d=Difficulty.forLevel(level)
            assertEquals(d.trains,g.yard.size);assertEquals(d.colors,g.yard.map { it.color }.toSet().size)
        }
    }
    @Test fun savesRetainDemandFromOlderLevelLayouts() {
        val g=Game(3)
        val oldTrain=g.yard.removeAt(0)
        repeat(oldTrain.capacity) { g.robots.remove(oldTrain.color) }
        // This represents an older generator with fewer trains, not a paid booster.
        val parts=g.checkpoint().split('|').toMutableList()
        parts[11]=""
        parts[0]="v3"
        val restored=Game.restore(parts.joinToString("|"))!!
        assertEquals(g.robots.size,restored.total)
        assertEquals(g.yard,restored.yard)
        assertEquals(g.robots,restored.robots)
        assertEquals(restored.yard.size,restored.solution.size)
    }
    @Test fun waitingRowsEnterFourAtOnceWithoutOvertaking() {
        val g=Game(20);g.dispatch(g.solution.first())
        var admissions=0
        repeat(2500) {
            val waiting=g.passengers.filter { it.phase<0f }
            g.update(.02f)
            val entered=waiting.filter { it.phase>=0f }
            if(entered.isNotEmpty()) {
                assertEquals(4,entered.size)
                assertEquals(waiting.take(4),entered)
                assertEquals(setOf(0,1,2,3),entered.map { it.lane%4 }.toSet())
                assertTrue(entered.all { it.phase==entered[0].phase });admissions++
            }
        }
        assertTrue(admissions>0)
    }
    @Test fun automaticDispatchContinuesWhenPreviousDockIsOccupied() {
        val g=Game(3)
        assertEquals(Result.MOVED,g.dispatch(g.solution[0]))
        assertEquals(Result.MOVED,g.dispatch(g.solution[1]))
        assertNotNull(g.platforms[0]);assertNotNull(g.platforms[1])
    }
    @Test fun rewardedFlowSpeedChangesOnlyTheCrowdAndPersistsForThisLevel() {
        val normal=Game(2);val boosted=Game(2)
        assertFalse(boosted.activateFlowSpeed(4));assertTrue(boosted.activateFlowSpeed(3))
        assertFalse(boosted.activateFlowSpeed(2));assertEquals(1,Game(3).flowSpeed)
        val id=normal.solution.first();normal.dispatch(id);boosted.dispatch(id)
        val p=normal.passengers.first().phase
        normal.update(.1f);boosted.update(.1f)
        assertEquals(normal.platforms[0]!!.progress,boosted.platforms[0]!!.progress,0f)
        assertEquals(.005f,normal.passengers.first().phase-p,.0001f)
        assertEquals(.015f,boosted.passengers.first().phase-p,.0001f)
        val restored=Game.restore(boosted.checkpoint())!!
        assertEquals(3,restored.flowSpeed);assertEquals(boosted.checkpoint(),restored.checkpoint())
        val old=normal.checkpoint().split('|').take(12).joinToString("|")
        assertEquals(1,Game.restore(old)!!.flowSpeed)
    }
    @Test fun tripleSpeedKeepsFourWideEntrySpacingAndPassengerDemand() {
        val g=Game(25);assertTrue(g.activateFlowSpeed(3));g.dispatch(g.solution.first())
        repeat(1200) {
            g.update(.02f)
            assertEquals(g.total,g.boarded+g.robots.size)
            for(lane in 0..3) {
                val phases=g.passengers.filter { it.lane%4==lane && it.phase>=0f }.map { it.phase }.sorted()
                phases.zipWithNext().forEach { (a,b) -> assertTrue(b-a>=.0215f) }
            }
        }
    }
}

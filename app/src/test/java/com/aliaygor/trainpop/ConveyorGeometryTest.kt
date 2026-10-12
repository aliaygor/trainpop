package com.aliaygor.trainpop

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.hypot

class ConveyorGeometryTest {
    @Test fun wrapAndMergePointsAreContinuous() {
        val before=ConveyorGeometry.position(.99999f,1);val after=ConveyorGeometry.position(0f,1)
        assertTrue(hypot(before.x-after.x,before.y-after.y)<.02f)
        assertEquals(67f,ConveyorGeometry.position(.25f,1).x,.01f)
        assertEquals(205f,ConveyorGeometry.position(.75f,2).y,.01f)
    }
    @Test fun bendsKeepNearlyTheSameDistancePerFrame() {
        val distances=(0 until 200).map { i ->
            val a=ConveyorGeometry.position(i/200f,1);val b=ConveyorGeometry.position((i+1)/200f,1)
            hypot(a.x-b.x,a.y-b.y)
        }
        assertTrue(distances.max()/distances.min()<1.06f)
    }
    @Test fun oneFullLapTakesTwentySeconds() {
        val game=Game(2);val robot=game.passengers.first();val start=robot.phase
        repeat(1000) { game.update(.02f) }
        assertEquals(start,robot.phase,.0001f)
        assertSame(robot,game.passengers.first())
    }
}

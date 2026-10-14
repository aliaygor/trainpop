package com.axioapps.trainpop
import kotlin.math.*
data class FlowPoint(val x:Float,val y:Float)
/** Per-lane arc lengths preserve ground speed even on the compact station loop. */
object ConveyorGeometry {
    const val inletEndY=118f
    private const val samples=512
    private fun raw(turn:Float,lane:Int):FlowPoint {
        val a=PI/2+turn*2*PI;val offset=(lane%4-1.5f)*15.5f
        return FlowPoint(cos(a).toFloat()*(139f+offset),sin(a).toFloat()*(60f+offset))
    }
    private val points=Array(4) { lane -> Array(samples+1) { i -> raw(i.toFloat()/samples,lane) } }
    private val distances=Array(4) { lane -> FloatArray(samples+1).also { table ->
        var previous=points[lane][0]
        for(i in 1..samples) { val p=points[lane][i];table[i]=table[i-1]+hypot(p.x-previous.x,p.y-previous.y);previous=p }
        val perimeter=table.last();for(i in table.indices) table[i]/=perimeter
    } }
    fun position(phase:Float,lane:Int):FlowPoint {
        val fraction=((phase%1f)+1f)%1f;val table=distances[lane%4]
        var low=0;var high=samples
        while(high-low>1) { val mid=(low+high)/2;if(table[mid]<fraction) low=mid else high=mid }
        val blend=(fraction-table[low])/(table[high]-table[low])
        val a=points[lane%4][low];val b=points[lane%4][high]
        return FlowPoint(200f+a.x+(b.x-a.x)*blend,239f+a.y+(b.y-a.y)*blend)
    }
}

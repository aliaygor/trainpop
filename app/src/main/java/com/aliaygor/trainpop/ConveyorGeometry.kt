package com.aliaygor.trainpop

import kotlin.math.*

data class FlowPoint(val x:Float,val y:Float)

/** Arc-length lookup keeps the crowd's ground speed steady through curved sections. */
object ConveyorGeometry {
    private const val samples=512
    private val distance=FloatArray(samples+1).also { table ->
        var previous=raw(0f)
        for(i in 1..samples) {
            val point=raw(i.toFloat()/samples)
            table[i]=table[i-1]+hypot(point.x-previous.x,point.y-previous.y)
            previous=point
        }
        val perimeter=table.last()
        for(i in table.indices) table[i]/=perimeter
    }
    private fun raw(turn:Float):FlowPoint {
        val angle=PI/2+turn*2*PI
        return FlowPoint(cos(angle).toFloat()*139f,sin(angle).toFloat()*86f)
    }
    fun position(phase:Float,lane:Int):FlowPoint {
        val fraction=((phase%1f)+1f)%1f
        var low=0;var high=samples
        while(high-low>1) { val middle=(low+high)/2;if(distance[middle]<fraction) low=middle else high=middle }
        val blend=(fraction-distance[low])/(distance[high]-distance[low])
        val angle=PI/2+(low+blend)/samples*PI*2
        val offset=(lane%4-1.5f)*17f
        return FlowPoint(200f+cos(angle).toFloat()*(139f+offset),205f+sin(angle).toFloat()*(86f+offset))
    }
}

package com.axioapps.trainpop

import kotlin.random.Random

enum class Direction(val dx: Int, val dy: Int, val angle: Float) {
    UP(0, -1, 0f), RIGHT(1, 0, 90f), DOWN(0, 1, 180f), LEFT(-1, 0, 270f)
}
enum class TrainPhase { YARD, ARRIVING, PARKED, DEPARTING }
data class Cell(val x: Int, val y: Int)
data class Train(
    val id: Int, val x: Int, val y: Int, var color: Int, val direction: Direction,
    val length: Int, val capacity: Int, var remaining: Int = capacity,
    var phase: TrainPhase = TrainPhase.YARD, var progress: Float = 0f, var slot: Int = -1,
) {
    fun cells(): List<Cell> = (0 until length).map {
        if (direction.dx != 0) Cell(x + it, y) else Cell(x, y + it)
    }
}
enum class Result { MOVED, BLOCKED, FULL, BUSY }
data class Passenger(val color: Int, var phase: Float, val lane: Int)
data class Boarding(val color: Int, val slot: Int, var age: Float = 0f, val lane: Int = 1)

/** A bounded conveyor feeds a circulating crowd. Only robots crossing its gate may board. */
class Game(val level: Int) {
    val yard = mutableListOf<Train>()
    val platforms = arrayOfNulls<Train>(7)
    val robots = mutableListOf<Int>()
    val departing = mutableListOf<Train>()
    val boarding = mutableListOf<Boarding>()
    val solution = mutableListOf<Int>()
    var platformCount = 4; private set
    var boarded = 0; private set
    var flowSpeed = 1; private set
    fun activateFlowSpeed(multiplier:Int):Boolean {
        if(multiplier !in 2..3 || multiplier<=flowSpeed || completed) return false
        flowSpeed=multiplier;return true
    }
    var elapsed = 0f; private set
    var moves = 0; private set
    var failed = false; private set
    var completed = false; private set
    val passengers = mutableListOf<Passenger>()
    private var feedClock = 0f
    private val circulating = Array(4) { ArrayList<Passenger>(44) }
    private val laneSpeeds = Array(4) { FloatArray(176) }
    private val feedRow = ArrayList<Passenger>(4)
    private val phaseOrder = Comparator<Passenger> { a,b -> a.phase.compareTo(b.phase) }

    private fun resetFlow() {
        passengers.clear()
        robots.forEachIndexed { i, color -> passengers += Passenger(color, if(i<176) ((.84f-i/4*(1f/44f))%1f+1f)%1f else -1f, i%8) }
    }
    private var blockedFor = 0f
    var total: Int
        private set
    private val random = Random(level * 719 + 83)
    init {
        val difficulty=Difficulty.forLevel(level)
        val count=difficulty.trains
        val colors=difficulty.colors
        val target=difficulty.blockedExits
        // Reverse insertion keeps a legal solution; the profile separates new
        // colors from train-count increases and provides regular lighter boards.
        var best = emptyList<Train>()
        var bestScore = Int.MIN_VALUE
        for(attempt in 0 until 64) {
            yard.clear()
            val occupied=BooleanArray(36)
            repeat(600) {
                if (yard.size < count) {
                    val d = Direction.entries.random(random)
                    val len = if (random.nextFloat() < .42f) 2 else 1
                    val x = random.nextInt(if (d.dx != 0) 7 - len else 6)
                    val y = random.nextInt(if (d.dy != 0) 7 - len else 6)
                    val t = Train(yard.size, x, y, 0, d, len, len * 16)
                    val fits=(0 until len).all { i -> !occupied[(y+if(d.dy!=0) i else 0)*6+x+if(d.dx!=0) i else 0] }
                    if(fits && canExit(t)) {
                        yard+=t
                        for(i in 0 until len) occupied[(y+if(d.dy!=0) i else 0)*6+x+if(d.dx!=0) i else 0]=true
                    }
                }
            }
            val blocked = yard.count { !canExit(it) }
            val score = yard.size * 1000 - kotlin.math.abs(blocked - target) * 100
            if (score > bestScore) { bestScore = score; best = yard.map { it.copy() } }
            if(bestScore==count*1000) break
        }
        yard.clear(); yard += best
        val palette = (0 until colors).shuffled(random)
        yard.forEachIndexed { i, t -> t.color = palette[i % colors] }
        solution += yard.asReversed().map { it.id }
        solution.forEach { id -> val t = yard.first { it.id == id }; repeat(t.capacity) { robots += t.color } }
        total = robots.size
        resetFlow()
    }
    fun canExit(t: Train): Boolean {
        for(other in yard) if(other.id!=t.id) {
            for(i in 0 until other.length) {
                val cx=other.x+if(other.direction.dx!=0) i else 0
                val cy=other.y+if(other.direction.dy!=0) i else 0
                for(j in 0 until t.length) {
                    val bx=t.x+if(t.direction.dx!=0) j else 0
                    val by=t.y+if(t.direction.dy!=0) j else 0
                    if(when(t.direction) {
                        Direction.LEFT -> cy==by && cx<bx
                        Direction.RIGHT -> cy==by && cx>bx
                        Direction.UP -> cx==bx && cy<by
                        Direction.DOWN -> cx==bx && cy>by
                    }) return false
                }
            }
        }
        return true
    }
    fun dispatch(id: Int, preferredSlot:Int?=null): Result {
        if (failed || completed) return Result.BUSY
        val t = yard.firstOrNull { it.id == id } ?: return Result.BUSY
        if (!canExit(t)) return Result.BLOCKED
        val free=(0 until platformCount).filter { platforms[it]==null && departing.none { t -> t.slot==it } }
        val slot=if(preferredSlot==null) free.firstOrNull() else preferredSlot.takeIf { it in free }
        if(slot==null) return Result.FULL
        yard.remove(t); t.slot = slot; t.phase = TrainPhase.ARRIVING; t.progress = 0f
        platforms[slot] = t; moves++
        return Result.MOVED
    }
    fun update(dt: Float) {
        if (completed || failed) return
        elapsed += dt
        boarding.forEach { it.age += dt }; boarding.removeAll { it.age > .5f }
        departing.forEach { it.progress += dt / .95f }; departing.removeAll { it.progress >= 1f }
        platforms.forEach { t ->
            if(t==null) return@forEach
            if (t.phase == TrainPhase.ARRIVING) {
                t.progress = (t.progress + dt / 1.05f).coerceAtMost(1f)
                if (t.progress >= 1f) t.phase = TrainPhase.PARKED
            }
        }
        if(passengers.size != robots.size) resetFlow()
        circulating.forEach { it.clear() }
        var waitingForEntry=false
        var activeCount=0
        for(robot in passengers) {
            if(robot.phase>=0f) { circulating[robot.lane%4].add(robot);activeCount++ }
            else waitingForEntry=true
        }
        for(lane in 0..3) {
            val ordered=circulating[lane]
            if(!waitingForEntry) ordered.sortWith(phaseOrder)
            for(i in ordered.indices) {
                val robot=ordered[i]
                val next=ordered[(i+1)%ordered.size]
                val gap=if(ordered.size==1) 1f else (next.phase-robot.phase+1f)%1f
                laneSpeeds[lane][i]=if(!waitingForEntry && ordered.size<42) .05f+((gap-1f/44f).coerceAtLeast(0f)*.4f).coerceAtMost(.04f) else .05f
            }
        }
        for(lane in 0..3) for(i in circulating[lane].indices) {
            val robot=circulating[lane][i]
            robot.phase+=dt*flowSpeed*laneSpeeds[lane][i]
            if(robot.phase>=1f) {
                robot.phase-=1f
                val t=platforms.firstOrNull { it?.phase==TrainPhase.PARKED && it.color==robot.color }
                if(t!=null) {
                    robot.phase=-2f;check(robots.remove(robot.color));t.remaining--;boarded++;activeCount--
                    boarding+=Boarding(robot.color,t.slot,lane=robot.lane)
                    if(t.remaining==0) { platforms[t.slot]=null;t.phase=TrainPhase.DEPARTING;t.progress=0f;departing+=t }
                }
            }
        }
        passengers.removeAll { it.phase == -2f }
        feedClock+=dt*flowSpeed
        if(feedClock>=.12f && activeCount<=172) {
            feedRow.clear()
            for(robot in passengers) if(robot.phase<0f && feedRow.size<4) feedRow.add(robot)
            // A whole four-wide row enters together. Never skip a blocked lane
            // or insert later colors ahead of the first waiting row.
            val clear=feedRow.isNotEmpty() && feedRow.all { next ->
                val entry=if(next.lane<4) .38f else .62f
                circulating[next.lane%4].none { other ->
                    val distance=kotlin.math.abs(other.phase-entry)
                    other.phase>=0f && kotlin.math.min(distance,1f-distance)<.0218f
                }
            }
            if(clear) { feedRow.forEach { it.phase=if(it.lane<4) .38f else .62f };feedClock%=.12f }
        }
        val inTransit = platforms.any { it?.phase == TrainPhase.ARRIVING } || departing.isNotEmpty()
        val activeColors = passengers.filter { it.phase >= 0f }.map { it.color }.toSet()
        val locked = (0 until platformCount).all { platforms[it] != null } && activeColors.isNotEmpty() && platforms.none { it?.color in activeColors }
        blockedFor = if(locked && !inTransit) blockedFor + dt else 0f
        failed = blockedFor > 1.4f
        completed = yard.isEmpty() && robots.isEmpty() && platforms.all { it == null } && departing.isEmpty() && boarding.isEmpty()
    }
    fun addPlatform(): Boolean {
        if (platformCount == platforms.size) return false
        platformCount++; failed = false; blockedFor = 0f; return true
    }
    fun arrange(): Boolean {
        val color = platforms.firstOrNull { it?.phase == TrainPhase.PARKED }?.color ?: return false
        val matches = robots.filter { it == color }
        if (matches.isEmpty()) return false
        val rest = robots.filter { it != color }
        robots.clear(); robots.addAll(matches); robots.addAll(rest)
        resetFlow(); failed = false; blockedFor = 0f; return true
    }
    fun shuffle(): Boolean {
        if (yard.size < 2) return false
        val old = yard.map { it.copy() }
        val colors = old.map { it.color }.shuffled(random)
        yard.clear(); old.forEachIndexed { i, t -> yard += t.copy(color = colors[i], remaining = t.capacity) }
        val parkedDemand = platforms.filterNotNull().flatMap { t -> List(t.remaining) { t.color } }
        val demand = solution.mapNotNull { id -> yard.firstOrNull { it.id == id } }.flatMap { t -> List(t.capacity) { t.color } }
        robots.clear(); robots += parkedDemand; robots += demand
        resetFlow(); failed = false; blockedFor = 0f; return true
    }
    fun vip(): Boolean {
        val color = robots.firstOrNull() ?: return false
        val t = yard.firstOrNull { it.color == color } ?: return false
        yard.remove(t)
        repeat(t.remaining) { check(robots.remove(t.color)); boarded++ }
        departing += t.copy(phase = TrainPhase.DEPARTING, slot = -1, progress = 0f)
        resetFlow(); failed = false; blockedFor = 0f; return true
    }
    fun rescue(): Boolean {
        val parked = platforms.filterNotNull()
        if (parked.isEmpty()) return false
        parked.forEach { t ->
            repeat(t.remaining) { check(robots.remove(t.color)); boarded++ }
            platforms[t.slot] = null; t.phase = TrainPhase.DEPARTING; t.progress = 0f; departing += t
        }
        if (platformCount < platforms.size) platformCount++
        resetFlow(); failed = false; blockedFor = 0f; return true
    }
    fun checkpoint(): String {
        fun trains(values: List<Train>) = values.joinToString(";") { t -> listOf(t.id,t.x,t.y,t.color,t.direction.ordinal,t.length,t.capacity,t.remaining,t.phase.ordinal,t.progress,t.slot).joinToString(",") }
        return listOf("v5",level,boarded,moves,platformCount,elapsed,if(failed) 1 else 0,trains(yard),trains(platforms.filterNotNull()),trains(departing),robots.joinToString(","),passengers.joinToString(";") { "${it.color},${it.phase},${it.lane}" },flowSpeed).joinToString("|")
    }
    companion object {
        fun restore(checkpoint:String):Game? = try {
            val parts=checkpoint.split('|');require((parts.size==11 && parts[0]=="v1") || (parts.size in 12..13 && parts[0] in setOf("v2","v3","v4","v5")))
            val g=Game(parts[1].toInt());g.boarded=parts[2].toInt();g.moves=parts[3].toInt();g.platformCount=parts[4].toInt();g.elapsed=parts[5].toFloat();g.failed=parts[6]=="1"
            require(g.platformCount in 4..7 && g.boarded >= 0)
            fun trains(s:String):List<Train> = if(s.isEmpty()) emptyList() else s.split(';').map { row->val v=row.split(',');require(v.size==11)
                Train(v[0].toInt(),v[1].toInt(),v[2].toInt(),v[3].toInt(),Direction.entries[v[4].toInt()],v[5].toInt(),v[6].toInt(),v[7].toInt(),TrainPhase.entries[v[8].toInt()],v[9].toFloat(),v[10].toInt()).also { require(it.color in 0..5 && it.remaining in 0..it.capacity) }
            }
            g.yard.clear();g.yard+=trains(parts[7]);g.platforms.fill(null)
            trains(parts[8]).forEach { require(it.slot in 0 until g.platformCount && g.platforms[it.slot]==null);g.platforms[it.slot]=it }
            g.departing+=trains(parts[9]);g.robots.clear();if(parts[10].isNotEmpty()) g.robots+=parts[10].split(',').map { it.toInt().also { c->require(c in 0..5) } }
            // Upgrade pre-density sessions while keeping level, coins and dispatched trains.
            val factor=if((g.yard + g.platforms.filterNotNull() + g.departing).let { it.isNotEmpty() && it.all { t -> t.capacity <= 8 } }) 4 else 1
            if(factor==4) {
                g.boarded*=4
                val oldYard=g.yard.toList();g.yard.clear();g.yard+=oldYard.map { it.copy(capacity=it.capacity*4,remaining=it.remaining*4) }
                g.platforms.indices.forEach { i -> g.platforms[i]=g.platforms[i]?.let { it.copy(capacity=it.capacity*4,remaining=it.remaining*4) } }
                val oldDeparting=g.departing.toList();g.departing.clear();g.departing+=oldDeparting.map { it.copy(capacity=it.capacity*4,remaining=it.remaining*4) }
                val oldRobots=g.robots.toList();g.robots.clear();g.robots+=oldRobots.flatMap { List(4) { _ -> it } }
            }
            // Saved sessions retain their original demand across generator upgrades.
            g.total = g.robots.size + g.boarded
            require(g.total > 0)
            val remainingYard = g.yard.toList()
            g.solution.clear()
            while (g.yard.isNotEmpty()) {
                val next = g.yard.firstOrNull { g.canExit(it) } ?: error("Blocked saved yard")
                g.solution += next.id; g.yard.remove(next)
            }
            g.yard += remainingYard
            require(g.yard.sumOf { it.remaining }+g.platforms.filterNotNull().sumOf { it.remaining }==g.robots.size)
            val expected=IntArray(6);g.yard.forEach { expected[it.color]+=it.remaining };g.platforms.filterNotNull().forEach { expected[it.color]+=it.remaining }
            require(expected.indices.all { c->expected[c]==g.robots.count { it==c } })
            g.resetFlow()
            if(parts[0]=="v5" && factor==1) {
                g.passengers.clear()
                if(parts[11].isNotEmpty()) parts[11].split(';').forEach { row -> val v=row.split(','); require(v.size==3)
                    g.passengers += Passenger(v[0].toInt(),v[1].toFloat(),v[2].toInt()).also { require(it.phase.isFinite() && it.phase >= -1f && it.phase < 1f && it.lane in 0..7) }
                }
                require(g.passengers.groupingBy { it.color }.eachCount()==g.robots.groupingBy { it }.eachCount())
            }
            if(parts.size==13 && parts[0]=="v5") { g.flowSpeed=parts[12].toInt();require(g.flowSpeed in 1..3) }
            g
        } catch(_:Exception) { null }
    }
}

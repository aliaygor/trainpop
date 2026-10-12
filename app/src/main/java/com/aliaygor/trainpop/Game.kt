package com.aliaygor.trainpop

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
    var elapsed = 0f; private set
    var moves = 0; private set
    var failed = false; private set
    var completed = false; private set
    val passengers = mutableListOf<Passenger>()
    private var feedClock = 0f
    private fun resetFlow() {
        passengers.clear()
        robots.forEachIndexed { i, color -> passengers += Passenger(color, if(i<32) ((.8f-i/4*.0625f)%1f+1f)%1f else -1f, i%4) }
    }
    private var blockedFor = 0f
    val total: Int
    private val random = Random(level * 719 + 83)
    init {
        val count = if (level == 1) 8 else (10 + level / 2).coerceAtMost(24)
        val colors = if (level < 3) 3 else (3 + level / 5).coerceAtMost(6)
        // Each new vehicle has a clear exit when placed. Reverse placement order is a solution.
        repeat(2400) {
            if (yard.size < count) {
                val d = Direction.entries.random(random)
                val len = if (random.nextFloat() < .42f) 2 else 1
                val x = random.nextInt(if (d.dx != 0) 7 - len else 6)
                val y = random.nextInt(if (d.dy != 0) 7 - len else 6)
                val t = Train(yard.size, x, y, random.nextInt(colors), d, len, len * 4)
                if (t.cells().none { cell -> yard.any { cell in it.cells() } } && canExit(t)) yard += t
            }
        }
        solution += yard.asReversed().map { it.id }
        solution.forEach { id -> val t = yard.first { it.id == id }; repeat(t.capacity) { robots += t.color } }
        total = robots.size
        resetFlow()
    }
    fun canExit(t: Train): Boolean = yard.none { other -> other.id != t.id && other.cells().any { cell -> t.cells().any { base ->
        when (t.direction) {
            Direction.LEFT -> cell.y == base.y && cell.x < base.x
            Direction.RIGHT -> cell.y == base.y && cell.x > base.x
            Direction.UP -> cell.x == base.x && cell.y < base.y
            Direction.DOWN -> cell.x == base.x && cell.y > base.y
        }
    } } }
    fun dispatch(id: Int): Result {
        if (failed || completed) return Result.BUSY
        val t = yard.firstOrNull { it.id == id } ?: return Result.BUSY
        if (!canExit(t)) return Result.BLOCKED
        val slot = (0 until platformCount).firstOrNull { platforms[it] == null && departing.none { t -> t.slot == it } } ?: return Result.FULL
        yard.remove(t); t.slot = slot; t.phase = TrainPhase.ARRIVING; t.progress = 0f
        platforms[slot] = t; moves++
        return Result.MOVED
    }
    fun update(dt: Float) {
        if (completed || failed) return
        elapsed += dt
        boarding.forEach { it.age += dt }; boarding.removeAll { it.age > .5f }
        departing.forEach { it.progress += dt / .95f }; departing.removeAll { it.progress >= 1f }
        platforms.filterNotNull().forEach { t ->
            if (t.phase == TrainPhase.ARRIVING) {
                t.progress = (t.progress + dt / 1.05f).coerceAtMost(1f)
                if (t.progress >= 1f) t.phase = TrainPhase.PARKED
            }
        }
        if(passengers.size != robots.size) resetFlow()
        val removed = mutableListOf<Passenger>()
        passengers.filter { it.phase >= 0f }.forEach { robot ->
            robot.phase += dt * .05f
            if(robot.phase >= 1f) {
                robot.phase -= 1f
                val t = platforms.filterNotNull().firstOrNull { it.phase == TrainPhase.PARKED && it.color == robot.color }
                if(t != null) {
                    removed += robot; check(robots.remove(robot.color)); t.remaining--; boarded++
                    boarding += Boarding(robot.color, t.slot, lane = robot.lane)
                    if(t.remaining == 0) { platforms[t.slot] = null; t.phase = TrainPhase.DEPARTING; t.progress = 0f; departing += t }
                }
            }
        }
        passengers.removeAll(removed.toSet())
        feedClock += dt
        if(feedClock >= .09f && passengers.count { it.phase >= 0f } < 64) {
            passengers.firstOrNull { it.phase < 0f }?.let { next ->
                val entry=if(next.lane<2) .25f else .75f
                val clear=passengers.none { other ->
                    val distance=kotlin.math.abs(other.phase-entry)
                    other.phase>=0f && other.lane==next.lane && kotlin.math.min(distance,1f-distance)<.05f
                }
                if(clear) { next.phase=entry;feedClock %= .09f }
            }
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
        return listOf("v2",level,boarded,moves,platformCount,elapsed,if(failed) 1 else 0,trains(yard),trains(platforms.filterNotNull()),trains(departing),robots.joinToString(","),passengers.joinToString(";") { "${it.color},${it.phase},${it.lane}" }).joinToString("|")
    }
    companion object {
        fun restore(checkpoint:String):Game? = try {
            val parts=checkpoint.split('|');require((parts.size==11 && parts[0]=="v1") || (parts.size==12 && parts[0]=="v2"))
            val g=Game(parts[1].toInt());g.boarded=parts[2].toInt();g.moves=parts[3].toInt();g.platformCount=parts[4].toInt();g.elapsed=parts[5].toFloat();g.failed=parts[6]=="1"
            require(g.platformCount in 4..7 && g.boarded in 0..g.total)
            fun trains(s:String):List<Train> = if(s.isEmpty()) emptyList() else s.split(';').map { row->val v=row.split(',');require(v.size==11)
                Train(v[0].toInt(),v[1].toInt(),v[2].toInt(),v[3].toInt(),Direction.entries[v[4].toInt()],v[5].toInt(),v[6].toInt(),v[7].toInt(),TrainPhase.entries[v[8].toInt()],v[9].toFloat(),v[10].toInt()).also { require(it.color in 0..5 && it.remaining in 0..it.capacity) }
            }
            g.yard.clear();g.yard+=trains(parts[7]);g.platforms.fill(null)
            trains(parts[8]).forEach { require(it.slot in 0 until g.platformCount && g.platforms[it.slot]==null);g.platforms[it.slot]=it }
            g.departing+=trains(parts[9]);g.robots.clear();if(parts[10].isNotEmpty()) g.robots+=parts[10].split(',').map { it.toInt().also { c->require(c in 0..5) } }
            require(g.robots.size+g.boarded==g.total)
            require(g.yard.sumOf { it.remaining }+g.platforms.filterNotNull().sumOf { it.remaining }==g.robots.size)
            val expected=IntArray(6);g.yard.forEach { expected[it.color]+=it.remaining };g.platforms.filterNotNull().forEach { expected[it.color]+=it.remaining }
            require(expected.indices.all { c->expected[c]==g.robots.count { it==c } })
            g.resetFlow()
            if(parts[0]=="v2") {
                g.passengers.clear()
                if(parts[11].isNotEmpty()) parts[11].split(';').forEach { row -> val v=row.split(','); require(v.size==3)
                    g.passengers += Passenger(v[0].toInt(),v[1].toFloat(),v[2].toInt()).also { require(it.phase.isFinite() && it.phase >= -1f && it.phase < 1f && it.lane in 0..3) }
                }
                require(g.passengers.groupingBy { it.color }.eachCount()==g.robots.groupingBy { it }.eachCount())
            }
            g
        } catch(_:Exception) { null }
    }
}

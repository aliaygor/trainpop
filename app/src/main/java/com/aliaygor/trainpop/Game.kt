package com.aliaygor.trainpop

enum class Direction(val dx: Int, val dy: Int, val glyph: String) { UP(0,-1,"↑"), RIGHT(1,0,"→"), DOWN(0,1,"↓"), LEFT(-1,0,"←") }
data class Train(val id: Int, val x: Int, val y: Int, val color: Int, val direction: Direction, val capacity: Int = 4, var remaining: Int = capacity)
enum class Result { MOVED, BLOCKED, FULL }

/** Discrete puzzle rules, independent of Android and rendering. */
class Game(val level: Int) {
    val yard = mutableListOf<Train>()
    val platforms = arrayOfNulls<Train>(4)
    val robots = mutableListOf<Int>()
    var boarded = 0; private set
    var completed = false; private set
    val total: Int
    init {
        // Each row exits toward its nearest edge. A known edge-first solution exists.
        val colors = 3 + ((level - 1) / 4).coerceAtMost(2)
        for (row in 0..3) for (col in 0..3) {
            val id = row * 4 + col
            yard += Train(id, col, row, (id + level) % colors, if(col < 2) Direction.LEFT else Direction.RIGHT)
        }
        // Rotating queue: every train has exactly its capacity in matching passengers.
        yard.forEach { train -> repeat(train.capacity) { robots += train.color } }
        total = robots.size
    }
    fun canExit(t: Train): Boolean = yard.none { other -> other.id != t.id && when(t.direction) {
        Direction.LEFT -> other.y == t.y && other.x < t.x
        Direction.RIGHT -> other.y == t.y && other.x > t.x
        Direction.UP -> other.x == t.x && other.y < t.y
        Direction.DOWN -> other.x == t.x && other.y > t.y
    } }
    fun dispatch(id: Int): Result {
        val t = yard.firstOrNull { it.id == id } ?: return Result.BLOCKED
        if(!canExit(t)) return Result.BLOCKED
        val slot = platforms.indexOfFirst { it == null }
        if(slot < 0) return Result.FULL
        yard.remove(t); platforms[slot] = t
        return Result.MOVED
    }
    fun tick() {
        if(robots.isEmpty()) return
        val color = robots.removeAt(0)
        val slot = platforms.indexOfFirst { it?.color == color }
        if(slot >= 0) {
            val train = platforms[slot]!!
            train.remaining--; boarded++
            if(train.remaining == 0) platforms[slot] = null
        } else robots += color
        completed = robots.isEmpty() && yard.isEmpty() && platforms.all { it == null }
    }
    fun rescue(): Boolean {
        if(platforms.all { it == null }) return false
        platforms.indices.forEach { i -> platforms[i]?.let { t ->
            repeat(t.remaining) { robots.remove(t.color); boarded++ }; platforms[i] = null
        } }
        completed = robots.isEmpty() && yard.isEmpty()
        return true
    }
}

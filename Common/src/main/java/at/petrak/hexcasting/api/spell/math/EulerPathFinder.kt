package at.petrak.hexcasting.api.spell.math

import java.util.*
import kotlin.random.Random

object EulerPathFinder {
    /**
     * Find an alternative way to draw the given pattern, based on a random seed.
     */
    @JvmStatic
    fun findAltDrawing(original: HexPattern, seed: Long): HexPattern {
        // http://www.graph-magics.com/articles/euler.php

        val rand = Random(seed)

        val graph = toGraph(original)
        val oddNodes = graph.filter { (_, dirs) -> dirs.size % 2 == 1 }
        var current: HexCoord = when (oddNodes.size) {
            // An euler-walkable graph must have 0 odd nodes and start anywhere...
            0 -> graph.keys.random(rand)
            // or two, and start at one of them
            2 -> oddNodes.keys.random(rand)
            else -> throw IllegalStateException()
        }

        val stack = Stack<HexCoord>()
        val out = mutableListOf<HexCoord>()
        do {
            val exits = graph[current]!!
            if (exits.isEmpty()) {
                out.add(current)
                current = stack.pop()
            } else {
                stack.push(current)
                // This is where the random part happens, mostly
                val burnDir = exits.random(rand)
                exits.remove(burnDir)
                graph[current + burnDir]?.remove(burnDir * HexAngle.BACK)
                current += burnDir
            }
        } while (graph[current]?.isNotEmpty() == true || stack.isNotEmpty())
        out.add(current)

        val dirs = out.zipWithNext { a, b -> a.immediateDelta(b)!! }
        val angles = dirs.zipWithNext { a, b -> b.angleFrom(a) }
        return HexPattern(dirs[0], angles.toMutableList())
    }

    private fun toGraph(pat: HexPattern): HashMap<HexCoord, EnumSet<HexDir>> {
        val graph = HashMap<HexCoord, EnumSet<HexDir>>()

        var compass: HexDir = pat.startDir
        var cursor = HexCoord.Origin
        for (a in pat.angles) {
            // i hate kotlin
            graph.getOrPut(cursor) { EnumSet.noneOf(HexDir::class.java) }.add(compass)
            graph.getOrPut(cursor + compass) { EnumSet.noneOf(HexDir::class.java) }.add(compass * HexAngle.BACK)

            cursor += compass
            compass *= a
        }
        graph.getOrPut(cursor) { EnumSet.noneOf(HexDir::class.java) }.add(compass)
        graph.getOrPut(cursor + compass) { EnumSet.noneOf(HexDir::class.java) }.add(compass * HexAngle.BACK)

        return graph
    }
}

package me.ddevil.graph.search

import me.ddevil.graph.Graph
import me.ddevil.graph.Weighted
import me.ddevil.graph.transversePath

typealias Heuristics<V> = (vertex: V) -> Float

fun <V> zeroHeuristics(vertex: V) = 0F

fun <E, V> Graph<E, V>.aStar(
    from: V,
    to: V,
    heuristics: Heuristics<V> = ::zeroHeuristics
): List<V>? where E : Weighted {
    if (from == to) {
        return listOf(from)
    }
    val open = ArrayList<V>().apply {
        this += from
    }
    val closed = ArrayList<V>()
    val origin = HashMap<V, V>()
    val gScore = HashMap<V, Float>().apply {
        this[from] = 0F
    }
    val fScore = HashMap<V, Float>().apply {
        this[from] = heuristics(from)
    }
    while (open.isNotEmpty()) {
        val current = open.minBy { fScore[it] ?: Float.POSITIVE_INFINITY }!!
        val currentIndex = indexOf(current)
        if (current == to) {
            return transversePath(current, origin)
        }
        open -= current
        closed += current
        for ((neighborIndex, neighbor) in neighborsOf(current)) {
            val edge = edge(currentIndex, neighborIndex)!!
            val tentative = gScore[current]!! + edge.weight
            val currentNeighborGScore = gScore[neighbor] ?: Float.POSITIVE_INFINITY
            if (tentative < currentNeighborGScore) {
                origin[neighbor] = current
                gScore[neighbor] = tentative
                fScore[neighbor] = tentative + heuristics(neighbor)
                if (neighbor !in closed) {
                    open += neighbor
                }
            }
        }
    }

    return null
}
package me.ddevil.graph

import java.security.SecureRandom
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

fun main() {
    val graph = Graph<Unit, Int>()
    val total = 10
    for (i in 0 until total) {
        graph.addVertex(i)
    }
    val r = SecureRandom()
    for (i in 0 until total) {
        graph.connect(r.nextInt(total), r.nextInt(total), Unit)
    }
    graph.printTree()
    println(graph.depthFirstSearch(0, total - 1))
    println(graph.breadthFirstSearch(0, total - 1))
}

fun <E, V, T : Comparable<T>> Graph<E, V>.selectHighestVertexBy(
    selector: (V) -> T
): V {
    return vertices.maxBy(selector)!!
}

private fun <E> reconstructPath(element: E, map: Map<E, E>): List<E> {
    val list = ArrayList<E>()
    list += element
    var current: E? = element
    do {
        current = map[current]
        if (current != null) {
            list += current
        }
    } while (current != null)

    return list.reversed()
}

fun <E, V> Graph<E, V>.depthFirstSearch(
    from: V,
    to: V
) = search(from, to, Stack<V>()) {
    if (it.empty()) {
        return@search null
    }
    return@search it.pop()
}

fun <E, V> Graph<E, V>.breadthFirstSearch(
    from: V,
    to: V
) = search(from, to, PriorityQueue<V>()) { it.poll() }

fun <E, V, O> Graph<E, V>.search(
    from: V,
    to: V,
    open: O,
    next: (O) -> V?
): List<V>? where O : MutableCollection<V> {
    var current = from
    val visited = ArrayList<V>()
    val cameFrom = HashMap<V, V>()
    while (current != to) {
        visited += current
        val neighboors = edgesFrom(current)
        for ((edge, index) in neighboors) {
            val neighboor = this[index]
            if (neighboor in visited) {
                continue
            }
            println("Currently @ $current")
            cameFrom[neighboor] = current
            if (neighboor == to) {
                return reconstructPath(neighboor, cameFrom)
            }
            open += neighboor
        }
        val candidate = next(open)
        if (candidate == null) {
            break
        } else {
            current = candidate
        }

    }
    return null
}
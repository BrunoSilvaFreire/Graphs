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
) = search(from, to, PriorityQueue<V>()) {
    it.poll()
}

fun <E, V, O> Graph<E, V>.search(
    from: V,
    to: V,
    open: O,
    next: (O) -> V?
): List<V>? where O : MutableCollection<V> {
    if (from == to) {
        return listOf(from)
    }
    var current = from
    val visited = ArrayList<V>()
    println("Searching from $from to $to")
    val cameFrom = HashMap<V, V>()
    while (current != to) {
        visited += current
        val neighboors = edgesFrom(current)
        for ((edge, index) in neighboors) {
            val neighboor = this[index]
            if (neighboor in visited) {
                continue
            }
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

private fun <E, V> searchRecurse(graph: Graph<E, V>, current: V, index: Int, target: Int, output: MutableList<V>) {
    if (target == index) {
        output += current
    } else {
        for ((edge, vert) in graph.edgesFrom(current)) {
            searchRecurse(graph, graph[vert], index + 1, target, output)
        }
    }


}

fun <E, V> Graph<E, V>.searchAtRadius(reference: V, radius: Int): List<V> {
    val list = ArrayList<V>()
    searchRecurse(this, reference, 0, radius, list)
    return list
}


fun <E, V> Graph<E, V>.pathWithHighestWeight(
    a: V,
    b: V
): List<V>? where E : Weighted {
    val open = ArrayList<V>()
    val visited = ArrayList<V>()
    val score = HashMap<V, Int>()
    val cameFrom = HashMap<V, V>()
    score[a] = 0
    open += a
    do {
        val current = open.maxBy { score[it]!! } ?: break
        open -= current
        visited += current
        val s = score[current]!!
        for ((edge, index) in this.edgesFrom(current)) {
            val other = this[index]
            val candidate = s + edge.weight
            if (other in visited) {
                // Cycle
                continue
            }
            if (other in score) {
                if (score[other]!! >= candidate) {
                    continue
                }
            }
            score[other] = candidate
            cameFrom[other] = current
            if (other == b) {
                return reconstructPath(other, cameFrom)
            }
        }
    } while (current != null)
    return null
}

/**
 * Passes through all the vertices reachable from [origin] once
 * and invokes [onDiscovery] upon them.
 */
fun <E, V> Graph<E, V>.explore(
    origin: V,
    onDiscovery: (V) -> Unit
) {

    val visited = ArrayList<V>()
    val open = Stack<V>()
    open.push(origin)
    while (open.isNotEmpty()) {
        val current = open.pop()
        visited.add(current)
        val neighbors = edgesFrom(current)
        for ((_, index) in neighbors) {
            val neighbor = this[index]
            if (neighbor in visited) {
                continue
            }
            onDiscovery(neighbor)
            open += neighbor
        }


    }
}

/**
 * Creates a sub graph that contains only the vertices
 * specified in [containing] and it's respective edges.
 */
fun <E, V> Graph<E, V>.subGraph(
    containing: Collection<V>
): Graph<E, V> {
    val sub = Graph<E, V>()
    // Add all vertices first so that we can be sure
    for (v in containing) {
        sub.addVertex(v)
    }
    for (v in containing) {
        val neighbors = edgesFrom(v)
        val mappedIndex = sub.indexOf(v)
        for ((edge, destination) in neighbors) {
            val destV = this[destination]
            val dMappedIndex = sub.indexOf(destV)
            sub.connect(mappedIndex, dMappedIndex, edge)
        }
    }
    return sub
}

/**
 *
 */
fun <E, V> Graph<E, V>.components(): Set<Graph<E, V>> {
    val pending: ArrayList<V> = ArrayList(this.vertices)
    val components = HashSet<Graph<E, V>>()
    while (pending.isNotEmpty()) {
        val elements = HashSet<V>()
        val first = pending.random()
        elements += first
        explore(first) {
            elements += it
        }
        with(pending) {
            remove(first)
            removeAll(elements)
        }
        components += this.subGraph(elements)
    }
    return components
}

fun <E, V> graph(builder: Graph<E, V>.() -> Unit): Graph<E, V> {
    val graph = Graph<E, V>()
    graph.builder()
    return graph
}
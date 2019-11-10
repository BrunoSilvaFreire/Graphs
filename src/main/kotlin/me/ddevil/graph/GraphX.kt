package me.ddevil.graph

import me.ddevil.graph.search.BreadthFirstStrategy
import me.ddevil.graph.search.DepthFirstStrategy
import me.ddevil.graph.search.SearchStrategy
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


private fun <E> transversePath(element: E, map: Map<E, E>): List<E> {
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

/**
 * Performs a [search] using [DepthFirstStrategy]
 */
fun <E : Weighted, V> Graph<E, V>.depthFirstSearch(from: V, to: V) = search(from, to, DepthFirstStrategy)

/**
 * Performs a [search] using [BreadthFirstStrategy]
 */
fun <E : Weighted, V> Graph<E, V>.breadthFirstSearch(from: V, to: V) = search(from, to, BreadthFirstStrategy)

/**
 * Find a path where the origin is [from] and the destination is
 * [to] using the specified [strategy].
 * @see depthFirstSearch
 * @see breadthFirstSearch
 * @see DepthFirstStrategy
 * @see BreadthFirstStrategy
 * @return null if there is no possible path
 */
fun <E, V, O> Graph<E, V>.search(
    from: V,
    to: V,
    strategy: SearchStrategy
): List<V>? where O : MutableCollection<V>, E : Weighted {
    if (from == to) {
        return listOf(from)
    }

    val score = HashMap<V, Int>().apply {
        this[from] = 0
    }

    val cameFrom = HashMap<V, V>()
    explore(from, strategy = strategy) { current, neighbor ->
        val edge = edge(indexOf(current), indexOf(neighbor))!!

        val record = score[neighbor]
        if (record == null || edge.weight < record) {
            cameFrom[neighbor] = current
            score[neighbor] = edge.weight
        }
        if (neighbor == to) {
            return transversePath(neighbor, cameFrom)
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

/**
 * Passes through all the vertices reachable from [origin] once
 * and invokes [onDiscovery] upon them.
 */
inline fun <E, V> Graph<E, V>.explore(
    origin: V,
    strategy: SearchStrategy = DepthFirstStrategy,
    onDiscovery: (current: V, discovered: V) -> Unit
) {

    val visited = ArrayList<V>()
    val open = ArrayDeque<V>()
    open.push(origin)
    while (open.isNotEmpty()) {
        val current = strategy.next(open)
        visited.add(current)
        val neighbors = edgesFrom(current)
        for ((_, index) in neighbors) {
            val neighbor = this[index]
            if (neighbor in visited) {
                continue
            }
            onDiscovery(current, neighbor)
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
 * Gets all the [components](https://en.wikipedia.org/wiki/Component_(graph_theory)) of this graph
 */
fun <E, V> Graph<E, V>.components(filter: ((V) -> Boolean)? = null): Set<Graph<E, V>> {
    val pending = if (filter != null) {
        vertices.filter(filter).toMutableList()
    } else {
        ArrayList(vertices)
    }
    val components = HashSet<Graph<E, V>>()
    while (pending.isNotEmpty()) {
        val elements = HashSet<V>()
        val first = pending.random()
        elements += first
        explore(first) { _, discovered ->
            elements += discovered
        }
        with(pending) {
            remove(first)
            removeAll(elements)
        }
        components += this.subGraph(elements)
        println("${pending.size} vertices remaining")

    }
    return components
}

/**
 * Type-safe builder for graphs.
 */
fun <E, V> graph(builder: Graph<E, V>.() -> Unit): Graph<E, V> {
    val graph = Graph<E, V>()
    graph.builder()
    return graph
}

/**
 * Syntax sugar for [Graph.addVertex].
 * Doesn't return the vertex's index.
 */
operator fun <E, V> Graph<E, V>.plusAssign(vertex: V) {
    addVertex(vertex)
}

fun <E, V> Graph<E, V>.printToConsole() {
    for ((index, value) in vertices.withIndex()) {
        print("$index ")
        val edges = edgesFrom(value)
        if (edges.isEmpty()) {
            println("has no edges.")
        } else {
            println("has ${edges.size} edges:")
            for ((first, second) in edges) {
                println("-> $second: $first")
            }
        }

    }
}

fun completeLabeledGraph(
    vertexCount: Int
) = completeGraph(vertexCount, {
    IntLabeledVertex(it)
}) { _, _ ->
    Unit
}

fun <E, V> completeGraph(
    vertexCount: Int,
    vertCreator: (Int) -> V,
    edgeCreator: (x: Int, y: Int) -> E
): Graph<E, V> {
    val graph = Graph<E, V>()
    for (i in 0 until vertexCount) {
        graph += vertCreator(i)
    }
    for (x in 0 until vertexCount) {
        for (y in x + 1 until vertexCount) {
            graph.connect(x, y, edgeCreator(x, y))
        }
    }

    return graph
}


fun <E : Weighted, V> Graph<E, V>.kruskal(): Graph<E, V> {
    val subGraph = Graph<E, V>()
    val edges: ArrayList<E> = ArrayList(edges.values().filterNotNull().filter {
        val (from, to) = edgeSources(it)
        if (from == to) {
            return@filter false
        }

        return@filter true
    })

    for (vertex in vertices) {
        subGraph.addVertex(vertex)
    }

    val queue = PriorityQueue<E>(compareBy { it.weight })
    queue.addAll(edges)
    var next = queue.poll()
    val visited = ArrayList<Int>()
    visited += edgeSources(next).second
    fun allowed(from: Int, to: Int): Boolean {
        return true
    }

    while (next != null) {
        val (from, to) = edgeSources(next)
        if (!allowed(from, to)) {
            continue
        }
        subGraph.connect(from, to, next, Graph.ConnectionMode.UNIDIRECTIONAL)
        visited += to
        next = queue.poll()

    }
    return subGraph
}
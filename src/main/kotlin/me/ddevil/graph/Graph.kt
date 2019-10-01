package me.ddevil.graph

import com.google.common.collect.Table
import com.google.common.collect.TreeBasedTable

typealias  EdgeTable<E> = Table<Int, Int, E?>

class Graph<E, V> {

    enum class ConnectionMode {
        UNIDIRECTIONAL, BIDIRECTIONAL
    }

    val edges: EdgeTable<E> = TreeBasedTable.create()
    val vertices = ArrayList<V>()
    val totalEdges: Int
        get() = edges.size()
    val totalVertices: Int
        get() = vertices.size

    fun addVertex(vertex: V): Int {
        val i = vertices.size
        vertices.add(vertex)
        return i
    }

    @JvmOverloads
    fun connect(first: Int, second: Int, edge: E, mode: ConnectionMode = ConnectionMode.BIDIRECTIONAL) {
        edges[first, second] = edge
        if (mode == ConnectionMode.BIDIRECTIONAL) {
            connect(second, first, edge, mode = ConnectionMode.UNIDIRECTIONAL)
        }
    }

    @JvmOverloads
    fun disconnect(first: Int, second: Int, mode: ConnectionMode = ConnectionMode.BIDIRECTIONAL) {
        edges[first, second] = null
        if (mode == ConnectionMode.BIDIRECTIONAL) {
            disconnect(second, first, mode = ConnectionMode.UNIDIRECTIONAL)
        }
    }

    operator fun get(first: Int, second: Int) = edges[first, second]
    operator fun set(first: Int, second: Int, edge: E) {
        edges[first, second] = edge
    }

    operator fun get(first: Int) = vertices[first]
    private fun collectEdges(
        vertex: V,
        selector: EdgeTable<E>.(first: Int, second: Int) -> E?
    ): List<Pair<E, Int>> {
        val i = vertices.indexOf(vertex)
        val r = ArrayList<Pair<E, Int>>()
        for (j in vertices.indices) {
            val f = edges.selector(i, j)
            if (f != null) {
                r.add(f to j)
            }
        }
        return r
    }

    fun edgesTo(vertex: V): List<Pair<E, Int>> {
        return collectEdges(vertex) { f, s -> this[s, f] }
    }

    fun edgesFrom(vertex: V): List<Pair<E, Int>> {
        return collectEdges(vertex) { f, s -> this[f, s] }

    }

    fun printTree() {
        for ((index, value) in vertices.withIndex()) {
            println("$index:")
            for ((first, second) in edgesFrom(value)) {
                println("  -> $second: $first")
            }
        }
    }

    fun vertex(function: (V) -> Boolean): Pair<V, Int>? {
        for ((i, v) in vertices.withIndex()) {
            if (function(v)) {
                return v to i
            }
        }
        return null
    }

    fun edge(x: Int, y: Int): E? = edges[x, y]


}

operator fun <R, C, V> Table<R, C, V>.set(first: R, second: C, value: V): V = put(first, second, value)

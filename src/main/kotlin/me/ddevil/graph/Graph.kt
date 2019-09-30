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

    fun edges(vertex: V, debug: Boolean = false): List<Pair<E, Int>> {
        return collectEdges(vertex) { f, s -> this[f, s] }

    }

    fun printTree() {
        for ((index, value) in vertices.sortedBy { edgesTo(it).size }.withIndex()) {
            println("$index: $value")
            for ((first, second) in edges(value)) {
                println("  -> $second: $first")
            }
        }
    }


}

operator fun <R, C, V> Table<R, C, V>.set(first: R, second: C, value: V): V = put(first, second, value)

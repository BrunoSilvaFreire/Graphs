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

    fun addVertices(vararg vertices: V) {
        for (vertex in vertices) {
            addVertex(vertex)
        }
    }

    @JvmOverloads
    fun connect(first: Int, second: Int, edge: E, mode: ConnectionMode = ConnectionMode.BIDIRECTIONAL) {
        this[first, second] = edge
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

    fun vertex(function: (V) -> Boolean): Pair<V, Int>? {
        for ((i, v) in vertices.withIndex()) {
            if (function(v)) {
                return v to i
            }
        }
        return null
    }

    fun edge(x: Int, y: Int): E? = edges[x, y]
    fun indexOf(vertex: V) = vertices.indexOf(vertex)
    fun neighborsOf(v: V) = edgesFrom(v).map {
        it.second to this[it.second]
    }

    fun edgeSources(edge: E): Pair<Int, Int> {
        val idx = vertices.indices
        for (x in idx) {
            for (y in idx) {
                if (this[x, y] == edge) {
                    return x to y
                }
            }
        }
        throw  IllegalStateException()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graph<*, *>

        if (edges != other.edges) return false
        if (vertices != other.vertices) return false

        return true
    }

    override fun hashCode(): Int {
        var result = edges.hashCode()
        result = 31 * result + vertices.hashCode()
        return result
    }

}


private operator fun <R, C, V> Table<R, C, V>.set(first: R, second: C, value: V): V = put(first, second, value)

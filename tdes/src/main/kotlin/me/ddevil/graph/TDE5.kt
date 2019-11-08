package me.ddevil.graph

import kotlin.random.Random

class Edge(
    override val weight: Int
) : Weighted {
    companion object {
        fun create() = Edge(Random.nextInt(10) + 1)
    }
}

class Vertex

fun tde5() {
    val graph = graph<Edge, Vertex> {
        this += Vertex()
        for (i in 1 until 10) {
            this += Vertex()
            connect(i - 1, i, Edge.create())
        }
        connect(0, 3, Edge.create())
        connect(9, 4, Edge.create())
    }


    graph.printToConsole()
    graph.kruskal().printToConsole()
}
package me.ddevil.project2

import me.ddevil.graph.Graph

typealias MazeGraph = Graph<Corridor, Room>

const val mazeSize = 100

fun MazeGraph.vertexAt(x: Int, y: Int): Room {
    val index = x + y * mazeSize
    return this[index]
}

fun isOutOfBounds(x: Int, y: Int): Boolean {
    return x < 0 || y < 0 || x >= mazeSize || y >= mazeSize
}

fun indexOf(x: Int, y: Int) = y * mazeSize + x
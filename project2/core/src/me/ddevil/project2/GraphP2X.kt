package me.ddevil.project2

import kotlinx.coroutines.yield
import me.ddevil.graph.Graph
import kotlin.random.Random

typealias MazeGraph = Graph<Corridor, Room>

const val mazeSize = 100
typealias RoomCoordinate = Pair<Int, Int>

fun MazeGraph.vertexAt(x: Int, y: Int): Room {
    val index = x + y * mazeSize
    return this[index]
}

fun isOutOfBounds(x: Int, y: Int): Boolean {
    return x < 0 || y < 0 || x >= mazeSize || y >= mazeSize
}

fun indexOf(x: Int, y: Int) = y * mazeSize + x

fun addVerticesToMaze(
    width: Int
): Sequence<Room> = sequence {
    for (x in 0 until width) {
        for (y in 0 until width) {
            yield(
                Room(
                    indexOf(x, y),
                    if (Random.nextBoolean()) {
                        Room.RoomType.OPAQUE
                    } else {
                        Room.RoomType.EMPTY
                    }
                )
            )
        }
    }
}

fun addCorridorsToMaze(
    width: Int,
    maze: Maze
): Sequence<Pair<Corridor, RoomCoordinate>> = sequence {
    for (x in 0 until width) {
        for (y in 0 until width) {
            val cell = maze.vertexAt(x, y)
            if (cell.type == Room.RoomType.OPAQUE) {
                continue
            }
            for ((dx, dy) in listOf(
                -1 to 0,
                1 to 0,
                0 to -1,
                0 to 1
            )) {
                val (nx, ny) = x + dx to y + dy
                if (isOutOfBounds(nx, ny)) {
                    continue
                }
                val neighbor = maze.vertexAt(nx, ny)
                if (neighbor.type == Room.RoomType.EMPTY) {
                    val connection = indexOf(x, y) to indexOf(nx, ny)
                    yield(Corridor to connection)
                }
            }
        }
    }
}

fun generateMaze(
    width: Int
): Maze {
    val graph = Maze()
    for (room in addVerticesToMaze(width)) {
        graph.addVertex(room)
    }
    for ((corridor, connection) in addCorridorsToMaze(width, graph)) {
        val (from, to) = connection
        graph.connect(
            from, to, corridor,
            Graph.ConnectionMode.UNIDIRECTIONAL
        )
    }

    return graph
}
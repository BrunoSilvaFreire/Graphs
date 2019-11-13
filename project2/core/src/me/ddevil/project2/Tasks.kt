package me.ddevil.project2

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import kotlinx.coroutines.delay
import me.ddevil.graph.Graph
import me.ddevil.graph.Labeled
import java.util.*

typealias Maze = Graph<Corridor, Room>


suspend fun awaitFrame() = delay(16L)
var lastAwaitInFrames = 0
const val maxFramesUntilAwait = 20
suspend fun maybeAwaitFrame() {
    if (lastAwaitInFrames++ > maxFramesUntilAwait) {
        awaitFrame()
        lastAwaitInFrames = 0
    }
}

interface State {
    suspend fun progress(simulation: Simulation)
    fun render(simulation: Simulation)
}

object Renderers {
    val shapes = ShapeRenderer()
}

object NotGeneratedState : State {
    var width = 100

    override fun render(simulation: Simulation) {
        val graph = simulation.maze
        if (graph == null) {
            println("No maze")
            return
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val r = Renderers.shapes
        r.begin(ShapeRenderer.ShapeType.Filled)
        r.projectionMatrix = simulation.camera.combined
        val c = emptyColor
        for (cell in ArrayList(graph.vertices)) {
            val (x, y) = cell.position
            if (cell.type == Room.RoomType.EMPTY) {
                r.rect(
                    x * cellSize,
                    y * cellSize,
                    cellSize,
                    cellSize,
                    c, c, c, c
                )
            }
        }
        r.end()
    }

    override suspend fun progress(simulation: Simulation) {
        val graph = Maze()
        simulation.maze = graph
        for (room in addVerticesToMaze(width)) {
            graph.addVertex(room)
            maybeAwaitFrame()
        }
        for ((corridor, connection) in addCorridorsToMaze(width, graph)) {
            val (from, to) = connection
            graph.connect(
                from, to, corridor,
                Graph.ConnectionMode.UNIDIRECTIONAL
            )
        }
    }

}
package me.ddevil.project2

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.ddevil.graph.Labeled
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Room(
    override val label: Int,
    val type: RoomType
) : Labeled<Int> {
    enum class RoomType {
        OPAQUE,
        EMPTY
    }

    val position: Pair<Int, Int> get() = label % mazeSize to label / mazeSize
}

object Corridor

const val cellSize = 1.0F

val opaqueColor = Color(
    0.3F, 0.3F, 0.3F, 1F
)

val emptyColor = Color(
    0.5F, 0.847058824F, 1F, 1F
)
const val zoomSpeed = 0.5F
const val speed = 2.5F

class Simulation : ApplicationAdapter() {
    var maze: Maze? = null
    private var job: Job? = null
    lateinit var camera: OrthographicCamera
        private set
    val states = Stack<State>().apply {
        add(NotGeneratedState)
    }
    val currentState: State = states.peek()
    fun progress() {
        if (job != null) {
            return
        }
        job = GlobalScope.launch {
            currentState.progress(this@Simulation)
            job = null
        }
    }

    override fun create() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        camera = OrthographicCamera(30f, 30 * (h / w))
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
        progress()
    }

    override fun render() {
        handleInput()
        currentState.render(this)
    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom += zoomSpeed
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom -= zoomSpeed
        }
        camera.zoom = max(0.1F, camera.zoom)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-speed, 0.0F, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(speed, 0.0F, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0.0F, -speed, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0.0F, speed, 0.0F)
        }

        //camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100 / camera.viewportWidth)

        val effectiveViewportWidth = camera.viewportWidth * camera.zoom
        val effectiveViewportHeight = camera.viewportHeight * camera.zoom

        /*camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f)
        camera.position.y =
            MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f)*/
        camera.update()
    }
}
package me.ddevil.project2

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import me.ddevil.graph.Graph
import me.ddevil.graph.Weighted
import kotlin.random.Random
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import me.ddevil.graph.Labeled
import me.ddevil.graph.components
import kotlin.math.floor

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

class Corridor(
    override val weight: Int //Distance
) : Weighted

const val cellSize = 1.0F

val opaqueColor = Color(
    0.3F, 0.3F, 0.3F, 1F
)

val emptyColor = Color(
    0.5F, 0.847058824F, 1F, 1F
)
const val zoomSpeed = 0.5F
const val speed = 2.5F

class Project2 : ApplicationAdapter() {
    class Selector(
        val game: Project2
    ) : InputAdapter() {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            val pos = game.cam.unproject(Vector3(screenX.toFloat(), screenY.toFloat(), 0.0F))
            val newX = floor(pos.x).toInt()
            val newY = floor(pos.y).toInt()
            val i = newX + newY * mazeSize
            if (button == Input.Buttons.LEFT) {
                game.first = i
                return true
            }
            if (button == Input.Buttons.RIGHT) {
                game.last = i

                return true
            }
            return false
        }
    }

    var cam = OrthographicCamera()
    lateinit var firstLabel: Label
    lateinit var secondLabel: Label
    lateinit var cellRenderer: ShapeRenderer
    var first = 0
    var last = (mazeSize * mazeSize) - 1
    lateinit var components: Set<MazeGraph>
    lateinit var ui: Stage
    private fun updateLabels() {
        val x = first % mazeSize
        val y = first / mazeSize
        firstLabel.setText("First ($first): $x, $y")
        val x2 = last % mazeSize
        val y2 = last / mazeSize
        secondLabel.setText("Second ($last): $x2, $y2")

    }

    override fun create() {
        Gdx.input.inputProcessor = Selector(this)
        ui = Stage()
        val style = Label.LabelStyle()
        val myFont = BitmapFont()
        style.font = myFont
        style.fontColor = Color.RED
        val rowHeight = 16F
        firstLabel = Label(
            "First not updated", style
        ).apply {
            setSize(Gdx.graphics.width.toFloat(), rowHeight);
            setPosition(0.0F, Gdx.graphics.height - rowHeight);
            setAlignment(Align.left);
        }
        secondLabel = Label(
            "Last not updated", style
        ).apply {
            setSize(Gdx.graphics.width.toFloat(), rowHeight);
            setPosition(0.0F, Gdx.graphics.height - rowHeight * 2);
            setAlignment(Align.left);
        }
        ui.addActor(firstLabel)
        ui.addActor(secondLabel)
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        // Constructs a new OrthographicCamera, using the given viewport width and height
        // Height is multiplied by aspect ratio.
        cam = OrthographicCamera(30f, 30 * (h / w))

        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0f)
        cam.update()

        cellRenderer = ShapeRenderer()

        val graph = MazeGraph()
        for (x in 0 until mazeSize) {
            for (y in 0 until mazeSize) {
                graph.addVertex(
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
        for (x in 0 until mazeSize) {
            for (y in 0 until mazeSize) {
                val cell = graph.vertexAt(x, y)
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
                    val neighbor = graph.vertexAt(nx, ny)
                    if (neighbor.type == Room.RoomType.EMPTY) {
                        graph.connect(
                            indexOf(x, y), indexOf(nx, ny), Corridor(1),
                            Graph.ConnectionMode.UNIDIRECTIONAL

                        )
                    }
                }
            }
        }
        println("Creating components")
        components = graph.components {
            return@components it.type == Room.RoomType.EMPTY
        }

    }

    private fun handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            cam.zoom += zoomSpeed
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            cam.zoom -= zoomSpeed
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cam.translate(-speed, 0.0F, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cam.translate(speed, 0.0F, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cam.translate(0.0F, -speed, 0.0F)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cam.translate(0.0F, speed, 0.0F)
        }

        //cam.zoom = MathUtils.clamp(cam.zoom, 0.1f, 100 / cam.viewportWidth)

        val effectiveViewportWidth = cam.viewportWidth * cam.zoom
        val effectiveViewportHeight = cam.viewportHeight * cam.zoom

        /*cam.position.x = MathUtils.clamp(cam.position.x, effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f)
        cam.position.y =
            MathUtils.clamp(cam.position.y, effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f)*/
        cam.update()
    }

    override fun resize(width: Int, height: Int) {
        cam.viewportWidth = 30f;
        cam.viewportHeight = 30f * height / width;
        cam.update();

    }

    override fun render() {
        handleInput()
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        cellRenderer.begin(ShapeRenderer.ShapeType.Filled)
        cellRenderer.projectionMatrix = cam.combined
        for (component in components) {
            val c = Color(component.hashCode())
            c.a = 1.0F
            for (cell in component.vertices) {
                val (x, y) = cell.position
                if (cell.type == Room.RoomType.EMPTY) {
                    cellRenderer.rect(
                        x * cellSize,
                        y * cellSize,
                        cellSize,
                        cellSize,
                        c, c, c, c
                    )
                }
            }
        }
        cellRenderer.end()
        updateLabels()
        ui.draw()
    }

    override fun dispose() {

    }
}

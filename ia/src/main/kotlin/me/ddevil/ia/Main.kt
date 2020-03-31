package me.ddevil.ia

import guru.nidi.graphviz.get
import guru.nidi.graphviz.minus
import guru.nidi.graphviz.toGraphviz
import me.ddevil.graph.*
import me.ddevil.graph.search.aStar
import java.io.File
import java.text.Format
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.system.measureTimeMillis


data class Position(
    val x: Float,
    val y: Float
) {
    fun distanceTo(position: Position): Float {

        return sqrt(
            Math.pow(
                (x - position.x).toDouble(), 2.0
            ) + Math.pow(
                (y - position.y).toDouble(),
                2.0
            )
        ).toFloat()

    }
}

data class City(
    override val label: String,
    val position: Position
) : Labeled<String>

class Highway(
    override val weight: Int
) : Weighted
typealias Benchmark = (from: City, to: City, graph: Graph<Highway, City>) -> List<City>?

fun main() {
    val totalCities = 200
    val graph = graph<Highway, City> {
        val maxPos = 1000
        var totalHighways = totalCities + totalCities / 2
        println("Creating $totalCities cites with $totalHighways total highways")
        for (i in 0 until totalCities) {
            this += City(
                "City #$i",
                Position(
                    Random.nextFloat() * maxPos,
                    Random.nextFloat() * maxPos
                )
            )
        }
        while (totalHighways > 0) {
            val cityAIndex = Random.nextInt(totalCities)
            val cityBIndex = Random.nextInt(totalCities)
            if (this[cityAIndex, cityBIndex] != null) {
                continue
            }
            val cityA = this[cityAIndex]
            val cityB = this[cityBIndex]
            val distance = cityA.position.distanceTo(cityB.position)

            connect(cityAIndex, cityBIndex, Highway(distance.toInt()))
            totalHighways--
        }
    }

    val cityFromIndex = 0
    val cityToIndex = Random.nextInt(totalCities)
    val from = graph[cityFromIndex]
    val to = graph[cityToIndex]

    println("Finding a path from $from to $to")
    val benchmarks = arrayOf<Benchmark>(
        ::depth,
        ::breadth,
        ::aStar
    )
    println()
    for (benchmark in benchmarks) {
        var result: List<City>? = null
        val ms = measureTimeMillis {
            result = benchmark(from, to, graph)
        }
        val f = result
        println("$benchmark took ${ms}ms")
        if (f == null) {
            println("No path")
        } else {
            val lastI = f.lastIndex
            var sum = 0f
            var msg = ""
            for ((i, city) in f.withIndex()) {
                if (i == lastI) {
                    break
                }
                val next = f[i + 1]
                val d = city.position.distanceTo(next.position)
                msg += city.label + " -($d)-> "
                sum += d
            }
            msg += f[lastI].label
            println(msg)
            println("Total cost: $sum")
        }
    }
    /*guru.nidi.graphviz.graph(directed = false, strict = false) {
        for (vertex in graph.vertices) {
            val e = graph.edgesFrom(vertex)
            for (pair in e) {
                (vertex.label - graph[pair.second].label)
            }
        }
    }.toGraphviz().render(guru.nidi.graphviz.engine.Format.PNG).toFile(File("cities.png"))*/
}

fun depth(from: City, to: City, graph: Graph<Highway, City>): List<City>? {
    return graph.depthFirstSearch(from, to)
}

fun breadth(from: City, to: City, graph: Graph<Highway, City>): List<City>? {
    return graph.breadthFirstSearch(from, to)
}

fun aStar(from: City, to: City, graph: Graph<Highway, City>): List<City>? {
    return graph.aStar(from, to) { it.position.distanceTo(to.position) }
}
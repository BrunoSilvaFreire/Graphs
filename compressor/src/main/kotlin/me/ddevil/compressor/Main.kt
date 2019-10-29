package me.ddevil.compressor

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import me.ddevil.graph.Graph
import me.ddevil.graph.printToConsole
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CharVertex(
    val code: Char,
    override val count: Long
) : Vertex()

class MergedVertex(
    override val count: Long
) : Vertex()

abstract class Vertex {
    abstract val count: Long
}


class Edge(
    val bit: Boolean
)

class Compressor : CliktCommand() {
    val file: String by option(help = "The file to compress").required()
    val debug: Boolean by option(help = "Show info").flag()
    fun debug(msg: String) {
        if (debug) {
            print(msg)
        }
    }

    fun debugln(msg: String) {
        if (debug) {
            println(msg)
        }
    }

    override fun run() {
        val graph = Graph<Edge, Vertex>()
        val f = File(file)
        val content = f.readText()
        debugln(content)
        val charCount = HashMap<Char, Long>()
        for (char in content) {
            var count = charCount.getOrDefault(char, 0L)
            count++
            charCount[char] = count
        }
        val open = ArrayList<Vertex>()
        for ((c, count) in charCount) {
            val v = CharVertex(c, count)
            graph.addVertex(v)
            open.add(v)
        }
        while (open.size > 1) {
            open.sortBy { it.count }
            val first = open[0]
            val second = open[1]
            val nV = MergedVertex(first.count + second.count)
            val new = graph.addVertex(nV)

            graph.connect(new, graph.vertices.indexOf(first), Edge(false), mode = Graph.ConnectionMode.UNIDIRECTIONAL)
            graph.connect(new, graph.vertices.indexOf(second), Edge(true), mode = Graph.ConnectionMode.UNIDIRECTIONAL)
            open.remove(first)
            open.remove(second)
            open.add(nV)
        }
        if (debug) {
            graph.printToConsole()
        }
        val bitSet = BitSet()
        var index = 0
        for (c in content) {
            val enconding = getEncoding(c, graph)
            for (b in enconding) {
                if (b) {
                    bitSet.set(index)
                } else {
                    bitSet.clear(index)
                }
                index++
            }
        }
        for ((c) in charCount) {
            debugln("$c: ${getEncoding(c, graph).joinToString("") {
                if (it)
                    "1" else "0"

            }
            }")
        }
        for (i in 0 until bitSet.length()) {
            debug(if (bitSet[i]) "1" else "0")
        }
        val out = File(f.parentFile, "${f.nameWithoutExtension}.compressed.${f.extension}")
        val stream = out.outputStream()
        stream.write(bitSet.toByteArray())
        stream.close()

    }

    private fun getEncoding(c: Char, graph: Graph<Edge, Vertex>): Array<Boolean> {
        val bools = ArrayList<Boolean>()
        val charV = graph.vertices.first { it ->
            return@first it is CharVertex && it.code == c
        }
        var currentVertex = charV
        var edges: List<Pair<Edge, Int>>
        do {
            edges = graph.edgesTo(currentVertex)
            if (edges.isEmpty()) {
                break
            }
            val other = edges.first { graph[it.second] is MergedVertex }
            bools.add(0, other.first.bit)
            currentVertex = graph[other.second]
        } while (true)
        return bools.toTypedArray()
    }

}

fun main(vararg args: String) {
    Compressor().main(args.toList())
}
package me.ddevil.emailstuff

import me.ddevil.graph.*
import java.io.File
import java.util.logging.Logger
import javax.mail.internet.MimeMessage
import java.io.ByteArrayInputStream
import java.util.*
import javax.mail.Address
import javax.mail.Session


class Vertex(
    override val label: String
) : Labeled<String> {
    override fun toString() = label
}

class Edge(
    val emails: MutableList<MimeMessage> = ArrayList()
) : Weighted {
    override val weight: Int get() = emails.size
}


val logger = Logger.getGlobal()
typealias EmailGraph = Graph<Edge, Vertex>

fun main() {
    logger.info("Loading emails...")
    val graph = EmailGraph()
    val mails = File("/home/bruno/IdeaProjects/graph/project1/maildir")
    for (userDir in mails.listFiles()) {
        if (!userDir.isDirectory) {
            continue
        }
        val inbox = File(userDir, "inbox")
        val index = graph.addVertex(Vertex(userDir.name))
        logger.info("Created vertex ${userDir.name}")
        if (!inbox.exists() || !inbox.isDirectory) {
            continue
        }
        for (email in inbox.listFiles()) {

            if (email.isDirectory) {
                continue
            }
            val msg = readMail(email)
            try {
                for (from in msg.from) {
                    val r = msg.allRecipients ?: continue
                    for (to in r) {
                        val fromV = graph.findVertex(from)
                        val toV = graph.findVertex(to)
                        var edge = graph.edge(fromV.second, toV.second)
                        if (edge == null) {
                            edge = Edge()
                            graph.connect(fromV.second, toV.second, edge, Graph.ConnectionMode.UNIDIRECTIONAL)
                        }
                        edge.emails += msg
                    }

                }
            } catch (e: Exception) {
                logger.severe("Unable to load email ${msg.messageID}")
            }
        }
    }
    logger.info("Ready")
    val s = Scanner(System.`in`)
    do {
        val code = s.nextInt()
        when (code) {
            0 -> {
                logger.info("${graph.vertices.size} vertices and ${graph.edges.size()} edges")
            }
            1 -> {

                val withMostIncomingEmail = graph.vertices.sortedBy {
                    return@sortedBy graph.edgesTo(it).size
                }.reversed()

                logger.info("With most incoming:")

                for ((index, vertex) in withMostIncomingEmail.slice(0 until 20).withIndex()) {
                    println("#$index $vertex: ${graph.edgesTo(vertex).size}")
                }
            }
            2 -> {
                val withMostOutgoingEmail = graph.vertices.sortedBy {
                    return@sortedBy graph.edgesFrom(it).size
                }.reversed()
                logger.info("With most outgoing:")
                for ((index, vertex) in withMostOutgoingEmail.slice(0 until 20).withIndex()) {
                    println("#$index $vertex: ${graph.edgesFrom(vertex).size}")
                }
            }
            3 -> {
                logger.info("Start...")
                val i0 = s.nextInt()

                val atRangeFour = graph.searchAtRadius(graph[i0], 4)
                println(atRangeFour.joinToString())
            }
            4 -> {
                val (from, to) = graph.loadFromTo(s)
                val pathA = graph.depthFirstSearch(from, to)
                println(pathA)
            }
            5 -> {
                val (from, to) = graph.loadFromTo(s)
                val pathB = graph.breadthFirstSearch(from, to)
                println(pathB)
            }

        }
    } while (code != 9)


}

fun EmailGraph.loadFromTo(s: Scanner): Pair<Vertex, Vertex> {
    logger.info("Start...")
    val i0 = s.nextInt()
    logger.info("End...")
    val i1 = s.nextInt()
    return this[i0] to this[i1]
}

fun EmailGraph.findVertex(user: Address): Pair<Vertex, Int> {
    val name = user.toString()
    val found = vertex {
        it.label == name
    }
    if (found == null) {
        val v = Vertex(name)
        val i = addVertex(v)
        return v to i
    }
    return found
}

fun readMail(file: File): MimeMessage {
    val s = Session.getInstance(Properties())
    val stream = ByteArrayInputStream(file.readBytes())
    return MimeMessage(s, stream)
}

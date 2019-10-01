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
) {
    val weight: Int get() = emails.size
}


val logger = Logger.getGlobal()
typealias EmailGraph = Graph<Edge, Vertex>

fun main() {
    logger.info("Loading emails...")
    val graph = EmailGraph()
    val mails = File("/home/bruno/IdeaProjects/Graphs/project1/src/main/resources")
    for (userDir in mails.listFiles()) {
        if (!userDir.isDirectory) {
            continue
        }
        val inbox = File(userDir, "inbox")
        val index = graph.addVertex(Vertex(userDir.name))
        if (!inbox.exists() || !inbox.isDirectory) {
            continue
        }
        for (email in inbox.listFiles()) {

            if (email.isDirectory) {
                continue
            }
            val msg = readMail(email)
            for (from in msg.from) {
                val r = msg.allRecipients
                if (r == null) {
                    logger.warning("Found no recipients to email '${msg.subject}'")
                    continue
                }
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
        }
    }
    logger.info("${graph.vertices.size} vertices and ${graph.edges.size()} edges")
    val withMostIncomingEmail = graph.vertices.sortedBy {
        return@sortedBy graph.edgesTo(it).size
    }.reversed()

    logger.info("With most incoming:")

    for ((index, vertex) in withMostIncomingEmail.slice(0 until 20).withIndex()) {
        println("#$index $vertex: ${graph.edgesTo(vertex).size}")
    }
    val withMostOutgoingEmail = graph.vertices.sortedBy {
        return@sortedBy graph.edgesFrom(it).size
    }.reversed()
    logger.info("With most outgoing:")
    for ((index, vertex) in withMostOutgoingEmail.slice(0 until 20).withIndex()) {
        println("#$index $vertex: ${graph.edgesFrom(vertex).size}")
    }
    val first = graph.vertices.first()
    val last = graph.vertices.last()
    val pathA = graph.depthFirstSearch(first, last)
    println(pathA)
    val pathB = graph.breadthFirstSearch(first, last)
    println(pathB)
}

fun EmailGraph.findVertex(user: Address): Pair<Vertex, Int> {
    val name = user.toString()
    val found = vertex {
        it.label == name
    }
    if (found == null) {
        logger.info("Creating new vertex for $name")
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
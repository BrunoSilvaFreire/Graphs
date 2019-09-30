package me.ddevil.emailstuff

import com.google.common.io.Resources
import me.ddevil.graph.Graph
import org.apache.commons.mail.SimpleEmail
import java.io.File
import java.util.logging.Logger


class Vertex(

)

class Edge(

)

data class Email(
    val username: String,
    val server: String
) {
    override fun toString() = "$username@$server"
}

val logger = Logger.getGlobal()
typealias EmailGraph = Graph<Vertex, Edge>

fun main() {
    logger.info("Loading emails...")
    val graph = EmailGraph()
    val root = File("maildir/")
    println("Using file ${root.absolute}")
}
package me.ddevil.graph

fun tde6() {

    val dummyGraph = graph<Unit, IntLabeledVertex> {
        for (i in 0..5) {
            this += IntLabeledVertex(i)
        }
        connect(0, 1, Unit)
        connect(0, 5, Unit)
        connect(2, 3, Unit)
        connect(3, 4, Unit)
    }
    println("Main graph:")
    dummyGraph.printToConsole()
    val components = dummyGraph.components()
    println("There are ${components.size} components")
    for ((i, component) in components.toList().withIndex()) {
        println("Component #$i:")
        component.printToConsole()
        println()
    }
}
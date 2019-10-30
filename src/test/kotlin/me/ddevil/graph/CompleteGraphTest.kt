package me.ddevil.graph

class CompleteGraphTest {
    val graphs = ArrayList<Graph<Unit, IntLabeledVertex>>().apply {
        for (i in 1..5) {
            this += completeLabeledGraph(i)
        }
    }
}
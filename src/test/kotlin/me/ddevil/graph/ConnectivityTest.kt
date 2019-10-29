package me.ddevil.graph

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


val dummyGraph = graph<Unit, IntLabeledVertex> {
    for (i in 0..5) {
        this += IntLabeledVertex(i)
    }
    connect(0, 1, Unit)
    connect(2, 3, Unit)
    connect(3, 4, Unit)
}

class ConnectivityTest {
    @BeforeTest
    fun graphVisualization() {
        dummyGraph.printToConsole()
    }

    @Test
    fun componentCountTest() {
        assertEquals(3, dummyGraph.components().size)
    }
}
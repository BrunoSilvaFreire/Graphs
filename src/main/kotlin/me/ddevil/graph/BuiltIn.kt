package me.ddevil.graph


data class LabeledVertex<T>(
    override val label: T
) : Labeled<T>

typealias IntLabeledVertex = LabeledVertex<Int>
typealias StringLabeledVertex = LabeledVertex<String>
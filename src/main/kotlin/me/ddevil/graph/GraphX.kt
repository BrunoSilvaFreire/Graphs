package me.ddevil.graph

fun <E, V, T : Comparable<T>> Graph<E, V>.selectHighestVertexBy(
    selector: (V) -> T
): V {
    return vertices.maxBy(selector)!!
}

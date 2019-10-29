package me.ddevil.graph.search

import java.util.*

interface SearchStrategy {
    fun <E> next(deque: Deque<E>): E
}

object DepthFirstStrategy : SearchStrategy {
    override fun <E> next(deque: Deque<E>) = deque.pollLast()
}

object BreadthFirstStrategy : SearchStrategy {
    override fun <E> next(deque: Deque<E>) = deque.pollFirst()
}
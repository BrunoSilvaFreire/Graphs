package me.ddevil.graph

import java.util.*

data class TDE(
    val number: Int,
    val executable: () -> Unit
)

val tdes = listOf(
    TDE(5, ::tde5),
    TDE(6, ::tde6)
)

fun main() {
    var running = true

    val input = Scanner(System.`in`)
    while (running) {
        println("Olá! Favor digitar o numero do TDE a ser executado:")
        for (tde in tdes) {
            println("TDE${tde.number}: ${tde.number}")
        }
        val value = input.next()
        if (value == "q" || value == "quit") {
            continue
        }
        val nmbr: Int
        try {
            nmbr = value.toInt()
        } catch (e: Exception) {
            continue
        }
        val tde = tdes.firstOrNull { it.number == nmbr }
        if (tde == null) {
            println("TDE desconhecido")
            continue
        }
        tde.executable()
    }
}
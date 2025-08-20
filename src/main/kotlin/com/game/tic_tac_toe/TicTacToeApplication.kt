package com.game.tic_tac_toe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TicTacToeApplication

fun main(args: Array<String>) {
	runApplication<TicTacToeApplication>(*args)
}

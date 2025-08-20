package com.game.tic_tac_toe.game.model

data class Player(val marker: Marker, val name: String)

enum class Marker {
    X, O
}

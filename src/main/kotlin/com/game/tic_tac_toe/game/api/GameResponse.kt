package com.game.tic_tac_toe.game.api

import com.game.tic_tac_toe.game.model.Game
import com.game.tic_tac_toe.game.model.GameState
import java.util.*

data class GameResponse(
    val gameId: UUID,
    val board: ArrayList<ArrayList<String>>,
    val state: GameState,
    val winner: String = "",
    val lastPlayerMarker: String = ""
)

fun createGameResponse(game: Game) =
    GameResponse(
        game.gameId,
        game.board,
        game.gameState,
        game.winner,
        game.lastPlayerMarker
    )

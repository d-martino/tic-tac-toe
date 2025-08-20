package com.game.tic_tac_toe.game.api

import com.game.tic_tac_toe.game.model.Player
import java.util.UUID

data class PlayRequest(
    val gameId: UUID,
    val rowIndex: Int,
    val colIndex: Int,
    val player: Player
)

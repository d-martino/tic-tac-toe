package com.game.tic_tac_toe.game.persistence

import com.game.tic_tac_toe.game.model.Game
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameRepository : JpaRepository<Game, Long> {
    fun findByGameId(gameId: UUID): Game?
}

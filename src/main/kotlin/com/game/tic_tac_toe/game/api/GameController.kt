package com.game.tic_tac_toe.game.api

import com.game.tic_tac_toe.game.model.Player
import com.game.tic_tac_toe.game.service.GameService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("tic-tac-toe/api/v1")
class GameController(val gameService: GameService) {

    @PostMapping("/start")
    fun startGame(@RequestBody players: List<Player>): GameResponse {
        return gameService.startGame(players)
    }


    @PatchMapping("/play")
    fun playGame(@RequestBody playRequest: PlayRequest): GameResponse {
        return gameService.playGame(playRequest)
    }

    @GetMapping("/game/{gameId}")
    fun getGame(@PathVariable gameId: UUID): GameResponse {
        return gameService.fetchGame(gameId)
    }

}

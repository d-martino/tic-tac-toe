package com.game.tic_tac_toe.game.service

import com.game.tic_tac_toe.game.api.GameResponse
import com.game.tic_tac_toe.game.api.createGameResponse
import com.game.tic_tac_toe.game.model.GameState
import com.game.tic_tac_toe.game.model.Marker
import com.game.tic_tac_toe.game.api.PlayRequest
import com.game.tic_tac_toe.game.model.Player
import com.game.tic_tac_toe.game.model.EMPTY_SQUARE
import com.game.tic_tac_toe.game.model.Game
import com.game.tic_tac_toe.game.persistence.GameRepository
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

private const val NO_WINNER = ""

@Service
class GameService(
    val gameRepository: GameRepository
) {

    @Value("\${board.size}")
    var boardSize: Int = 0

    @Transactional
    fun startGame(players: List<Player>): GameResponse {
        val game = Game(players, UUID.randomUUID())
        game.board = arrayListOf(
            arrayListOf(EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE),
            arrayListOf(EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE),
            arrayListOf(EMPTY_SQUARE, EMPTY_SQUARE, EMPTY_SQUARE)
        )
        game.gameState = GameState.STARTED
        gameRepository.save(game)
        return createGameResponse(game)
    }

    @Transactional
    fun playGame(playRequest: PlayRequest): GameResponse {
        val game = retrieveGame(playRequest.gameId)
        validatePlay(playRequest, game)
        savePlay(game, playRequest)
        return createGameResponse(game)
    }

    fun fetchGame(gameId: UUID): GameResponse =
        createGameResponse(retrieveGame(gameId))

    private fun retrieveGame(gameId: UUID): Game {
        val game = gameRepository.findByGameId(gameId)
        require(game != null) { "Cannot find a game with id $gameId" }
        return game
    }

    private fun savePlay(game: Game, playRequest: PlayRequest) {
        game.board[playRequest.rowIndex][playRequest.colIndex] = playRequest.player.marker.name
        game.lastPlayerMarker = playRequest.player.marker.name

        val winnerMark = checkForWinner(game.board, playRequest)
        when {
            winnerMark in listOf(Marker.X.name, Marker.O.name) -> {
                game.winner = winnerMark
                game.gameState = GameState.WIN
            }

            winnerMark == NO_WINNER && noMoreMovesAvailable(game) -> game.gameState = GameState.DRAW
            else -> game.gameState = GameState.RUNNING
        }
        gameRepository.save(game)
    }

    private fun validatePlay(playRequest: PlayRequest, game: Game) {
        require(isGameRunning(game)) { "Game already ended. ${game.board}" }
        require(isWithinBoard(playRequest.rowIndex, playRequest.colIndex)) {
            "Play is outside board, at row and col index (${playRequest.rowIndex},${playRequest.colIndex})!"
        }
        require(isSquareFree(playRequest.rowIndex, playRequest.colIndex, game)) {
            "Square already occupied, at row and col index (${playRequest.rowIndex},${playRequest.colIndex})!"
        }
        require(isPlayerTurn(playRequest.player, game)) {
            "The ${playRequest.player} has already played, it is the other player turn now!"
        }
    }

    private fun noMoreMovesAvailable(game: Game): Boolean =
        game.board.flatten().none { it.isEmpty() }

    private fun isGameRunning(game: Game) = game.gameState !in listOf(GameState.DRAW, GameState.WIN)

    private fun isWithinBoard(rowIndex: Int, colIndex: Int): Boolean =
        rowIndex in 0..<boardSize && colIndex in 0..<boardSize

    private fun isSquareFree(rowIndex: Int, colIndex: Int, game: Game): Boolean =
        game.board[rowIndex][colIndex].isEmpty()

    private fun isPlayerTurn(player: Player, game: Game): Boolean =
        player.marker.name != game.lastPlayerMarker

    private fun checkForWinner(board: ArrayList<ArrayList<String>>, playRequest: PlayRequest): String {
        // checking only the row the marker is placed
        if (hasSameMarker(board[playRequest.rowIndex])) {
            return board[playRequest.rowIndex].first
        }

        // checking only the col the marker is placed
        val columnValues = board.map { it[playRequest.colIndex] }
        if (hasSameMarker(columnValues)) {
            return board[0][playRequest.colIndex]
        }

        val diagonal = board.indices.map { i -> board[i][i] }
        if (hasSameMarker(diagonal)) {
            return board[0][0]
        }
        val antiDiagonal = (0 until boardSize).map { i -> board[i][boardSize - 1 - i] }
        if (hasSameMarker(antiDiagonal)) {
            return board[0][boardSize - 1]
        }
        return NO_WINNER
    }

    private fun hasSameMarker(row: List<String>) = row.toSet().size == 1 && row.first() != NO_WINNER


}
package com.game.tic_tac_toe

import com.game.tic_tac_toe.game.model.GameState
import com.game.tic_tac_toe.game.model.Marker.O
import com.game.tic_tac_toe.game.model.Marker.X
import com.game.tic_tac_toe.game.api.PlayRequest
import com.game.tic_tac_toe.game.model.Player
import com.game.tic_tac_toe.game.model.Game
import com.game.tic_tac_toe.game.service.GameService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameServiceTests {

    @Autowired
    lateinit var gameService: GameService

    @Test
    fun `start game should initialize board and state`() {
        val game = gameService.startGame(
            createPlayers()
        )
        assertTrue(game.board.flatten().all { it.isEmpty() })
        assertEquals(gameService.boardSize, game.board.size)
        assertEquals(GameState.STARTED, game.state)
    }

    @Test
    fun `get game by id should return game state`() {
        val game = Game(createPlayers(), UUID.randomUUID())
        game.gameState = GameState.RUNNING
        game.lastPlayerMarker = "X"
        game.board = arrayListOf(
            arrayListOf("X", "X", "X"),
            arrayListOf("O", "", ""),
            arrayListOf("O", "", "")
        )
        gameService.gameRepository.save(game)
        val retrievedGame = gameService.fetchGame(game.gameId)
        assertEquals(GameState.RUNNING, retrievedGame.state)
        assertEquals("", retrievedGame.winner)
        assertEquals(3, game.board.flatten().filter { it == X.name }.size)
        assertEquals(2, game.board.flatten().filter { it == O.name }.size)
    }

    @ParameterizedTest
    @MethodSource("getWinnerData")
    fun `running play game should return winner`(board: ArrayList<ArrayList<String>>, lastPlayerSymbol: String,
                                                 rowIndex: Int, colIndex: Int, player: Player, winner: String) {
        val gameId = UUID.randomUUID()
        val players = createPlayers()
        val game = Game(players, gameId)
        game.lastPlayerMarker = lastPlayerSymbol
        game.board = board
        game.gameState = GameState.RUNNING
        gameService.gameRepository.save(game)
        val playRequest = PlayRequest(gameId, rowIndex, colIndex, player)
        assertEquals(winner, gameService.playGame(playRequest).winner)
    }

    @ParameterizedTest
    @MethodSource("getDrawData")
    fun `running play game should return draw - empty winner`(board: ArrayList<ArrayList<String>>, lastPlayerSymbol: String,
                                                              rowIndex: Int, colIndex: Int, player: Player, winner: String) {
        val gameId = UUID.randomUUID()
        val players = createPlayers()
        val game = Game(players, gameId)
        game.lastPlayerMarker = lastPlayerSymbol
        game.board = board
        game.gameState = GameState.RUNNING
        gameService.gameRepository.save(game)
        val playRequest = PlayRequest(gameId, rowIndex, colIndex, player)
        assertEquals(winner, gameService.playGame(playRequest).winner)
    }

    companion object {
        @JvmStatic
        fun getWinnerData(): List<Arguments> {
            return listOf(
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "X", ""),
                        arrayListOf("O", "", ""),
                        arrayListOf("O", "", "")
                    ), "O", 0, 2, Player(X, "test"), "X"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("O", "O", ""),
                        arrayListOf("X", "X", ""),
                        arrayListOf("", "", "")
                    ), "O", 1, 2, Player(X, "test"), "X"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("", "", ""),
                        arrayListOf("O", "O", ""),
                        arrayListOf("", "X", "X")
                    ), "O", 2, 0, Player(X, "test"), "X"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "O", "X"),
                        arrayListOf("X", "X", "O"),
                        arrayListOf("", "O", "O")
                    ), "O",  2, 0, Player(X, "test"), "X"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "", "X"),
                        arrayListOf("O", "X", "O"),
                        arrayListOf("O", "X", "O")
                    ), "O", 0, 1, Player(X, "test"), "X"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "O", "O"),
                        arrayListOf("O", "X", "O"),
                        arrayListOf("X", "O", "")
                    ), "X", 2, 2, Player(O, "test"), "O"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("O", "X", "X"),
                        arrayListOf("O", "", "X"),
                        arrayListOf("X", "X", "O")
                    ), "X", 1, 1, Player(O, "test"), "O"
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "O", "X"),
                        arrayListOf("O", "X", "O"),
                        arrayListOf("", "X", "O")
                    ), "O", 2, 0, Player(X, "test"), "X"
                )
            )
        }

        @JvmStatic
        fun getDrawData(): List<Arguments> {
            return listOf(
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "O", "X"),
                        arrayListOf("X", "O", "O"),
                        arrayListOf("O", "", "X")
                    ), "O", 2, 1, Player(X, "test"), ""
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "", "X"),
                        arrayListOf("O", "X", "X"),
                        arrayListOf("O", "X", "O")
                    ), "X", 0, 1, Player(O, "test"), ""
                ),
                Arguments.of(
                    arrayListOf(
                        arrayListOf("X", "O", "X"),
                        arrayListOf("", "O", "O"),
                        arrayListOf("O", "X", "X")
                    ), "O", 1, 0, Player(X, "test"), ""
                )
            )
        }

        @Container
        @ServiceConnection
        @JvmStatic
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:latest"))

        private fun createPlayers() = listOf(
            Player(X, "test"),
            Player(O, "test")
        )
    }


}

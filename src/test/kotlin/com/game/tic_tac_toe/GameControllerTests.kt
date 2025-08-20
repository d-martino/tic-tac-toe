package com.game.tic_tac_toe

import com.game.tic_tac_toe.game.model.GameState
import com.game.tic_tac_toe.game.model.Marker.O
import com.game.tic_tac_toe.game.model.Marker.X
import com.game.tic_tac_toe.game.api.PlayRequest
import com.game.tic_tac_toe.game.model.Player
import com.game.tic_tac_toe.game.model.Game
import com.game.tic_tac_toe.game.persistence.GameRepository
import com.game.tic_tac_toe.game.api.GameResponse
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.web.util.DefaultUriBuilderFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class GameControllerTests {

    @Autowired
    lateinit var gameRepository: GameRepository

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate


    @BeforeEach
    fun setUp() {
        restTemplate.setUriTemplateHandler(DefaultUriBuilderFactory("http://localhost:$port"))
    }

    @AfterEach
    fun clear() {
        gameRepository.deleteAll()
    }

    @Test
    fun `calling get game by id is successful`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, O.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", ""),
            arrayListOf("X", "X", "X"),
            arrayListOf("", "", "")
        )
        game.gameState = GameState.RUNNING
        gameRepository.save(game)
        val uriVariables = mapOf("gameId" to gameId)

        val response = restTemplate.getForEntity(
            "/tic-tac-toe/api/v1/game/{gameId}",
            GameResponse::class.java,
            *uriVariables.values.toTypedArray()
        )

        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(GameState.RUNNING, response.body?.state)
        assertEquals(O.name, response.body?.lastPlayerMarker)
    }

    @Test
    fun `create game is successful`() {
        val players = createPlayers()
        val requestStartGame = HttpEntity(players)

        val response =
            restTemplate.postForEntity("/tic-tac-toe/api/v1/start", requestStartGame, GameResponse::class.java)

        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(GameState.STARTED, response.body?.state)
    }

    @Test
    fun `play game is successful winning`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, X.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", ""),
            arrayListOf("X", "X", ""),
            arrayListOf("", "X", "")
        )
        game.gameState = GameState.RUNNING
        gameRepository.save(game)

        val playRequest = PlayRequest(gameId, 0, 2, Player(O, "test"))
        val requestPlay = HttpEntity(playRequest)

        val gameResponse = restTemplate.patchForObject("/tic-tac-toe/api/v1/play", requestPlay, GameResponse::class.java)

        assertNotNull(gameResponse)
        assertEquals(GameState.WIN, gameResponse.state)
        assertEquals("O", gameResponse.winner)
    }

    @Test
    fun `play request invalid - square occupied`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, X.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", ""),
            arrayListOf("X", "X", ""),
            arrayListOf("", "X", "")
        )
        game.gameState = GameState.RUNNING
        gameRepository.save(game)

        val playRequest = PlayRequest(gameId, 0, 0, Player(O, "test"))
        val requestPlay = HttpEntity(playRequest)

        val response = restTemplate.patchForObject("/tic-tac-toe/api/v1/play", requestPlay, String::class.java)

        assertTrue(response.contains("Square already occupied"))
    }

    @Test
    fun `play request invalid - outside board`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, X.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", ""),
            arrayListOf("X", "X", ""),
            arrayListOf("", "X", "")
        )
        game.gameState = GameState.RUNNING
        gameRepository.save(game)

        val playRequest = PlayRequest(gameId, 0, 10, Player(O, "test"))
        val requestPlay = HttpEntity(playRequest)

        val response = restTemplate.patchForObject("/tic-tac-toe/api/v1/play", requestPlay, String::class.java)

        assertTrue(response.contains("Play is outside board"))
    }

    @Test
    fun `play request invalid - same player`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, X.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", ""),
            arrayListOf("X", "X", ""),
            arrayListOf("", "X", "")
        )
        game.gameState = GameState.RUNNING
        gameRepository.save(game)

        val playRequest = PlayRequest(gameId, 0, 2, Player(X, "test"))
        val requestPlay = HttpEntity(playRequest)

        val response = restTemplate.patchForObject("/tic-tac-toe/api/v1/play", requestPlay, String::class.java)

        assertTrue(response.contains("The Player(marker=X, name=test) has already played"))
    }

    @Test
    fun `play request invalid - game is ended with a win`() {
        val gameId = UUID.randomUUID()
        val game = Game(createPlayers(), gameId, X.name)
        game.board = arrayListOf(
            arrayListOf("O", "O", "O"),
            arrayListOf("X", "X", ""),
            arrayListOf("", "X", "")
        )
        game.gameState = GameState.WIN
        gameRepository.save(game)

        val playRequest = PlayRequest(gameId, 2, 0, Player(O, "test"))
        val requestPlay = HttpEntity(playRequest)

        val response = restTemplate.patchForObject("/tic-tac-toe/api/v1/play", requestPlay, String::class.java)

        assertTrue(response.contains("Game already ended"))
    }

    companion object {

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

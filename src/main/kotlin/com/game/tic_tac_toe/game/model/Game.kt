package com.game.tic_tac_toe.game.model

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

const val EMPTY_SQUARE = ""

@Entity
@EntityListeners(AuditingEntityListener::class)
class Game(
    @Column(columnDefinition = "jsonb")
    @Type(JsonType::class)
    val players: List<Player> = listOf(),

    @Column(nullable = false, unique = true)
    val gameId: UUID,

    var lastPlayerMarker: String = "",
    var winner: String = "",
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(columnDefinition = "jsonb")
    @Type(JsonType::class)
    lateinit var board: ArrayList<ArrayList<String>>

    @Enumerated(EnumType.STRING)
    lateinit var gameState: GameState

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
}

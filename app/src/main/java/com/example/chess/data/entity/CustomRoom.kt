package com.example.chess.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_room")
data class CustomRoom(
    @PrimaryKey val roomCode: String,
    val hostUsername: String,
    val guestUsername: String? = null,
    val status: String = "WAITING", // "WAITING", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val boardState: String? = null,
    val currentPlayerColor: String = "WHITE", // Whose turn it is
    val winner: String? = null
)

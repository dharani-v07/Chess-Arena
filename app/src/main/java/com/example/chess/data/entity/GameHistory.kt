package com.example.chess.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: String,
    val operandName: String, // opponent
    val result: String,       // "WON", "LOST", "DRAW"
    val coinsDelta: Int,      // e.g. +30 or -50
    val ratingDelta: Int,     // e.g. +15 or -10
    val timestamp: Long = System.currentTimeMillis()
)

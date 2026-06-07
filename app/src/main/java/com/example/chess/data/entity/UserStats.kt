package com.example.chess.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val username: String = "Grandmaster",
    val coinBalance: Int = 100, // Starts with 100 coins
    val rating: Int = 0,      // Starts with 0 ELO
    val localWins: Int = 0,
    val localLosses: Int = 0,
    val localDraws: Int = 0,
    val multiWins: Int = 0,
    val multiLosses: Int = 0,
    val multiDraws: Int = 0,
    
    // Cosmetics shop & skins selection
    val selectedTheme: String = "Classic",      // Classic, Wooden, Neon, Gold, Diamond
    val selectedPieces: String = "Standard",    // Standard, Modern, Fantasy
    val unlockedThemes: String = "Classic",     // Comma-separated list of unlocked boards
    val unlockedPieces: String = "Standard",    // Comma-separated list of unlocked pieces
    
    // Daily Reward Tracking
    val lastDailyRewardClaimTime: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val nextClaimTimestamp: Long = 0L,
    val totalClaimedRewards: Int = 0,
    val rewardHistory: String = "",
    val lastWeeklyChestClaimTime: Long = 0L,
    
    // Save Game State
    val savedBoardStateRaw: String? = null,     // null means no active saved game
    val savedGameMode: String? = null,
    val savedOpponentName: String? = null,
    val savedOpponentRating: Int = 1200,
    val savedPlayerColor: String = "WHITE"      // WHITE or BLACK
)

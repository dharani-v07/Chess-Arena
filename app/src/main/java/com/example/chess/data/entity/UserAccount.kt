package com.example.chess.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_account")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val username: String,
    val passwordHash: String, // Stored securely
    val coinBalance: Int = 100,
    val rating: Int = 0,
    val localWins: Int = 0,
    val localLosses: Int = 0,
    val localDraws: Int = 0,
    val multiWins: Int = 0,
    val multiLosses: Int = 0,
    val multiDraws: Int = 0,
    val selectedTheme: String = "Classic",
    val selectedPieces: String = "Standard",
    val unlockedThemes: String = "Classic",
    val unlockedPieces: String = "Standard",
    val lastDailyRewardClaimTime: Long = 0L,
    val dailyRewardStreak: Int = 0,
    val nextClaimTimestamp: Long = 0L,
    val totalClaimedRewards: Int = 0,
    val rewardHistory: String = "",
    val lastWeeklyChestClaimTime: Long = 0L,
    val isEmailVerified: Boolean = true,
    val isLoggedIn: Boolean = false
)

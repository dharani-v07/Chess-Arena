package com.example.chess.data.repository

import com.example.chess.data.dao.ChessDao
import com.example.chess.data.entity.GameHistory
import com.example.chess.data.entity.UserStats
import com.example.chess.data.entity.UserAccount
import com.example.chess.data.entity.CustomRoom
import kotlinx.coroutines.flow.Flow

class ChessRepository(private val chessDao: ChessDao) {

    val userStatsFlow: Flow<UserStats?> = chessDao.getUserStatsFlow()
    val allHistoryFlow: Flow<List<GameHistory>> = chessDao.getAllHistoryFlow()

    suspend fun getStats(): UserStats {
        ensureUserStatsExist()
        return chessDao.getUserStats() ?: UserStats()
    }

    suspend fun ensureUserStatsExist() {
        if (!chessDao.hasUserStats()) {
            chessDao.insertUserStats(UserStats())
        }
    }

    suspend fun updateStats(stats: UserStats) {
        chessDao.insertUserStats(stats)
    }

    suspend fun clearHistory() {
        chessDao.clearHistory()
    }

    suspend fun updateCoins(delta: Int): UserStats {
        val currentStats = getStats()
        val updatedStats = currentStats.copy(
            coinBalance = Math.max(0, currentStats.coinBalance + delta)
        )
        chessDao.insertUserStats(updatedStats)
        return updatedStats
    }

    suspend fun recordMatch(
        mode: String,
        opponentName: String,
        result: String, // "WON", "LOST", "DRAW"
        coinsDelta: Int,
        ratingDelta: Int
    ): UserStats {
        val currentStats = getStats()
        
        val isMultiplayer = mode.contains("Arena")
        val isLocalAi = mode.contains("Local AI") || mode.contains("vs Computer")
        val isLocalPvP = mode.contains("Local PvP")

        var localWins = currentStats.localWins
        var localLosses = currentStats.localLosses
        var localDraws = currentStats.localDraws
        
        var multiWins = currentStats.multiWins
        var multiLosses = currentStats.multiLosses
        var multiDraws = currentStats.multiDraws

        when (result) {
            "WON" -> {
                if (isMultiplayer) multiWins++
                if (isLocalAi) localWins++
            }
            "LOST" -> {
                if (isMultiplayer) multiLosses++
                if (isLocalAi) localLosses++
            }
            "DRAW" -> {
                if (isMultiplayer) multiDraws++
                if (isLocalAi) localDraws++
            }
        }

        val updatedStats = currentStats.copy(
            coinBalance = Math.max(0, currentStats.coinBalance + coinsDelta),
            rating = Math.max(0, currentStats.rating + ratingDelta),
            localWins = localWins,
            localLosses = localLosses,
            localDraws = localDraws,
            multiWins = multiWins,
            multiLosses = multiLosses,
            multiDraws = multiDraws
        )

        // Write to DB
        chessDao.insertUserStats(updatedStats)
        chessDao.insertGameHistory(
            GameHistory(
                mode = mode,
                operandName = opponentName,
                result = result,
                coinsDelta = coinsDelta,
                ratingDelta = ratingDelta
            )
        )
        
        return updatedStats
    }

    // --- UserAccount repository methods ---
    suspend fun getActiveSession(): UserAccount? {
        return chessDao.getActiveSession()
    }

    suspend fun getAccountByEmail(email: String): UserAccount? {
        return chessDao.getAccountByEmail(email)
    }

    suspend fun getAccountByUsername(username: String): UserAccount? {
        return chessDao.getAccountByUsername(username)
    }

    suspend fun getAllAccountsSortedByRating(): List<UserAccount> {
        return chessDao.getAllAccountsSortedByRating()
    }

    suspend fun resetAllAccountsStats() {
        chessDao.resetAllAccountsStats()
    }

    suspend fun insertAccount(account: UserAccount): Long {
        return chessDao.insertAccount(account)
    }

    suspend fun deleteTemporaryAccounts(adminEmail: String = "admin@test.com") {
        chessDao.deleteTemporaryAccounts(adminEmail)
    }

    suspend fun deleteBotAccounts() {
        chessDao.deleteBotAccounts()
    }

    suspend fun getCustomRoom(roomCode: String): CustomRoom? {
        return chessDao.getCustomRoom(roomCode)
    }

    suspend fun insertCustomRoom(room: CustomRoom) {
        chessDao.insertCustomRoom(room)
    }

    suspend fun deleteCustomRoom(roomCode: String) {
        chessDao.deleteCustomRoom(roomCode)
    }
}

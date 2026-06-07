package com.example.chess.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chess.data.entity.GameHistory
import com.example.chess.data.entity.UserStats
import com.example.chess.data.entity.UserAccount
import com.example.chess.data.entity.CustomRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface ChessDao {
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStats?

    @Query("SELECT EXISTS(SELECT 1 FROM user_stats WHERE id = 1)")
    suspend fun hasUserStats(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Query("SELECT * FROM game_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<GameHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameHistory(history: GameHistory)
    
    @Query("DELETE FROM game_history")
    suspend fun clearHistory()

    // --- UserAccount DAO methods ---
    @Query("SELECT * FROM user_account WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getActiveSession(): UserAccount?

    @Query("SELECT * FROM user_account WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_account WHERE username = :username LIMIT 1")
    suspend fun getAccountByUsername(username: String): UserAccount?

    @Query("SELECT * FROM user_account ORDER BY rating DESC")
    suspend fun getAllAccountsSortedByRating(): List<UserAccount>

    @Query("UPDATE user_account SET rating = 0, localWins = 0, localLosses = 0, localDraws = 0, multiWins = 0, multiLosses = 0, multiDraws = 0")
    suspend fun resetAllAccountsStats()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccount): Long

    @Query("DELETE FROM user_account WHERE email != :adminEmail")
    suspend fun deleteTemporaryAccounts(adminEmail: String)

    @Query("DELETE FROM user_account WHERE email LIKE '%@bots.com'")
    suspend fun deleteBotAccounts()

    @Delete
    suspend fun deleteAccount(account: UserAccount)

    // --- Custom Room queries ---
    @Query("SELECT * FROM custom_room WHERE roomCode = :roomCode LIMIT 1")
    suspend fun getCustomRoom(roomCode: String): CustomRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomRoom(room: CustomRoom)

    @Query("DELETE FROM custom_room WHERE roomCode = :roomCode")
    suspend fun deleteCustomRoom(roomCode: String)
}


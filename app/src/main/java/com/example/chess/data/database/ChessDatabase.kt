package com.example.chess.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chess.data.dao.ChessDao
import com.example.chess.data.entity.GameHistory
import com.example.chess.data.entity.UserStats
import com.example.chess.data.entity.UserAccount
import com.example.chess.data.entity.CustomRoom

@Database(entities = [UserStats::class, GameHistory::class, UserAccount::class, CustomRoom::class], version = 7, exportSchema = false)
abstract class ChessDatabase : RoomDatabase() {
    abstract fun chessDao(): ChessDao

    companion object {
        @Volatile
        private var INSTANCE: ChessDatabase? = null

        fun getDatabase(context: Context): ChessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess_royale_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

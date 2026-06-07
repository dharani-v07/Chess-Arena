package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.chess.data.database.ChessDatabase
import com.example.chess.data.repository.ChessRepository
import com.example.chess.ui.ChessApp
import com.example.chess.ui.ChessViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: ChessViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room Database, Repository, and ViewModel directly 
        // to stay clean, lightweight, and compile-safe.
        val database = ChessDatabase.getDatabase(applicationContext)
        val repository = ChessRepository(database.chessDao())
        viewModel = ChessViewModel(repository)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Force beautiful Premium Dark Theme for Chess Arena!
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = com.example.chess.ui.DarkLobbyBg
                ) {
                    ChessApp(viewModel = viewModel)
                }
            }
        }
    }
}

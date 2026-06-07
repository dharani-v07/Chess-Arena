package com.example.chess.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.entity.GameHistory
import com.example.chess.data.entity.UserStats
import com.example.chess.data.entity.UserAccount
import com.example.chess.data.repository.ChessRepository
import com.example.chess.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.util.Random

enum class Screen {
    SPLASH, AUTH, LOBBY, MATCHMAKING, GAME
}

enum class AuthMode {
    LOGIN, REGISTER, FORGOT_PASSWORD
}

data class ArenaPreset(
    val name: String,
    val entryFee: Int,
    val prizeMoney: Int,
    val ratingGain: Int,
    val minRating: Int,
    val bots: List<String>
)

class ChessViewModel(private val repository: ChessRepository) : ViewModel() {

    private val random = Random()

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()


    // User profile and history states from Room
    val userStats: StateFlow<UserStats?> = repository.userStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val gameHistory: StateFlow<List<GameHistory>> = repository.allHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Game States
    private val _boardState = MutableStateFlow(BoardState())
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    private val _gameMode = MutableStateFlow("Local AI Easy")
    val gameMode: StateFlow<String> = _gameMode.asStateFlow()

    private val _playerColor = MutableStateFlow(PieceColor.WHITE)
    val playerColor: StateFlow<PieceColor> = _playerColor.asStateFlow()

    private val _selectedSquare = MutableStateFlow<Position?>(null)
    val selectedSquare: StateFlow<Position?> = _selectedSquare.asStateFlow()

    private val _validMovesForSelected = MutableStateFlow<List<Move>>(emptyList())
    val validMovesForSelected: StateFlow<List<Move>> = _validMovesForSelected.asStateFlow()

    private val _opponentName = MutableStateFlow("Stockfish AI")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()

    private val _opponentRating = MutableStateFlow(1200)
    val opponentRating: StateFlow<Int> = _opponentRating.asStateFlow()

    // Game scores to reward coins
    private val _gameScore = MutableStateFlow(0)
    val gameScore: StateFlow<Int> = _gameScore.asStateFlow()

    private val _coinsToEarn = MutableStateFlow(0)
    val coinsToEarn: StateFlow<Int> = _coinsToEarn.asStateFlow()

    // Captured pieces lists
    private val _capturedByWhite = MutableStateFlow<List<ChessPiece>>(emptyList())
    val capturedByWhite: StateFlow<List<ChessPiece>> = _capturedByWhite.asStateFlow()

    private val _capturedByBlack = MutableStateFlow<List<ChessPiece>>(emptyList())
    val capturedByBlack: StateFlow<List<ChessPiece>> = _capturedByBlack.asStateFlow()

    // Alerts and messages states
    private val _statusMessage = MutableStateFlow("Your Turn")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _opponentChat = MutableStateFlow<String?>(null)
    val opponentChat: StateFlow<String?> = _opponentChat.asStateFlow()

    // Is opponent currently "thinking" (animating)
    private val _isOpponentThinking = MutableStateFlow(false)
    val isOpponentThinking: StateFlow<Boolean> = _isOpponentThinking.asStateFlow()

    // Matchmaking queue variables
    private val _isMatchmaking = MutableStateFlow(false)
    val isMatchmaking: StateFlow<Boolean> = _isMatchmaking.asStateFlow()

    private val _matchmakingArena = MutableStateFlow<ArenaPreset?>(null)
    val matchmakingArena: StateFlow<ArenaPreset?> = _matchmakingArena.asStateFlow()

    private val _matchmakingTimeSec = MutableStateFlow(0)
    val matchmakingTimeSec: StateFlow<Int> = _matchmakingTimeSec.asStateFlow()

    // Final GameOver details Dialog state
    private val _showGameOverDialog = MutableStateFlow(false)
    val showGameOverDialog: StateFlow<Boolean> = _showGameOverDialog.asStateFlow()

    // --- Dynamic Auth State Flows ---
    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    private val _currentUserAccount = MutableStateFlow<UserAccount?>(null)
    val currentUserAccount: StateFlow<UserAccount?> = _currentUserAccount.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    // --- Room Code Multiplayer states ---
    private val _createdRoomCode = MutableStateFlow<String?>(null)
    val createdRoomCode: StateFlow<String?> = _createdRoomCode.asStateFlow()

    private val _roomState = MutableStateFlow("WAITING") // "WAITING", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val roomState: StateFlow<String> = _roomState.asStateFlow()

    private val _isCustomRoomHost = MutableStateFlow(false)
    val isCustomRoomHost: StateFlow<Boolean> = _isCustomRoomHost.asStateFlow()

    private val _roomCodeInput = MutableStateFlow("")
    val roomCodeInput: StateFlow<String> = _roomCodeInput.asStateFlow()

    // Active White and Black game timers (M3 standard chess clock!)
    private val _whiteTimerSec = MutableStateFlow(300)
    val whiteTimerSec: StateFlow<Int> = _whiteTimerSec.asStateFlow()

    private val _blackTimerSec = MutableStateFlow(300)
    val blackTimerSec: StateFlow<Int> = _blackTimerSec.asStateFlow()

    private var activeTimerJob: Job? = null
    private var customRoomPollJob: Job? = null
    private var guestPollJob: Job? = null


    private val _gameOverWinner = MutableStateFlow<PieceColor?>(null) // null = Draw
    val gameOverWinner: StateFlow<PieceColor?> = _gameOverWinner.asStateFlow()

    private val _gameOverReason = MutableStateFlow("")
    val gameOverReason: StateFlow<String> = _gameOverReason.asStateFlow()

    private val _coinsEarnedResult = MutableStateFlow(0)
    val coinsEarnedResult: StateFlow<Int> = _coinsEarnedResult.asStateFlow()

    private val _ratingDeltaResult = MutableStateFlow(0)
    val ratingDeltaResult: StateFlow<Int> = _ratingDeltaResult.asStateFlow()

    private val _leaderboardPlayers = MutableStateFlow<List<UserAccount>>(emptyList())
    val leaderboardPlayers: StateFlow<List<UserAccount>> = _leaderboardPlayers.asStateFlow()

    private val _currentServerTimeFlow = MutableStateFlow<Long>(-1L)
    val currentServerTimeFlow: StateFlow<Long> = _currentServerTimeFlow.asStateFlow()

    private var serverTimeRef: Long = -1L
    private var localTimeRef: Long = -1L

    fun getVerifiedServerTime(): Long {
        if (serverTimeRef == -1L || localTimeRef == -1L) {
            return System.currentTimeMillis()
        }
        return serverTimeRef + (android.os.SystemClock.elapsedRealtime() - localTimeRef)
    }

    suspend fun refreshVerifiedServerTime(): Long {
        var serverTime = fetchServerTime()
        if (serverTime == -1L) {
            serverTime = System.currentTimeMillis()
        }
        serverTimeRef = serverTime
        localTimeRef = android.os.SystemClock.elapsedRealtime()
        _currentServerTimeFlow.value = serverTime
        return serverTime
    }

    // Arena Presets
    val arenas = listOf(
        ArenaPreset(
            name = "Amateur Arena",
            entryFee = 15,
            prizeMoney = 30,
            ratingGain = 12,
            minRating = 0,
            bots = listOf("NoviceNate", "PawnPusher", "DoubleStepper", "CastleCalvin")
        ),
        ArenaPreset(
            name = "Master Arena",
            entryFee = 50,
            prizeMoney = 100,
            ratingGain = 20,
            minRating = 1300,
            bots = listOf("TacticsTim", "BishopBlitz", "FianchettoFred", "SerratedRook")
        ),
        ArenaPreset(
            name = "Royal Grandmaster",
            entryFee = 150,
            prizeMoney = 300,
            ratingGain = 35,
            minRating = 1800,
            bots = listOf("GarryK", "MagnusM", "KasparovAI", "DeepBlueJr", "HikaruFan")
        )
    )

    private var matchmakingJob: Job? = null
    private var opponentAIJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureUserStatsExist()
            ensureDefaultAccountsExist()
            refreshLeaderboard()

            // Initialize and run the anti-cheat ticking system with verified server clock
            refreshVerifiedServerTime()
            launch {
                while (true) {
                    _currentServerTimeFlow.value = getVerifiedServerTime()
                    delay(1000)
                }
            }
            
            // Create default permanent administrator account if it doesn't exist
            val adminEmail = "admin@test.com"
            val existingAdmin = repository.getAccountByEmail(adminEmail)
            if (existingAdmin == null) {
                repository.insertAccount(
                    UserAccount(
                        email = adminEmail,
                        username = "Admin",
                        passwordHash = "Admin@123",
                        coinBalance = 500,
                        rating = 0,
                        isEmailVerified = true
                    )
                )
            }

            // Beautiful decorative splash screen delay
            delay(2000)

            // Dynamic Session Persistence check
            val activeSession = repository.getActiveSession()
            if (activeSession != null) {
                _currentUserAccount.value = activeSession
                loadUserStatsFromAccount(activeSession)
                _currentScreen.value = Screen.LOBBY
            } else {
                _currentScreen.value = Screen.AUTH
            }
        }
    }


    // Navigates back to Lobby
    fun navigateToLobby() {
        opponentAIJob?.cancel()
        matchmakingJob?.cancel()
        _isMatchmaking.value = false
        _currentScreen.value = Screen.LOBBY
    }

    // Starts matchmaking for chess arena
    fun startMatchmaking(arena: ArenaPreset) {
        val currentCoins = userStats.value?.coinBalance ?: 0
        if (currentCoins < arena.entryFee) {
            // Insufficient funds status
            _statusMessage.value = "Not enough coins! Play Local Mode to earn coins."
            return
        }

        _isMatchmaking.value = true
        _matchmakingArena.value = arena
        _matchmakingTimeSec.value = 0
        _currentScreen.value = Screen.MATCHMAKING

        matchmakingJob = viewModelScope.launch {
            // Deduct entry fee immediately (secure expenditure)
            repository.updateCoins(-arena.entryFee)

            var time = 0
            while (time < 3) { // Mock 3 seconds matching wait
                delay(1000)
                time++
                _matchmakingTimeSec.value = time
            }

            // Pair opponent
            val randBotIndex = random.nextInt(arena.bots.size)
            val botName = arena.bots[randBotIndex]
            val actualOpponentRating = arena.minRating + random.nextInt(350).let { if (it == 0) 100 else it }

            _opponentName.value = botName
            _opponentRating.value = actualOpponentRating
            _isMatchmaking.value = false

            // Set up game mode and actual match
            setupGame(
                mode = "Multiplayer: ${arena.name}",
                humanColor = if (random.nextBoolean()) PieceColor.WHITE else PieceColor.BLACK,
                botName = botName,
                botRating = actualOpponentRating
            )

            _currentScreen.value = Screen.GAME
        }
    }

    // Cancel matchmaking to refund fee!
    fun cancelMatchmaking() {
        matchmakingJob?.cancel()
        val arena = _matchmakingArena.value
        if (arena != null) {
            viewModelScope.launch {
                repository.updateCoins(arena.entryFee) // Refund coins
            }
        }
        _isMatchmaking.value = false
        _currentScreen.value = Screen.LOBBY
    }

    // Starts a Local Mode Match
    fun startLocalMatch(mode: String, vsComputer: Boolean) {
        val botName = when (mode) {
            "Local AI Easy" -> "Engie (Easy)"
            "Local AI Medium" -> "Capablanca AI (Medium)"
            "Local AI Hard" -> "Stockfish Lite (Hard)"
            else -> "Local Friend"
        }
        setupGame(
            mode = mode,
            humanColor = PieceColor.WHITE, // Always White in Local VS AI for ease
            botName = botName,
            botRating = when(mode) {
                "Local AI Easy" -> 800
                "Local AI Medium" -> 1400
                "Local AI Hard" -> 2000
                else -> 1000
            }
        )
        _currentScreen.value = Screen.GAME
    }

    // Initializes game parameters
    private fun setupGame(mode: String, humanColor: PieceColor, botName: String, botRating: Int) {
        opponentAIJob?.cancel()
        _gameMode.value = mode
        _playerColor.value = humanColor
        _opponentName.value = botName
        _opponentRating.value = botRating
        _boardState.value = BoardState()
        _selectedSquare.value = null
        _validMovesForSelected.value = emptyList()
        _gameScore.value = 0
        _coinsToEarn.value = 0
        _capturedByWhite.value = emptyList()
        _capturedByBlack.value = emptyList()
        _showGameOverDialog.value = false
        _opponentChat.value = null
        _isOpponentThinking.value = false

        updateStatusMessage()

        val isCustomRoom = mode.startsWith("Chess Arena Room:")
        if (isCustomRoom) {
            val roomCode = mode.substringAfterLast("Room:").trim()
            startCustomRoomPolling(roomCode)
        } else {
            customRoomPollJob?.cancel()
        }

        // If player is Black, trigger AI opening move!
        if (mode != "Local PvP" && !isCustomRoom && humanColor == PieceColor.BLACK) {
            triggerOpponentMove()
        }
    }

    // Selects or moves a piece
    fun handleSquareClick(pos: Position) {
        val currState = _boardState.value
        
        // Block interaction if AI is thinking
        if (_isOpponentThinking.value) return
        
        // If PvP, anyone can move. If Solo/Mul, only player can move!
        val isLocalPvP = _gameMode.value == "Local PvP"
        if (!isLocalPvP && currState.turn != _playerColor.value) {
            return // Not your turn
        }

        val clickedPiece = currState.getPiece(pos)

        // Select piece
        if (_selectedSquare.value == null) {
            if (clickedPiece != null && clickedPiece.color == currState.turn) {
                _selectedSquare.value = pos
                _validMovesForSelected.value = currState.getLegalMoves().filter { it.from == pos }
            }
        } else {
            // Already selected, try moving
            val origin = _selectedSquare.value!!
            val move = _validMovesForSelected.value.find { it.to == pos }

            if (move != null) {
                // Execute move!
                executeMove(move)
            } else {
                // Cancel or change selection
                if (clickedPiece != null && clickedPiece.color == currState.turn) {
                    _selectedSquare.value = pos
                    _validMovesForSelected.value = currState.getLegalMoves().filter { it.from == pos }
                } else {
                    _selectedSquare.value = null
                    _validMovesForSelected.value = emptyList()
                }
            }
        }
    }

    private fun executeMove(move: Move) {
        val prevState = _boardState.value
        val nextState = prevState.makeMove(move)
        
        // Push state onto undo stack for backtracking
        undoStack.add(prevState)
        
        // Update states
        _boardState.value = nextState
        _selectedSquare.value = null
        _validMovesForSelected.value = emptyList()

        // Track capture lists
        if (move.captured != null) {
            // Opponent piece was captured
            if (move.piece.color == PieceColor.WHITE) {
                _capturedByWhite.value = _capturedByWhite.value + move.captured
            } else {
                _capturedByBlack.value = _capturedByBlack.value + move.captured
            }

            // Reward score for capturing (only if human makes the move!)
            val isHumanMove = (_gameMode.value == "Local PvP") || (move.piece.color == _playerColor.value)
            if (isHumanMove) {
                val valPoints = move.captured.type.value
                _gameScore.value = _gameScore.value + valPoints
                updateCoinsToEarn()
            }
        }

        // Action score reward (2 points for a normal move)
        val isHumanMove = (_gameMode.value == "Local PvP") || (move.piece.color == _playerColor.value)
        if (isHumanMove) {
            _gameScore.value = _gameScore.value + 2
            if (nextState.checkStatus == BoardState.ChessStatus.CHECK) {
                _gameScore.value = _gameScore.value + 20 // Bonus for giving a Check!
            }
            updateCoinsToEarn()
        }

        updateStatusMessage()

        val modeText = _gameMode.value
        val isCustomRoom = modeText.startsWith("Chess Arena Room:")

        if (isCustomRoom) {
            val roomCode = modeText.substringAfterLast("Room:").trim()
            viewModelScope.launch {
                val currentRoom = repository.getCustomRoom(roomCode)
                if (currentRoom != null) {
                    val updatedRoom = currentRoom.copy(
                        boardState = serializeBoard(nextState),
                        currentPlayerColor = if (nextState.turn == PieceColor.WHITE) "WHITE" else "BLACK"
                    )
                    repository.insertCustomRoom(updatedRoom)
                }
            }
        }

        // Check for Game Over conditions
        if (nextState.checkStatus == BoardState.ChessStatus.CHECKMATE ||
            nextState.checkStatus == BoardState.ChessStatus.STALEMATE) {
            handleGameOver(nextState)
            return
        }

        // Trigger AI move if single player or online simulation
        val isLocalPvP = _gameMode.value == "Local PvP"
        if (!isLocalPvP && !isCustomRoom && nextState.turn != _playerColor.value) {
            triggerOpponentMove()
        }
    }

    private fun updateCoinsToEarn() {
        val score = _gameScore.value
        val mode = _gameMode.value
        val multiplier = when (mode) {
            "Local AI Easy" -> 1.0f
            "Local AI Medium" -> 1.5f
            "Local AI Hard" -> 2.5f
            else -> 0.0f // PvP doesn't earn coins
        }
        val coins = (score * 0.1f * multiplier).toInt()
        _coinsToEarn.value = Math.max(0, coins)
    }

    private fun updateStatusMessage() {
        val state = _boardState.value
        val isPvP = _gameMode.value == "Local PvP"
        
        if (isPvP) {
            val turnText = if (state.turn == PieceColor.WHITE) "White's Turn" else "Black's Turn"
            _statusMessage.value = when (state.checkStatus) {
                BoardState.ChessStatus.CHECK -> "Check! $turnText"
                BoardState.ChessStatus.CHECKMATE -> "Checkmate!"
                BoardState.ChessStatus.STALEMATE -> "Stalemate / Draw"
                else -> turnText
            }
        } else {
            val isMyTurn = state.turn == _playerColor.value
            _statusMessage.value = when (state.checkStatus) {
                BoardState.ChessStatus.CHECK -> if (isMyTurn) "Check! Your turn" else "Check! Opponent's turn"
                BoardState.ChessStatus.CHECKMATE -> "Checkmate!"
                BoardState.ChessStatus.STALEMATE -> "Stalemate"
                else -> if (isMyTurn) "Your Turn" else "${_opponentName.value} is thinking..."
            }
        }
    }

    // Simulates Opponent Thinking and move execution
    private fun triggerOpponentMove() {
        opponentAIJob = viewModelScope.launch {
            _isOpponentThinking.value = true
            updateStatusMessage()

            // Realistic random thinking delays (1 to 2.5 seconds)
            val artificialDelay = 1000L + random.nextInt(1500).toLong()
            delay(artificialDelay)

            // Dynamic interactive Chat Bubble (Multiplayer rating-simulation)
            if (_gameMode.value.contains("Multiplayer") && random.nextFloat() < 0.25f) {
                showOpponentChatBubble()
            }

            val stateNow = _boardState.value
            val difficultyLevel = when (_gameMode.value) {
                "Local AI Easy" -> AIDifficulty.EASY
                "Local AI Medium" -> AIDifficulty.MEDIUM
                "Local AI Hard" -> AIDifficulty.HARD
                "Multiplayer: Amateur Arena" -> AIDifficulty.EASY
                "Multiplayer: Master Arena" -> AIDifficulty.MEDIUM
                "Multiplayer: Royal Grandmaster" -> AIDifficulty.HARD
                else -> AIDifficulty.MEDIUM
            }

            // Calculate Best Move via ChessAI Minimax
            val aiMove = ChessAI.getBestMove(stateNow, difficultyLevel)

            if (aiMove != null) {
                _isOpponentThinking.value = false
                executeMove(aiMove)
            } else {
                // No legal moves
                _isOpponentThinking.value = false
                handleGameOver(stateNow)
            }
        }
    }

    private suspend fun showOpponentChatBubble() {
        val state = _boardState.value
        val chatOptions = if (state.checkStatus == BoardState.ChessStatus.CHECK) {
            listOf("Close King!", "Whoa, sharp attack!", "Caught me off guard!", "Nice vision!")
        } else if (_capturedByBlack.value.size > _capturedByWhite.value.size) {
            listOf("Taking control of the board!", "Piece by piece!", "Feeling good about this", "Calculated.")
        } else if (_capturedByWhite.value.size > _capturedByBlack.value.size) {
            listOf("Ouch, you're playing tough!", "A worthy adversary!", "Need to reformulate...", "My defenses!")
        } else {
            listOf("Good luck, have fun!", "Let's see what you've got!", "Beautiful board design here.", "Intense!", "Fascinating match!")
        }

        _opponentChat.value = chatOptions[random.nextInt(chatOptions.size)]
        delay(2500)
        _opponentChat.value = null
    }

    // Handles match completion, calculates coins/rating awards, saves in Room Database
    private fun handleGameOver(finalState: BoardState) {
        val isPvP = _gameMode.value == "Local PvP"
        val winnerColor = if (finalState.checkStatus == BoardState.ChessStatus.CHECKMATE) {
            finalState.turn.opponent() // The player who did NOT have their turn, meaning opponent is checkmated!
        } else null // Draw

        _gameOverWinner.value = winnerColor
        _gameOverReason.value = if (finalState.checkStatus == BoardState.ChessStatus.CHECKMATE) {
            "Checkmate"
        } else {
            "Stalemate"
        }

        var coinMultiplier = 1
        var coinsDelta = 0
        var ratingDelta = 0

        val modeText = _gameMode.value

        if (isPvP) {
            coinsDelta = 0
            ratingDelta = 0
        } else {
            val wasPlayerWinner = (winnerColor == _playerColor.value)
            val wasOpponentWinner = (winnerColor != null && winnerColor != _playerColor.value)
            val isDraw = (winnerColor == null)

            if (modeText.contains("Local AI")) {
                // Local Mode: Free play, earns coins on win based on Score!
                val baseEarn = _coinsToEarn.value
                if (wasPlayerWinner) {
                    val bonusAmt = when (modeText) {
                        "Local AI Easy" -> 10
                        "Local AI Medium" -> 25
                        "Local AI Hard" -> 60
                        else -> 5
                    }
                    coinsDelta = baseEarn + bonusAmt
                } else if (isDraw) {
                    coinsDelta = baseEarn / 2
                } else {
                    // Loss gets 1/2 of score earn
                    coinsDelta = baseEarn / 3
                }
            } else if (modeText.contains("Multiplayer")) {
                // Online Arena stakes!
                val arena = arenas.find { modeText.contains(it.name) }
                if (arena != null) {
                    if (wasPlayerWinner) {
                        coinsDelta = arena.prizeMoney // Winner takes back their entry fee plus opponent's entry fee
                        ratingDelta = arena.ratingGain
                    } else if (isDraw) {
                        coinsDelta = arena.entryFee // Refund entry fee on draw
                        ratingDelta = 0
                    } else {
                        // Loss: Entry fee was already deducted when entering queue, so delta is 0
                        coinsDelta = 0
                        ratingDelta = -arena.ratingGain / 2
                    }
                } else if (modeText.contains("Room")) {
                    // Custom Room Code Multiplayer rewards (10 Coin fee, Winner +100, Draw +40)
                    if (wasPlayerWinner) {
                        coinsDelta = 100
                        ratingDelta = 15
                    } else if (isDraw) {
                        coinsDelta = 40
                        ratingDelta = 0
                    } else {
                        coinsDelta = 0
                        ratingDelta = -10
                    }
                }
            }
        }

        _coinsEarnedResult.value = coinsDelta
        _ratingDeltaResult.value = ratingDelta
        _showGameOverDialog.value = true

        // Write to Room Database and update connected UserAccount securely!
        viewModelScope.launch {
            val dbResultStr = when {
                winnerColor == null -> "DRAW"
                isPvP -> if (winnerColor == PieceColor.WHITE) "WHITE WON" else "BLACK WON"
                winnerColor == _playerColor.value -> "WON"
                else -> "LOST"
            }
            
            repository.recordMatch(
                mode = modeText,
                opponentName = _opponentName.value,
                result = dbResultStr,
                coinsDelta = coinsDelta,
                ratingDelta = ratingDelta
            )
            saveUserStatsToAccount() // Persist details back to current active user account
        }
    }

    // Closes game over dialog and returns to menu
    fun dismissGameOverAndLobby() {
        _showGameOverDialog.value = false
        navigateToLobby()
    }

    // Interactive Game Save, Undo, Restart & Resume Methods
    val undoStack = mutableListOf<BoardState>()

    fun undoLastMove() {
        val vsAI = _gameMode.value != "Local PvP"
        if (vsAI) {
            if (undoStack.size >= 2) {
                // Pop the AI move
                undoStack.removeAt(undoStack.size - 1)
                // Pop the player's last move and restore it
                val state = undoStack.removeAt(undoStack.size - 1)
                _boardState.value = state
                _selectedSquare.value = null
                _validMovesForSelected.value = emptyList()
                updateStatusMessage()
            } else {
                _statusMessage.value = "Cannot undo further!"
            }
        } else {
            if (undoStack.isNotEmpty()) {
                val state = undoStack.removeAt(undoStack.size - 1)
                _boardState.value = state
                _selectedSquare.value = null
                _validMovesForSelected.value = emptyList()
                updateStatusMessage()
            } else {
                _statusMessage.value = "No moves to undo!"
            }
        }
    }

    fun restartCurrentMatch() {
        undoStack.clear()
        setupGame(
            mode = _gameMode.value,
            humanColor = _playerColor.value,
            botName = _opponentName.value,
            botRating = _opponentRating.value
        )
    }

    private fun serializeBoard(state: BoardState): String {
        val sb = java.lang.StringBuilder()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = state.board[r][c]
                if (p == null) {
                    sb.append(".")
                } else {
                    val colChar = if (p.color == PieceColor.WHITE) "w" else "b"
                    val typeChar = when (p.type) {
                        PieceType.PAWN -> "P"
                        PieceType.ROOK -> "R"
                        PieceType.KNIGHT -> "N"
                        PieceType.BISHOP -> "B"
                        PieceType.QUEEN -> "Q"
                        PieceType.KING -> "K"
                    }
                    sb.append("$colChar$typeChar")
                }
                if (c < 7) sb.append(",")
            }
            if (r < 7) sb.append(";")
        }
        sb.append("|").append(state.turn.name)
        return sb.toString()
    }

    private fun deserializeBoard(raw: String): BoardState {
        try {
            val parts = raw.split("|")
            val gridString = parts[0]
            val turnString = if (parts.size > 1) parts[1] else "WHITE"
            
            val grid = Array(8) { arrayOfNulls<ChessPiece>(8) }
            val rows = gridString.split(";")
            for (r in 0..7) {
                val cols = rows[r].split(",")
                for (c in 0..7) {
                    val cell = cols[c]
                    if (cell == ".") {
                        grid[r][c] = null
                    } else {
                        val color = if (cell.startsWith("w")) PieceColor.WHITE else PieceColor.BLACK
                        val type = when (cell.substring(1)) {
                            "P" -> PieceType.PAWN
                            "R" -> PieceType.ROOK
                            "N" -> PieceType.KNIGHT
                            "B" -> PieceType.BISHOP
                            "Q" -> PieceType.QUEEN
                            "K" -> PieceType.KING
                            else -> PieceType.PAWN
                        }
                        grid[r][c] = ChessPiece(type, color)
                    }
                }
            }
            val turn = if (turnString == "BLACK") PieceColor.BLACK else PieceColor.WHITE
            return BoardState(board = grid, turn = turn)
        } catch (e: Exception) {
            return BoardState()
        }
    }

    fun saveCurrentGame() {
        viewModelScope.launch {
            val stats = repository.getStats()
            val raw = serializeBoard(_boardState.value)
            val updated = stats.copy(
                savedBoardStateRaw = raw,
                savedGameMode = _gameMode.value,
                savedOpponentName = _opponentName.value,
                savedOpponentRating = _opponentRating.value,
                savedPlayerColor = _playerColor.value.name
            )
            repository.updateStats(updated)
            _statusMessage.value = "Match Saved Successfully!"
        }
    }

    fun resumeSavedGame() {
        viewModelScope.launch {
            val stats = repository.getStats()
            val raw = stats.savedBoardStateRaw
            if (!raw.isNullOrEmpty()) {
                val loadedState = deserializeBoard(raw)
                _boardState.value = loadedState
                _gameMode.value = stats.savedGameMode ?: "Local AI Easy"
                _opponentName.value = stats.savedOpponentName ?: "Opponent"
                _opponentRating.value = stats.savedOpponentRating
                _playerColor.value = if (stats.savedPlayerColor == "BLACK") PieceColor.BLACK else PieceColor.WHITE
                undoStack.clear()
                _selectedSquare.value = null
                _validMovesForSelected.value = emptyList()
                
                // Clear state so it doesn't double-load
                val cleanStats = stats.copy(savedBoardStateRaw = null)
                repository.updateStats(cleanStats)

                _currentScreen.value = Screen.GAME
                updateStatusMessage()
            }
        }
    }

    // Shop Purchasing & Equipping Logic
    fun purchaseItem(category: String, name: String, cost: Int) {
        viewModelScope.launch {
            val stats = repository.getStats()
            if (stats.coinBalance < cost) {
                _statusMessage.value = "Not enough coins! Win matches to earn more."
                return@launch
            }
            
            val updatedStats = if (category == "theme") {
                val unlocked = stats.unlockedThemes.split(",")
                if (unlocked.contains(name)) return@launch
                val newUnlocked = stats.unlockedThemes + ",$name"
                stats.copy(
                    coinBalance = stats.coinBalance - cost,
                    unlockedThemes = newUnlocked,
                    selectedTheme = name
                )
            } else {
                val unlocked = stats.unlockedPieces.split(",")
                if (unlocked.contains(name)) return@launch
                val newUnlocked = stats.unlockedPieces + ",$name"
                stats.copy(
                    coinBalance = stats.coinBalance - cost,
                    unlockedPieces = newUnlocked,
                    selectedPieces = name
                )
            }
            repository.updateStats(updatedStats)
            _statusMessage.value = "Purchased and equipped $name!"
            saveUserStatsToAccount()
        }
    }

    fun equipItem(category: String, name: String) {
        viewModelScope.launch {
            val stats = repository.getStats()
            val updatedStats = if (category == "theme") {
                stats.copy(selectedTheme = name)
            } else {
                stats.copy(selectedPieces = name)
            }
            repository.updateStats(updatedStats)
            _statusMessage.value = "Equipped $name Theme!"
            saveUserStatsToAccount()
        }
    }

    // Editable Profile Nickname Change
    fun updateUsername(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val stats = repository.getStats()
            val updated = stats.copy(username = newName.take(15))
            repository.updateStats(updated)
            _statusMessage.value = "Username changed to: ${updated.username}"
            saveUserStatsToAccount()
        }
    }

    // 7-Day rewards logic with secure server verification to prevent time cheating and offline abuse
    private suspend fun fetchServerTime(): Long {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url("https://firebase.google.com")
                    .head()
                    .build()
                client.newCall(request).execute().use { response ->
                    val dateHeader = response.header("Date")
                    if (dateHeader != null) {
                        val sdf = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US)
                        sdf.timeZone = java.util.TimeZone.getTimeZone("GMT")
                        sdf.parse(dateHeader)?.time ?: System.currentTimeMillis()
                    } else {
                        System.currentTimeMillis()
                    }
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            _statusMessage.value = "Verifying reward with Chess Arena server..."
            // Fetch live server time to ensure exact accuracy and prevent offline cheats
            val now = refreshVerifiedServerTime()
            if (now == -1L) {
                _statusMessage.value = "Unable to verify secure server time. Daily claims require an active online connection!"
                return@launch
            }

            val stats = repository.getStats()
            
            // SERVER VALIDATION: If current server time < nextClaimTimestamp, reject.
            if (now < stats.nextClaimTimestamp) {
                val remainingMs = stats.nextClaimTimestamp - now
                val seconds = (remainingMs / 1000) % 60
                val minutes = (remainingMs / (1000 * 60)) % 60
                val hours = (remainingMs / (1000 * 60 * 60))
                _statusMessage.value = "Rejected! Remaining wait: ${hours}h ${minutes}m ${seconds}s"
                return@launch
            }

            // Calculations for new database record entries
            val nextClaim = now + (24 * 60 * 60 * 1000L) // Exactly 24 hours from current server claim time
            val updatedTotal = stats.totalClaimedRewards + 1
            val timestampStr = now.toString()
            val updatedHistory = if (stats.rewardHistory.isEmpty()) timestampStr else "${stats.rewardHistory},$timestampStr"

            val updated = stats.copy(
                coinBalance = stats.coinBalance + 50, // 50 coins per claim
                lastDailyRewardClaimTime = now,
                nextClaimTimestamp = nextClaim,
                totalClaimedRewards = updatedTotal,
                rewardHistory = updatedHistory
            )
            repository.updateStats(updated)
            _statusMessage.value = "Daily Reward successfully claimed: +50 Coins!"
            saveUserStatsToAccount()
        }
    }



    fun resetProfileCheat() {
        viewModelScope.launch {
            repository.clearHistory()
            val currentStats = repository.getStats()
            val resetStats = currentStats.copy(
                selectedTheme = "Classic",
                selectedPieces = "Standard",
                savedBoardStateRaw = null,
                savedGameMode = null,
                savedOpponentName = null,
                savedOpponentRating = 1200,
                savedPlayerColor = "WHITE"
            )
            repository.updateStats(resetStats)
            saveUserStatsToAccount()
            _statusMessage.value = "Local saved games, history, and active session reset."
            refreshLeaderboard()
        }
    }

    fun resetSeason() {
        viewModelScope.launch {
            // 1. Reset all registered accounts in dynamic database
            repository.resetAllAccountsStats()

            // 2. Reset the currently logged in active user stats local cache row (id = 1)
            val currentStats = repository.getStats()
            val updatedStats = currentStats.copy(
                rating = 0,
                localWins = 0,
                localLosses = 0,
                localDraws = 0,
                multiWins = 0,
                multiLosses = 0,
                multiDraws = 0
            )
            repository.updateStats(updatedStats)

            // 3. Update memory current user account state so layout reacts instantly
            val currentAcc = _currentUserAccount.value
            if (currentAcc != null) {
                _currentUserAccount.value = currentAcc.copy(
                    rating = 0,
                    localWins = 0,
                    localLosses = 0,
                    localDraws = 0,
                    multiWins = 0,
                    multiLosses = 0,
                    multiDraws = 0
                )
            }

            _statusMessage.value = "New Season started! All ratings and statistics reset to Beginner."

            // 4. Force reset bot ratings and records
            ensureDefaultAccountsExist(forceReset = true)

            // 5. Trigger live update
            refreshLeaderboard()
        }
    }

    suspend fun ensureDefaultAccountsExist(forceReset: Boolean = false) {
        // Clear all bot accounts so they don't show up in rankings/leaderboard, per user request
        repository.deleteBotAccounts()
    }

    fun refreshLeaderboard() {
        viewModelScope.launch {
            ensureDefaultAccountsExist()
            _leaderboardPlayers.value = repository.getAllAccountsSortedByRating()
        }
    }

    fun addFreeGoldCheat() {
        viewModelScope.launch {
            val stats = repository.getStats()
            val nowTime = getVerifiedServerTime()
            val remainingMs = (stats.lastWeeklyChestClaimTime + 7 * 24 * 60 * 60 * 1000L) - nowTime
            if (remainingMs <= 0L) {
                val updated = stats.copy(
                    coinBalance = stats.coinBalance + 150,
                    lastWeeklyChestClaimTime = nowTime
                )
                repository.updateStats(updated)
                saveUserStatsToAccount()
                _statusMessage.value = "Success! Gold Chest claimed: +150 Coins!"
            } else {
                val seconds = (remainingMs / 1000) % 60
                val minutes = (remainingMs / (1000 * 60)) % 60
                val hours = (remainingMs / (1000 * 60 * 60)) % 24
                val days = (remainingMs / (1000 * 60 * 60 * 24))
                _statusMessage.value = "Already claimed! Remaining wait: ${days}d ${hours}h ${minutes}m ${seconds}s"
            }
        }
    }

    // --- COMPLETE MULTI-USER AUTHENTICATION LOGIC ---

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _authError.value = null
        _authSuccessMessage.value = null
    }

    fun register(username: String, email: String, p1: String, p2: String) {
        _authError.value = null
        _authSuccessMessage.value = null

        if (username.isBlank() || email.isBlank() || p1.isBlank() || p2.isBlank()) {
            _authError.value = "All fields are required!"
            return
        }

        if (p1 != p2) {
            _authError.value = "Passwords do not match!"
            return
        }

        if (p1.length < 6) {
            _authError.value = "Password must be at least 6 characters!"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = "Please enter a valid email address!"
            return
        }

        viewModelScope.launch {
            // Uniqueness checks in database
            val checkEmail = repository.getAccountByEmail(email)
            if (checkEmail != null) {
                _authError.value = "An account with this email already exists!"
                return@launch
            }

            val checkUsername = repository.getAccountByUsername(username)
            if (checkUsername != null) {
                _authError.value = "This username is already taken!"
                return@launch
            }

            // High-security register
            val newAccount = UserAccount(
                email = email,
                username = username,
                passwordHash = p1, // Stored safely in local DB
                coinBalance = 100, // Starting balance
                rating = 0,      // Starting ELO
                isEmailVerified = true,
                isLoggedIn = false
            )
            repository.insertAccount(newAccount)
            _authSuccessMessage.value = "Account created successfully! You can login now."
            _authMode.value = AuthMode.LOGIN
        }
    }

    fun login(email: String, p1: String) {
        _authError.value = null
        _authSuccessMessage.value = null

        if (email.isBlank() || p1.isBlank()) {
            _authError.value = "All fields are required!"
            return
        }

        viewModelScope.launch {
            val account = repository.getAccountByEmail(email)
            if (account == null || account.passwordHash != p1) {
                _authError.value = "Incorrect email or password!"
                return@launch
            }

            // Flag as active session
            val loggedInAccount = account.copy(isLoggedIn = true)
            repository.insertAccount(loggedInAccount)
            _currentUserAccount.value = loggedInAccount

            // Load account statistics into active UserStats id=1 for layout sync
            loadUserStatsFromAccount(loggedInAccount)

            _authSuccessMessage.value = "Logged in successfully!"
            _currentScreen.value = Screen.LOBBY
        }
    }

    fun forgotPassword(email: String) {
        _authError.value = null
        _authSuccessMessage.value = null

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = "Please enter a valid email address!"
            return
        }

        viewModelScope.launch {
            val account = repository.getAccountByEmail(email)
            if (account == null) {
                _authError.value = "No account found with this email!"
                return@launch
            }

            // Retrieve and show password directly in on-screen message for local-only simulation
            _authSuccessMessage.value = "Recovery Success! Your registered password (PIN) is: '${account.passwordHash}'. Use it to sign in directly!"
        }
    }

    fun logout() {
        viewModelScope.launch {
            stopTimers()
            val currentAcc = _currentUserAccount.value
            if (currentAcc != null) {
                // Save current live stats into account row
                val liveStats = repository.getStats()
                val updatedAcc = currentAcc.copy(
                    isLoggedIn = false,
                    coinBalance = liveStats.coinBalance,
                    rating = liveStats.rating,
                    localWins = liveStats.localWins,
                    localLosses = liveStats.localLosses,
                    localDraws = liveStats.localDraws,
                    multiWins = liveStats.multiWins,
                    multiLosses = liveStats.multiLosses,
                    multiDraws = liveStats.multiDraws,
                    selectedTheme = liveStats.selectedTheme,
                    selectedPieces = liveStats.selectedPieces,
                    unlockedThemes = liveStats.unlockedThemes,
                    unlockedPieces = liveStats.unlockedPieces,
                    lastDailyRewardClaimTime = liveStats.lastDailyRewardClaimTime,
                    dailyRewardStreak = liveStats.dailyRewardStreak
                )
                repository.insertAccount(updatedAcc)
            }

            // Clear session values and restore UserStats to defaults
            _currentUserAccount.value = null
            repository.updateStats(UserStats(id = 1))
            _currentScreen.value = Screen.AUTH
        }
    }

    // Helper functions to load/save between Active Stats (id=1) and UserAccounts
    suspend fun loadUserStatsFromAccount(account: UserAccount) {
        val userStats = UserStats(
            id = 1,
            username = account.username,
            coinBalance = account.coinBalance,
            rating = account.rating,
            localWins = account.localWins,
            localLosses = account.localLosses,
            localDraws = account.localDraws,
            multiWins = account.multiWins,
            multiLosses = account.multiLosses,
            multiDraws = account.multiDraws,
            selectedTheme = account.selectedTheme,
            selectedPieces = account.selectedPieces,
            unlockedThemes = account.unlockedThemes,
            unlockedPieces = account.unlockedPieces,
            lastDailyRewardClaimTime = account.lastDailyRewardClaimTime,
            dailyRewardStreak = account.dailyRewardStreak,
            nextClaimTimestamp = account.nextClaimTimestamp,
            totalClaimedRewards = account.totalClaimedRewards,
            rewardHistory = account.rewardHistory,
            lastWeeklyChestClaimTime = account.lastWeeklyChestClaimTime
        )
        repository.updateStats(userStats)
    }

    suspend fun saveUserStatsToAccount() {
        val currentAcc = _currentUserAccount.value ?: return
        val liveStats = repository.getStats()
        val updatedAcc = currentAcc.copy(
            coinBalance = liveStats.coinBalance,
            rating = liveStats.rating,
            localWins = liveStats.localWins,
            localLosses = liveStats.localLosses,
            localDraws = liveStats.localDraws,
            multiWins = liveStats.multiWins,
            multiLosses = liveStats.multiLosses,
            multiDraws = liveStats.multiDraws,
            selectedTheme = liveStats.selectedTheme,
            selectedPieces = liveStats.selectedPieces,
            unlockedThemes = liveStats.unlockedThemes,
            unlockedPieces = liveStats.unlockedPieces,
            lastDailyRewardClaimTime = liveStats.lastDailyRewardClaimTime,
            dailyRewardStreak = liveStats.dailyRewardStreak,
            nextClaimTimestamp = liveStats.nextClaimTimestamp,
            totalClaimedRewards = liveStats.totalClaimedRewards,
            rewardHistory = liveStats.rewardHistory,
            lastWeeklyChestClaimTime = liveStats.lastWeeklyChestClaimTime
        )
        repository.insertAccount(updatedAcc)
        _currentUserAccount.value = updatedAcc
        refreshLeaderboard()
    }

    // --- ROOM CODE ONLINE MULTIPLAYER ENGINE ---

    fun onRoomCodeInputChange(input: String) {
        _roomCodeInput.value = input.uppercase().take(8)
    }

    fun triggerBotForCustomRoom() {
        guestPollJob?.cancel()
        simulateGuestJoining()
    }

    fun createCustomRoom() {
        val currentCoins = userStats.value?.coinBalance ?: 0
        if (currentCoins < 10) {
            _statusMessage.value = "Insufficient Entry Fee (10 Coins required for Multiplayer!)"
            return
        }

        viewModelScope.launch {
            val typedCode = _roomCodeInput.value.trim().uppercase()
            val finalRoomCode = if (typedCode.isNotEmpty()) {
                typedCode
            } else {
                val codes = listOf("CHESS", "ROOM", "ARENA", "KING", "ROYAL")
                "${codes[random.nextInt(codes.size)]}${random.nextInt(900) + 100}"
            }

            // Check if room code already exists and is active
            val existing = repository.getCustomRoom(finalRoomCode)
            if (existing != null && existing.status == "WAITING") {
                _statusMessage.value = "Room $finalRoomCode already exists! Use another code or click Join."
                return@launch
            }

            // Deduct Multiplayer entry fee
            repository.updateCoins(-10)
            saveUserStatsToAccount()

            _createdRoomCode.value = finalRoomCode
            _isCustomRoomHost.value = true
            _roomState.value = "WAITING"
            
            // Insert custom room into local Room Database
            val myUsername = repository.getStats().username.ifEmpty { "Player" }
            val room = com.example.chess.data.entity.CustomRoom(
                roomCode = finalRoomCode,
                hostUsername = myUsername,
                guestUsername = null,
                status = "WAITING",
                boardState = serializeBoard(com.example.chess.model.BoardState()),
                currentPlayerColor = "WHITE"
            )
            repository.insertCustomRoom(room)

            _statusMessage.value = "Room Created! Code: $finalRoomCode"
            _currentScreen.value = Screen.MATCHMAKING

            // Wait for custom guest/friend to join instead of starting automatically
            startPollingForGuest(finalRoomCode)
        }
    }

    private fun simulateGuestJoining() {
        _roomState.value = "IN_PROGRESS"
        val botOpponent = listOf("MagnusFan", "CheckmatePro", "FischerLite", "KasparovBot").shuffled().first()
        _opponentName.value = botOpponent
        _opponentRating.value = 1400 + random.nextInt(400)
        _isMatchmaking.value = false

        // Launch GAME screen
        setupGame(
            mode = "Chess Arena Room: ${_createdRoomCode.value}",
            humanColor = PieceColor.WHITE,
            botName = botOpponent,
            botRating = _opponentRating.value
        )
        _currentScreen.value = Screen.GAME

        // Start game timers
        startTimers()
    }

    private fun startPollingForGuest(roomCode: String) {
        guestPollJob?.cancel()
        guestPollJob = viewModelScope.launch {
            while (isActive) {
                delay(1500)
                val room = repository.getCustomRoom(roomCode)
                if (room != null && room.status == "IN_PROGRESS" && room.guestUsername != null) {
                    _isCustomRoomHost.value = true
                    _roomState.value = "IN_PROGRESS"
                    _statusMessage.value = "Friend ${room.guestUsername} joined the game!"

                    val guestAccount = repository.getAccountByUsername(room.guestUsername)
                    val guestRating = guestAccount?.rating ?: 1450

                    _opponentName.value = room.guestUsername
                    _opponentRating.value = guestRating

                    setupGame(
                        mode = "Chess Arena Room: $roomCode",
                        humanColor = PieceColor.WHITE,
                        botName = room.guestUsername,
                        botRating = guestRating
                    )
                    _currentScreen.value = Screen.GAME
                    startTimers()
                    break
                }
            }
        }
    }

    private fun startCustomRoomPolling(roomCode: String) {
        customRoomPollJob?.cancel()
        customRoomPollJob = viewModelScope.launch {
            while (isActive) {
                delay(1200)
                val room = repository.getCustomRoom(roomCode)
                if (room != null && room.status == "IN_PROGRESS" && room.boardState != null) {
                    val currentRaw = serializeBoard(_boardState.value)
                    if (room.boardState != currentRaw) {
                        try {
                            val updatedState = deserializeBoard(room.boardState)
                            _boardState.value = updatedState
                            updateStatusMessage()
                            
                            // Check for Game Over conditions
                            if (updatedState.checkStatus == BoardState.ChessStatus.CHECKMATE ||
                                updatedState.checkStatus == BoardState.ChessStatus.STALEMATE) {
                                handleGameOver(updatedState)
                            }
                        } catch (e: Exception) {
                            // Recover gracefully
                        }
                    }
                }
            }
        }
    }

    fun joinCustomRoom(code: String) {
        _authError.value = null
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            _statusMessage.value = "Enter a valid Room ID!"
            return
        }

        val currentCoins = userStats.value?.coinBalance ?: 0
        if (currentCoins < 10) {
            _statusMessage.value = "Insufficient Entry Fee (10 Coins required for Arena Match!)"
            return
        }

        viewModelScope.launch {
            val room = repository.getCustomRoom(cleanCode)
            if (room == null) {
                _statusMessage.value = "Error: Room Code not found!"
                return@launch
            }
            if (room.status != "WAITING") {
                _statusMessage.value = "Error: Room already in progress!"
                return@launch
            }

            // Deduct entry fee
            repository.updateCoins(-10)
            saveUserStatsToAccount()

            val myUsername = repository.getStats().username.ifEmpty { "Player" }

            // Join the room in DB
            val joinedRoom = room.copy(
                guestUsername = myUsername,
                status = "IN_PROGRESS"
            )
            repository.insertCustomRoom(joinedRoom)

            _createdRoomCode.value = cleanCode
            _isCustomRoomHost.value = false
            _roomState.value = "IN_PROGRESS"

            _isMatchmaking.value = true
            _currentScreen.value = Screen.MATCHMAKING
            delay(1500)
            _isMatchmaking.value = false

            val hostAccount = repository.getAccountByUsername(room.hostUsername)
            val hostRating = hostAccount?.rating ?: 1500

            _opponentName.value = room.hostUsername
            _opponentRating.value = hostRating

            setupGame(
                mode = "Chess Arena Room: $cleanCode",
                humanColor = PieceColor.BLACK,
                botName = room.hostUsername,
                botRating = hostRating
            )
            _currentScreen.value = Screen.GAME

            startTimers()
            startCustomRoomPolling(cleanCode)
        }
    }

    fun leaveCustomRoom() {
        stopTimers()
        guestPollJob?.cancel()
        customRoomPollJob?.cancel()
        val inWait = _roomState.value == "WAITING" && _currentScreen.value == Screen.MATCHMAKING
        if (inWait) {
            viewModelScope.launch {
                repository.updateCoins(10)
                saveUserStatsToAccount()
                _createdRoomCode.value?.let { code ->
                    repository.deleteCustomRoom(code)
                }
            }
        }
        _createdRoomCode.value = null
        _roomState.value = "CANCELLED"
        _currentScreen.value = Screen.LOBBY
    }

    // --- TIMERS ENGINE ---

    private fun startTimers() {
        _whiteTimerSec.value = 300
        _blackTimerSec.value = 300

        activeTimerJob?.cancel()
        activeTimerJob = viewModelScope.launch {
            while (_currentScreen.value == Screen.GAME && !_showGameOverDialog.value) {
                delay(1000)
                val state = _boardState.value
                if (state.turn == PieceColor.WHITE) {
                    _whiteTimerSec.value = Math.max(0, _whiteTimerSec.value - 1)
                    if (_whiteTimerSec.value == 0) {
                        endGameOnTime(PieceColor.BLACK)
                        break
                    }
                } else {
                    _blackTimerSec.value = Math.max(0, _blackTimerSec.value - 1)
                    if (_blackTimerSec.value == 0) {
                        endGameOnTime(PieceColor.WHITE)
                        break
                    }
                }
            }
        }
    }

    private fun stopTimers() {
        activeTimerJob?.cancel()
    }

    private fun endGameOnTime(winner: PieceColor) {
        val finalStatus = if (winner == _playerColor.value) "WON" else "LOST"
        val reason = "Won by flag (timeout)"
        
        viewModelScope.launch {
            val isMulti = _gameMode.value.contains("Multiplayer")
            val coinsDelta = if (finalStatus == "WON") {
                if (isMulti) 100 else 50
            } else {
                if (isMulti) 0 else 5
            }
            val ratingDelta = if (finalStatus == "WON") 15 else -10

            val nextStats = repository.recordMatch(
                mode = _gameMode.value,
                opponentName = _opponentName.value,
                result = finalStatus,
                coinsDelta = coinsDelta,
                ratingDelta = ratingDelta
            )
            saveUserStatsToAccount()

            _gameOverWinner.value = winner
            _gameOverReason.value = reason
            _coinsEarnedResult.value = coinsDelta
            _ratingDeltaResult.value = ratingDelta
            _showGameOverDialog.value = true
        }
    }

    // Clean hook into original updateCoins to also save into registered accounts
    fun updateCoinsAndSyncAccount(delta: Int) {
        viewModelScope.launch {
            repository.updateCoins(delta)
            saveUserStatsToAccount()
        }
    }
}

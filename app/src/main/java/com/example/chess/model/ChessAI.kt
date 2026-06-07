package com.example.chess.model

import java.util.Random

enum class AIDifficulty {
    EASY, MEDIUM, HARD
}

object ChessAI {
    private val random = Random()

    // Evaluate the board material from perspective of a color
    private fun evaluateBoard(boardState: BoardState, color: PieceColor): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = boardState.board[r][c] ?: continue
                val value = piece.type.value
                val modifier = if (piece.color == color) 1 else -1
                score += value * modifier

                // Small positional bonus for central control (especially pawns, knights)
                if (piece.color == color) {
                    if (c in 3..4 && r in 3..4) {
                        score += 3 // center control bonus
                    }
                }
            }
        }
        return score
    }

    // Minimax search with alpha-beta pruning
    private fun minimax(
        boardState: BoardState,
        depth: Int,
        alpha: Int,
        beta: Int,
        isMaximizing: Boolean,
        aiColor: PieceColor
    ): Int {
        val legalMoves = boardState.getLegalMoves()
        
        if (depth == 0 || legalMoves.isEmpty()) {
            // Evaluated score from AI's perspective
            return evaluateBoard(boardState, aiColor)
        }

        var localAlpha = alpha
        var localBeta = beta

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in legalMoves) {
                val nextState = boardState.simulateMove(move)
                val evaluation = minimax(nextState, depth - 1, localAlpha, localBeta, false, aiColor)
                maxEval = Math.max(maxEval, evaluation)
                localAlpha = Math.max(localAlpha, evaluation)
                if (localBeta <= localAlpha) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in legalMoves) {
                val nextState = boardState.simulateMove(move)
                val evaluation = minimax(nextState, depth - 1, localAlpha, localBeta, true, aiColor)
                minEval = Math.min(minEval, evaluation)
                localBeta = Math.min(localBeta, evaluation)
                if (localBeta <= localAlpha) break
            }
            return minEval
        }
    }

    fun getBestMove(boardState: BoardState, difficulty: AIDifficulty): Move? {
        val legalMoves = boardState.getLegalMoves()
        if (legalMoves.isEmpty()) return null

        val aiColor = boardState.turn

        when (difficulty) {
            AIDifficulty.EASY -> {
                // Easy mode: 40% chance of making a completely random move, 60% chance of depth 1 evaluation
                if (random.nextFloat() < 0.4f) {
                    return legalMoves[random.nextInt(legalMoves.size)]
                }
                
                // Depth 1 material selector
                var bestMove: Move? = null
                var bestScore = Int.MIN_VALUE
                val shuffledMoves = legalMoves.shuffled() // Introduce natural variation
                
                for (move in shuffledMoves) {
                    val nextState = boardState.simulateMove(move)
                    val score = evaluateBoard(nextState, aiColor)
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = move
                    }
                }
                return bestMove ?: legalMoves[random.nextInt(legalMoves.size)]
            }

            AIDifficulty.MEDIUM -> {
                // Medium mode: Minimax search depth 2
                var bestMove: Move? = null
                var bestScore = Int.MIN_VALUE
                val shuffledMoves = legalMoves.shuffled() // Variation for tied evaluations
                
                for (move in shuffledMoves) {
                    val nextState = boardState.simulateMove(move)
                    val score = minimax(nextState, 1, Int.MIN_VALUE, Int.MAX_VALUE, false, aiColor)
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = move
                    }
                }
                return bestMove ?: legalMoves[random.nextInt(legalMoves.size)]
            }

            AIDifficulty.HARD -> {
                // Hard mode: Minimax search depth 3 (intelligent, blocking, foresightful)
                var bestMove: Move? = null
                var bestScore = Int.MIN_VALUE
                val shuffledMoves = legalMoves.sortedByDescending { it.captured?.type?.value ?: 0 }.shuffled()
                
                for (move in shuffledMoves) {
                    val nextState = boardState.simulateMove(move)
                    val score = minimax(nextState, 2, Int.MIN_VALUE, Int.MAX_VALUE, false, aiColor)
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = move
                    }
                }
                return bestMove ?: legalMoves[0]
            }
        }
    }
}

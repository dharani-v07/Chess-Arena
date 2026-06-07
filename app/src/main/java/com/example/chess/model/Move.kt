package com.example.chess.model

data class Move(
    val from: Position,
    val to: Position,
    val piece: ChessPiece,
    val captured: ChessPiece? = null,
    val isCastling: Boolean = false,
    val isPromotion: Boolean = false,
    val isEnPassant: Boolean = false
)

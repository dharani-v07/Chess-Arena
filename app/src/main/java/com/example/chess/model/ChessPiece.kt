package com.example.chess.model

enum class PieceType(val value: Int) {
    PAWN(10),
    KNIGHT(30),
    BISHOP(30),
    ROOK(50),
    QUEEN(90),
    KING(1000)
}

enum class PieceColor {
    WHITE, BLACK;

    fun opponent(): PieceColor = if (this == WHITE) BLACK else WHITE
}

data class ChessPiece(
    val type: PieceType,
    val color: PieceColor,
    val hasMoved: Boolean = false
) {
    val unicode: String
        get() = when (type) {
            PieceType.KING -> "♚"
            PieceType.QUEEN -> "♛"
            PieceType.ROOK -> "♜"
            PieceType.BISHOP -> "♝"
            PieceType.KNIGHT -> "♞"
            PieceType.PAWN -> "♟"
        }
}

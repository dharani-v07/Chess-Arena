package com.example.chess.model

data class Position(val row: Int, val col: Int) {
    fun isValid(): Boolean {
        return row in 0..7 && col in 0..7
    }
}

package com.example.chess.model

data class BoardState(
    val board: Array<Array<ChessPiece?>> = getInitialBoard(),
    val turn: PieceColor = PieceColor.WHITE,
    val lastMove: Move? = null,
    val checkStatus: ChessStatus = ChessStatus.NONE
) {
    enum class ChessStatus {
        NONE, CHECK, CHECKMATE, STALEMATE
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardState

        if (!board.contentDeepEquals(other.board)) return false
        if (turn != other.turn) return false
        if (lastMove != other.lastMove) return false
        if (checkStatus != other.checkStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + turn.hashCode()
        result = 31 * result + (lastMove?.hashCode() ?: 0)
        result = 31 * result + checkStatus.hashCode()
        return result
    }

    companion object {
        fun getInitialBoard(): Array<Array<ChessPiece?>> {
            val grid = Array(8) { arrayOfNulls<ChessPiece>(8) }

            // Set up pawns
            for (col in 0..7) {
                grid[1][col] = ChessPiece(PieceType.PAWN, PieceColor.BLACK)
                grid[6][col] = ChessPiece(PieceType.PAWN, PieceColor.WHITE)
            }

            // Set up major Black pieces
            val blackBackRow = arrayOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )
            for (col in 0..7) {
                grid[0][col] = ChessPiece(blackBackRow[col], PieceColor.BLACK)
            }

            // Set up major White pieces
            val whiteBackRow = arrayOf(
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
            )
            for (col in 0..7) {
                grid[7][col] = ChessPiece(whiteBackRow[col], PieceColor.WHITE)
            }

            return grid
        }
    }

    // Get a piece at position
    fun getPiece(pos: Position): ChessPiece? {
        return if (pos.isValid()) board[pos.row][pos.col] else null
    }

    // Get all legal moves for current turn player
    fun getLegalMoves(): List<Move> {
        val moves = mutableListOf<Move>()
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = board[r][c]
                if (piece != null && piece.color == turn) {
                    val pos = Position(r, c)
                    val pseudoMoves = getPseudoLegalMoves(pos)
                    for (m in pseudoMoves) {
                        if (isMoveSafe(m)) {
                            moves.add(m)
                        }
                    }
                }
            }
        }
        return moves
    }

    // Get pseudo-legal moves for a piece at position (moves ignoring check checks)
    fun getPseudoLegalMoves(pos: Position, includeCastling: Boolean = true): List<Move> {
        val piece = getPiece(pos) ?: return emptyList()
        val moves = mutableListOf<Move>()

        when (piece.type) {
            PieceType.PAWN -> getPawnMoves(pos, piece, moves)
            PieceType.KNIGHT -> getKnightMoves(pos, piece, moves)
            PieceType.BISHOP -> getSlidingMoves(pos, piece, moves, directionsDiagonal)
            PieceType.ROOK -> getSlidingMoves(pos, piece, moves, directionsOrthogonal)
            PieceType.QUEEN -> getSlidingMoves(pos, piece, moves, directionsDiagonal + directionsOrthogonal)
            PieceType.KING -> getKingMoves(pos, piece, moves, includeCastling)
        }

        return moves
    }

    private val directionsDiagonal = listOf(
        Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
    )
    private val directionsOrthogonal = listOf(
        Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)
    )

    private fun getPawnMoves(pos: Position, piece: ChessPiece, moves: MutableList<Move>) {
        val direction = if (piece.color == PieceColor.WHITE) -1 else 1
        val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
        val promoRow = if (piece.color == PieceColor.WHITE) 0 else 7

        // 1 step forward
        val nextPos = Position(pos.row + direction, pos.col)
        if (nextPos.isValid() && getPiece(nextPos) == null) {
            val isPromo = nextPos.row == promoRow
            moves.add(Move(pos, nextPos, piece, isPromotion = isPromo))

            // 2 steps forward
            val doublePos = Position(pos.row + 2 * direction, pos.col)
            if (pos.row == startRow && doublePos.isValid() && getPiece(doublePos) == null) {
                moves.add(Move(pos, doublePos, piece))
            }
        }

        // Standard captures
        val captureCols = listOf(pos.col - 1, pos.col + 1)
        for (c in captureCols) {
            val diagPos = Position(pos.row + direction, c)
            if (diagPos.isValid()) {
                val diagPiece = getPiece(diagPos)
                if (diagPiece != null && diagPiece.color != piece.color) {
                    val isPromo = diagPos.row == promoRow
                    moves.add(Move(pos, diagPos, piece, captured = diagPiece, isPromotion = isPromo))
                }
                
                // En Passant
                if (diagPiece == null && lastMove != null && lastMove.piece.type == PieceType.PAWN) {
                    val lastFrom = lastMove.from
                    val lastTo = lastMove.to
                    if (lastTo.col == c && lastTo.row == pos.row && Math.abs(lastFrom.row - lastTo.row) == 2) {
                        moves.add(Move(pos, diagPos, piece, captured = getPiece(lastTo), isEnPassant = true))
                    }
                }
            }
        }
    }

    private fun getKnightMoves(pos: Position, piece: ChessPiece, moves: MutableList<Move>) {
        val knightOffsets = listOf(
            Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
            Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
        )
        for (offset in knightOffsets) {
            val target = Position(pos.row + offset.first, pos.col + offset.second)
            if (target.isValid()) {
                val targetPiece = getPiece(target)
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(Move(pos, target, piece, captured = targetPiece))
                }
            }
        }
    }

    private fun getSlidingMoves(pos: Position, piece: ChessPiece, moves: MutableList<Move>, directions: List<Pair<Int, Int>>) {
        for (dir in directions) {
            var currRow = pos.row + dir.first
            var currCol = pos.col + dir.second
            while (true) {
                val target = Position(currRow, currCol)
                if (!target.isValid()) break
                val targetPiece = getPiece(target)
                if (targetPiece == null) {
                    moves.add(Move(pos, target, piece))
                } else {
                    if (targetPiece.color != piece.color) {
                        moves.add(Move(pos, target, piece, captured = targetPiece))
                    }
                    break // Blocked by piece
                }
                currRow += dir.first
                currCol += dir.second
            }
        }
    }

    private fun getKingMoves(pos: Position, piece: ChessPiece, moves: MutableList<Move>, includeCastling: Boolean = true) {
        val kingOffsets = listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1,          0 to 1,
            1 to -1,  1 to 0,  1 to 1
        )
        for (offset in kingOffsets) {
            val target = Position(pos.row + offset.first, pos.col + offset.second)
            if (target.isValid()) {
                val targetPiece = getPiece(target)
                if (targetPiece == null || targetPiece.color != piece.color) {
                    moves.add(Move(pos, target, piece, captured = targetPiece))
                }
            }
        }

        // Castling (King has not moved, King not in check)
        if (includeCastling && !piece.hasMoved && !isKingInCheck(piece.color)) {
            val kingRow = if (piece.color == PieceColor.WHITE) 7 else 0

            // King side castle (Rook at col 7)
            val rookRight = getPiece(Position(kingRow, 7))
            if (rookRight != null && rookRight.type == PieceType.ROOK && !rookRight.hasMoved) {
                // Check vacant blocks
                if (getPiece(Position(kingRow, 5)) == null && getPiece(Position(kingRow, 6)) == null) {
                    // Check squares not under attack
                    if (isSquareSafe(Position(kingRow, 5), piece.color) && isSquareSafe(Position(kingRow, 6), piece.color)) {
                        moves.add(Move(pos, Position(kingRow, 6), piece, isCastling = true))
                    }
                }
            }

            // Queen side castle (Rook at col 0)
            val rookLeft = getPiece(Position(kingRow, 0))
            if (rookLeft != null && rookLeft.type == PieceType.ROOK && !rookLeft.hasMoved) {
                // Check vacant blocks
                if (getPiece(Position(kingRow, 1)) == null && getPiece(Position(kingRow, 2)) == null && getPiece(Position(kingRow, 3)) == null) {
                    // Check squares not under attack (usually only the squares king traverses need security check, i.e., col 2 and 3)
                    if (isSquareSafe(Position(kingRow, 2), piece.color) && isSquareSafe(Position(kingRow, 3), piece.color)) {
                        moves.add(Move(pos, Position(kingRow, 2), piece, isCastling = true))
                    }
                }
            }
        }
    }

    // Check if moving piece leaves king in check
    private fun isMoveSafe(move: Move): Boolean {
        val nextBoardState = simulateMove(move)
        return !nextBoardState.isKingInCheck(move.piece.color)
    }

    // Can opponent piece attack target position
    private fun isSquareSafe(pos: Position, defenderColor: PieceColor): Boolean {
        val attackerColor = defenderColor.opponent()
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null && p.color == attackerColor) {
                    val pseudoMoves = getPseudoLegalMoves(Position(r, c), includeCastling = false)
                    if (pseudoMoves.any { it.to == pos }) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun isKingInCheck(color: PieceColor): Boolean {
        // Find king
        var kingPos: Position? = null
        for (r in 0..7) {
            for (c in 0..7) {
                val p = board[r][c]
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    kingPos = Position(r, c)
                    break
                }
            }
            if (kingPos != null) break
        }
        if (kingPos == null) return false
        return !isSquareSafe(kingPos, color)
    }

    // Deep copy and apply move
    fun simulateMove(move: Move): BoardState {
        // Deep copy board array
        val nextBoard = Array(8) { r ->
            Array(8) { c ->
                board[r][c]
            }
        }

        val piece = move.piece
        val from = move.from
        val to = move.to

        // Remove piece from origin
        nextBoard[from.row][from.col] = null

        // Apply promotion or normal piece placement
        val finalPiece = if (move.isPromotion) {
            ChessPiece(PieceType.QUEEN, piece.color, hasMoved = true)
        } else {
            piece.copy(hasMoved = true)
        }
        nextBoard[to.row][to.col] = finalPiece

        // Handle Castling
        if (move.isCastling) {
            val kingRow = from.row
            if (to.col == 6) { // King side
                val rook = nextBoard[kingRow][7]
                nextBoard[kingRow][7] = null
                nextBoard[kingRow][5] = rook?.copy(hasMoved = true)
            } else if (to.col == 2) { // Queen side
                val rook = nextBoard[kingRow][0]
                nextBoard[kingRow][0] = null
                nextBoard[kingRow][3] = rook?.copy(hasMoved = true)
            }
        }

        // Handle En Passant
        if (move.isEnPassant) {
            val direction = if (piece.color == PieceColor.WHITE) -1 else 1
            nextBoard[to.row - direction][to.col] = null
        }

        return BoardState(
            board = nextBoard,
            turn = turn.opponent(),
            lastMove = move
        )
    }

    // Process a confirmed move and evaluate checks/game overs
    fun makeMove(move: Move): BoardState {
        val nextState = simulateMove(move)
        val nextTurnOpponent = nextState.turn // this is the current player in nextState

        val hasLegalMoves = nextState.getLegalMoves().isNotEmpty()
        val isCheck = nextState.isKingInCheck(nextTurnOpponent)

        val finalStatus = when {
            isCheck && !hasLegalMoves -> ChessStatus.CHECKMATE
            !isCheck && !hasLegalMoves -> ChessStatus.STALEMATE
            isCheck -> ChessStatus.CHECK
            else -> ChessStatus.NONE
        }

        return nextState.copy(checkStatus = finalStatus)
    }
}

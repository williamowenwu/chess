package chess;

import java.util.ArrayList;
import java.util.Arrays;

import chess.ReturnPiece.PieceFile;
import chess.ReturnPiece.PieceType;
import chess.ReturnPlay.Message;

class ReturnPiece {
	static enum PieceType {WP, WR, WN, WB, WQ, WK, 
		            BP, BR, BN, BB, BK, BQ};
	static enum PieceFile {a, b, c, d, e, f, g, h};
	
	PieceType pieceType;
	PieceFile pieceFile;
	int pieceRank;  // 1..8
	public String toString() {
		return ""+pieceFile+pieceRank+":"+pieceType;
	}
	public boolean equals(Object other) {
		if (other == null || !(other instanceof ReturnPiece)) {
			return false;
		}
		ReturnPiece otherPiece = (ReturnPiece)other;
		return pieceType == otherPiece.pieceType &&
				pieceFile == otherPiece.pieceFile &&
				pieceRank == otherPiece.pieceRank;
	}
}

class ReturnPlay {
	enum Message {ILLEGAL_MOVE, DRAW, 
				  RESIGN_BLACK_WINS, RESIGN_WHITE_WINS, 
				  CHECK, CHECKMATE_BLACK_WINS,	CHECKMATE_WHITE_WINS, 
				  STALEMATE};
	
	ArrayList<ReturnPiece> piecesOnBoard;
	Message message;
}

public class Chess {
	enum Player { white, black }
	private static ReturnPlay board = new ReturnPlay();
    private static Player turnPlayer = Player.white;
    private static ArrayList<String> moveHistory = new ArrayList<>();
	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	public static ReturnPlay play(String move) {
		
		String[] inputs = sanitizeInput(move);
    if (inputs.length < 2 || !isValidMoveFormat(inputs)) {
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // Convert input strings to board coordinates
    int[] startCoords = convertToCoords(inputs[0]);
    int[] endCoords = convertToCoords(inputs[1]);
	
	System.out.println(Arrays.toString(startCoords));
	System.out.println(Arrays.toString(endCoords)); // * get rid after
    if (startCoords == null || endCoords == null) {
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // Find the piece and validate the move
    ChessPiece piece = pieceAt(startCoords[0], startCoords[1]);
	System.out.println(piece.pieceType);
    if (piece == null || !piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1])) {
		System.out.println(piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1]));
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // Move the piece
    movePiece(startCoords[0], startCoords[1], endCoords[0], endCoords[1]);

    // Check for special conditions (e.g., check, checkmate) - Placeholder for implementation
    // updateGameStatus();

    togglePlayerTurn(); // Toggle the player's turn

    return board; // Return the current state of the game
}
	
	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		board.piecesOnBoard = new ArrayList<>();
        // Initialize pieces for both white and black
        initializePiecesForBothSides();
        board.message = null; // Reset any previous game state message
        turnPlayer = Player.white; // White starts
        moveHistory.clear(); // Clear move history
}

private static void initializePiecesForBothSides() {
	// Setup pieces for White
	setupPieces(Player.white, 1, 2);
	// Setup pieces for Black
	setupPieces(Player.black, 8, 7);
}

private static void setupPieces(Player player, int backRow, int pawnRow) {
    // Initialize specific pieces for the back row
    ChessPiece[] backRowPieces = {
        new Rook(PieceType.WR, PieceFile.values()[0], backRow, player.toString()),
        new Knight(PieceType.WN, PieceFile.values()[1], backRow, player.toString()),
        new Bishop(PieceType.WB, PieceFile.values()[2], backRow, player.toString()),
        new Queen(PieceType.WQ, PieceFile.values()[3], backRow, player.toString()),
        new King(PieceType.WK, PieceFile.values()[4], backRow, player.toString()),
        new Bishop(PieceType.WB, PieceFile.values()[5], backRow, player.toString()),
        new Knight(PieceType.WN, PieceFile.values()[6], backRow, player.toString()),
        new Rook(PieceType.WR, PieceFile.values()[7], backRow, player.toString())
    };

    // Adjust the piece types for black pieces
    if (player == Player.black) {
        for (int i = 0; i < backRowPieces.length; i++) {
            PieceType blackType = PieceType.valueOf("B" + backRowPieces[i].pieceType.name().substring(1));
            backRowPieces[i].pieceType = blackType;
        }
    }

    // Add back row pieces to the board
    for (ChessPiece piece : backRowPieces) {
        board.piecesOnBoard.add(piece);
    }

    // Initialize pawns
    PieceType pawnType = (player == Player.white) ? PieceType.WP : PieceType.BP;
    for (PieceFile file : PieceFile.values()) {
        board.piecesOnBoard.add(new Pawn(pawnType, file, pawnRow, player.toString()));
    }
}


private static String[] sanitizeInput(String move) {
	return move.trim().toLowerCase().replaceAll("\\s+", " ").split(" ");
}

private static boolean isValidMoveFormat(String[] inputs) {
	// Additional logic to validate move format goes here
	return true; // Placeholder return value
}

public static ChessPiece pieceAt(int x, int y) {
	for (ReturnPiece piece : board.piecesOnBoard) {
		System.out.println("Checking piece: " + piece + " at [" + piece.pieceFile.ordinal() + ", " + piece.pieceRank + "]");
		if (piece.pieceFile.ordinal() == x && piece.pieceRank == y) {
			return (ChessPiece) piece; // Cast as necessary; ensure your piecesOnBoard are ChessPiece instances
		}
	}
	return null; // No piece at the given location
}

private static int[] convertToCoords(String position) {
    if (position.length() != 2) return null;

    int x = position.charAt(0) - 'a'; // Convert 'a'-'h' to 0-7
    int y = Character.getNumericValue(position.charAt(1)); 

    if (x < 0 || x >= 8 || y < 0 || y >= 8) return null; // Check bounds
    return new int[]{x, y};
}

private static void movePiece(int startX, int startY, int endX, int endY) {
    // Find and remove the target piece if it exists (capture)
    ChessPiece targetPiece = pieceAt(endX, endY);
	System.out.println("target piece" + targetPiece);
    if (targetPiece != null) {
        board.piecesOnBoard.remove(targetPiece);
    }

    // Find the moving piece and update its position
    ChessPiece movingPiece = pieceAt(startX, startY);
    if (movingPiece != null) {
        board.piecesOnBoard.remove(movingPiece); // Remove from old position
        movingPiece.pieceFile = PieceFile.values()[endX]; // Update file
        movingPiece.pieceRank = endY; // Update rank (convert back to 1-8 scale)
        board.piecesOnBoard.add(movingPiece); // Add back with new position
    }
}



private static void togglePlayerTurn() {
	turnPlayer = (turnPlayer == Player.white) ? Player.black : Player.white;
}
}
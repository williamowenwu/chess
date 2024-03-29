package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack; 

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
	// private static boolean offeredDraw = false;
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
    boolean canPerformThyEnPassant = false;
    boolean canPerformThyCastling = false;
	String[] inputs = sanitizeInput(move);

    // Check for resignation
    if (inputs[0].equals("resign")) {
        board.message = (turnPlayer == Player.white) ? Message.RESIGN_BLACK_WINS : Message.RESIGN_WHITE_WINS;
        return board;
    }

    // Check for draw offer
    if (inputs.length >= 3 && inputs[inputs.length - 1].equals("draw?")) {
        board.message = Message.DRAW;
        return board;
    }

    // Validate input format
    if (inputs.length < 2 || !isValidMoveFormat(inputs)) {
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    int[] startCoords = convertToCoords(inputs[0]);
    int[] endCoords = convertToCoords(inputs[1]);

    System.out.println(Arrays.toString(startCoords));
    System.out.println(Arrays.toString(endCoords));
	// System.out.println(inputs.length);
	// System.out.println(inputs[inputs.length-1]);

    if (startCoords == null || endCoords == null) {
		System.out.println("got hit");
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    ChessPiece piece = pieceAt(startCoords[0], startCoords[1]);
    // If not a piece to move, not a valid move for the piece, or not ur turn
    if (piece == null) {
        System.out.println("No piece at the starting position.");
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }
	// Check if it's the player's turn
    if (!piece.color.equalsIgnoreCase(turnPlayer.toString())) {
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // Handle special pawn moves
    if (piece instanceof Pawn) {
        if (isValidEnPassant(startCoords[0], startCoords[1], endCoords[0], endCoords[1])){
            // en Passant
            canPerformThyEnPassant = true;
            String lastMove = moveHistory.get(moveHistory.size() - 1);
            String[] parts = lastMove.split("-");
        
            // int[] lastMoveStartCoords = convertToCoords(parts[0]);
            int[] lastMoveEndCoords = convertToCoords(parts[1]);
            ChessPiece youJustGotEnPassantedPawn = pieceAt(lastMoveEndCoords[0], lastMoveEndCoords[1]);
            board.piecesOnBoard.remove(youJustGotEnPassantedPawn);
        }
        else if(((Pawn) piece).isPromotionMove(endCoords[1])){
            String promotionType = inputs.length == 3 ? inputs[2].toUpperCase() : "Q"; // Default to Queen
            promotePawn((Pawn) piece, promotionType, endCoords);
        }
        // castling
    } else if (piece instanceof King){
        int kingDistance = endCoords[0] - startCoords[0];

        if (Math.abs(kingDistance) == 2){
            boolean isKingside = kingDistance > 0;

            int rookFile = isKingside ? 7 : 0; // 1 - 7 for a - h
            int rookRank = piece.getColor().equals("white") ? 1 : 8;

            Rook rook = (Rook) pieceAt(rookFile, rookRank);

            if (rook != null && canCastle((King) piece, rook)) {
                // Update the positions of the king and rook to their castling positions
                // int kingEndFile = startCoords[0] + (isKingside ? 2 : -2);
                canPerformThyCastling = true;
                int rookEndFile = startCoords[0] + (isKingside ? 1 : -1); // Position rook next to the king
                
                // Move king and rook to their new positions
                // movePiece(startCoords[0], startCoords[1], kingEndFile, startCoords[1]); // Move king
                movePiece(rookFile, rookRank, rookEndFile, rookRank); // Move rook
            }

        }
        // if (canCastle())
    }

    // Check for valid move with exception to en Passant and castling
    if (!piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1]) && !canPerformThyEnPassant && !canPerformThyCastling){
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    movePiece(startCoords[0], startCoords[1], endCoords[0], endCoords[1]);

	System.out.println("Turn: " + turnPlayer);
    
	System.out.println(Arrays.toString(startCoords));
	System.out.println(Arrays.toString(endCoords)); // * get rid after
    

	System.out.println(piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1]));
	System.out.println(piece.pieceType);


    //testing purposes
    if(isInCheckMate(turnPlayer)){
        System.out.println(turnPlayer.name() + " Is Checkmating the Opponent!");
    }else{
        System.out.println(" No check or checkmate detected");
    }
        
    togglePlayerTurn(); 
	board.message = null;
    String moveNotation = inputs[0] + "-" + inputs[1];
    moveHistory.add(moveNotation);

    return board;
}
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		board.piecesOnBoard = new ArrayList<>();
        setupPieces(Player.white, 1, 2);
		setupPieces(Player.black, 8, 7);
        board.message = null; // Reset any previous game state message
        turnPlayer = Player.white; // White starts firstc1
        moveHistory.clear();
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

// are the move input valid?
private static boolean isValidMoveFormat(String[] inputs) {
	for (String input : inputs) {

        // Check if the first character is a file between 'a' and 'h'.
        char file = input.charAt(0);
        if (file < 'a' || file > 'h') {
            return false;
        }

        // Check if the second character is a rank between '1' and '8'.
        char rank = input.charAt(1);
        if (rank < '1' || rank > '8') {
            return false;
        }
    }
	return true; // Placeholder return value
}

// Gets the piece at a location if there is one, else null
public static ChessPiece pieceAt(int x, int y) {
	for (ReturnPiece piece : board.piecesOnBoard) {
		// System.out.println("Checking piece: " + piece + " at [" + piece.pieceFile.ordinal() + ", " + piece.pieceRank + "]");
		if (piece.pieceFile.ordinal() == x && piece.pieceRank == y) {
			return (ChessPiece) piece; // Cast as necessary; ensure your piecesOnBoard are ChessPiece instances
		}
	}
	return null; // No piece at the given location
}

// converts chess format to coords --> a1 = [0,0]
private static int[] convertToCoords(String position) {
    if (position.length() != 2) return null;

    int x = position.charAt(0) - 'a';// 1- 8
    int y = Character.getNumericValue(position.charAt(1)) -1; 

    if (x < 0 || x >= 8 || y < 0 || y >= 8) return null; // Check bounds
    return new int[]{x, y + 1};
}

// Moves a piece from (x,y) to endx, endy (also has capturing)
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

//checks to see if after the current move, the enemy player is in check. Required method to handle checkmate.
public static boolean isInCheck(ReturnPlay board, Player playerColor) {
    King targetedKing = (King)getKing((turnPlayer == Player.white) ? Player.black : Player.white);
    String kingPosition = targetedKing.pieceFile.name() + targetedKing.pieceRank;

    //now for every friendly piece we're gonna see if they can move onto the targetedKing
    for(ReturnPiece boardPiece: board.piecesOnBoard){
        int[] tempCoords = convertToCoords(((ChessPiece)boardPiece).pieceFile.toString() + ((ChessPiece)boardPiece).pieceRank);
        int[] tempKingCoords = convertToCoords(kingPosition);

        if(((ChessPiece)boardPiece).color.equals(turnPlayer.toString())){
            
            if(((ChessPiece)boardPiece).isValidMove(tempCoords[0], tempCoords[1], tempKingCoords[0], tempKingCoords[1])){
                board.message = Message.CHECK;
                return true;
            }
        }
    }

    return false;
}

//Checks to see if the enemy player is in checkmate. Utilizes isInCheck and a stack to track moves.
private static boolean isInCheckMate(Player playerColor) {
    Stack<int[]> turnLogs = new Stack<int[]>();

    String color = (playerColor == Player.white) ? "black" : "white";

    //all checkmates are also checks 
    if(isInCheck(board, playerColor)){ 
        for(int i = 0; i < board.piecesOnBoard.size(); i++){
            ReturnPiece currPiece = board.piecesOnBoard.get(i);
            if(((ChessPiece)currPiece).getColor().equals(color));
            
            int[] yPosArray = convertToCoords(((ChessPiece)currPiece).pieceFile.toString() + ((ChessPiece)currPiece).pieceRank);
            int yPos = yPosArray[0];
            int xPos = currPiece.pieceRank;

            //finds all legal moves possible
            for(int rank = 0; rank < 8; rank++){
                for(int file = 0; file < 8; file++){
                    if(((ChessPiece)currPiece).isValidMove(xPos, yPos, rank, file) == true){
                        int[] coords = {xPos, yPos, rank, file};
                        turnLogs.push(coords);
                    }
                }
            }

            while(!turnLogs.isEmpty()){
                int[] tempMove = turnLogs.pop();

                ReturnPlay tempBoard = new ReturnPlay();
                tempBoard.piecesOnBoard = board.piecesOnBoard;

                moveTempPiece(tempBoard, tempMove[0], tempMove[1], tempMove[2], tempMove[3]);

                // Check if the move resolves check
                if (isInCheck(tempBoard, playerColor)){ 
                    return true;
                }
            }
        }
    }
    return false;
}

private static void moveTempPiece(ReturnPlay tempBoard, int startX, int startY, int endX, int endY) {
    // Find and remove the target piece if it exists (capture)
    ChessPiece targetPiece = pieceAt(endX, endY);
	System.out.println("target piece" + targetPiece);
    if (targetPiece != null) {
        tempBoard.piecesOnBoard.remove(targetPiece);
    }

    // Find the moving piece and update its position
    ChessPiece movingPiece = pieceAt(startX, startY);
    if (movingPiece != null) {
        tempBoard.piecesOnBoard.remove(movingPiece); // Remove from old position
        movingPiece.pieceFile = PieceFile.values()[endX]; // Update file
        movingPiece.pieceRank = endY; // Update rank (convert back to 1-8 scale)
        tempBoard.piecesOnBoard.add(movingPiece); // Add back with new position
    }
}

//Gonna use this to find the King, will be needed for Check and Checkmate. Should make the code more readable.
private static ReturnPiece getKing(Player playerColor){
    String color = playerColor.toString();

    for(ReturnPiece boardPiece: board.piecesOnBoard){
        //checks if color of piece matches the toString() of targeted player's color
        if(boardPiece instanceof King && ((King)boardPiece).getColor().equals(color)){
            return boardPiece; //King found.
        }
    }

    return null; //no King found
}

private static void togglePlayerTurn() {
	turnPlayer = (turnPlayer == Player.white) ? Player.black : Player.white;
}

    // Helper method to promote a pawn
    private static void promotePawn(Pawn pawn, String promotionType, int[] endCoords) {
        ChessPiece promotedPiece;
        switch (promotionType) {
            case "N":
                promotedPiece = new Knight(PieceType.valueOf(pawn.color.substring(0, 1).toUpperCase() + "N"), pawn.pieceFile, endCoords[1], pawn.color);
                break;
            case "B":
                promotedPiece = new Bishop(PieceType.valueOf(pawn.color.substring(0, 1).toUpperCase() + "B"), pawn.pieceFile, endCoords[1], pawn.color);
                break;
            case "R":
                promotedPiece = new Rook(PieceType.valueOf(pawn.color.substring(0, 1).toUpperCase() + "R"), pawn.pieceFile, endCoords[1], pawn.color);
                break;
            case "Q":
            default:
                promotedPiece = new Queen(PieceType.valueOf(pawn.color.substring(0, 1).toUpperCase() + "Q"), pawn.pieceFile, endCoords[1], pawn.color);
                break;
        }
        // Remove the pawn from the board and add the new promoted piece
        board.piecesOnBoard.remove(pawn);
        board.piecesOnBoard.add(promotedPiece);
    }

    private static boolean isValidEnPassant(int startX, int startY, int endX, int endY) {
        if (moveHistory.isEmpty()) return false; // Cannot perform en passant without a move history
    
        String lastMove = moveHistory.get(moveHistory.size() - 1);
        String[] parts = lastMove.split("-");
        if (parts.length < 2) return false; // Invalid move format
    
        // Use convertToCoords to get board coordinates
        int[] lastMoveStartCoords = convertToCoords(parts[0]);
        int[] lastMoveEndCoords = convertToCoords(parts[1]);
    
        // Check if the last move was a two-square pawn advance
        boolean isTwoSquareAdvance = Math.abs(lastMoveStartCoords[1] - lastMoveEndCoords[1]) == 2 &&
                                      lastMoveStartCoords[0] == lastMoveEndCoords[0] &&
                                      Math.abs(startY - endY) == 1 && // Current move is a diagonal capture
                                      Math.abs(startX - endX) == 1 && // Must move diagonally for en passant
                                      (endX == lastMoveEndCoords[0]); // Must be capturing towards the advanced pawn's position

        return isTwoSquareAdvance;
    }

    private static boolean canCastle(King king, Rook rook) {
        // Check if both the king and rook have not moved
        if (!king.hasFirstMove() || !rook.hasFirstMove()) {
            return false;
        }

        // Check if the path between the king and rook is clear
        int startFile = Math.min(king.getPieceFile().ordinal(), rook.getPieceFile().ordinal());
        int endFile = Math.max(king.getPieceFile().ordinal(), rook.getPieceFile().ordinal());
        for (int file = startFile + 1; file < endFile; file++) {
            if (Chess.pieceAt(file, king.getPieceRank()) != null) {
                return false; // Path is not clear
            }
        }
        Player p = king.getColor().equals("white") ? Player.white : Player.black;
        // Check if the king is in check
        if (isInCheck(board, p)) {
            return false;
        }
        
        // Ensure the squares the king passes through are not under attack
        int kingStartFile = king.getPieceFile().ordinal();
        int kingEndFile = kingStartFile + (rook.getPieceFile().ordinal() > kingStartFile ? 2 : -2);
        for (int file = kingStartFile; file != kingEndFile; file += Integer.signum(kingEndFile - kingStartFile)) {
            if (isSquareUnderAttack(file, king.getPieceRank(), king.getColor())) {
                return false;
            }
        }
        
        return true; // All conditions met for castling
    }

    private static boolean isSquareUnderAttack(int file, int rank, String attackerColor) {
        // Loop through all pieces on the board
        for (ReturnPiece piece : board.piecesOnBoard) {
            // First, check if the piece can be downcasted to ChessPiece
            if (piece instanceof ChessPiece) {
                ChessPiece chessPiece = (ChessPiece) piece; // Safe downcast

                // Check if the piece is of the attacking color
                if (chessPiece.getColor().equals(attackerColor)) {
                    // Determine if the piece can legally move to the target square
                    if (chessPiece.isValidMove(chessPiece.getPieceFile().ordinal(), chessPiece.getPieceRank(), file, rank)) {
                        // If so, and if it's a Knight or Pawn, return true
                        if (chessPiece instanceof Knight || chessPiece instanceof Pawn) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
        
    }
    
    
    
    
}
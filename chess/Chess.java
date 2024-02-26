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
		
	String[] inputs = sanitizeInput(move);

	if(inputs[0].equals("resign")){
		board.message = (turnPlayer==Player.white) ? Message.RESIGN_BLACK_WINS : Message.RESIGN_WHITE_WINS;
		return board;
	}


	if(inputs.length >= 3 && inputs[inputs.length-1].equals("draw?")){
		board.message = Message.DRAW;
		return board;
	}
	System.out.println(inputs.length);
	System.out.println(inputs[inputs.length-1]);

	if (inputs.length < 2 || !isValidMoveFormat(inputs)) {
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }
	System.out.println("Turn: " + turnPlayer);
    int[] startCoords = convertToCoords(inputs[0]);
    int[] endCoords = convertToCoords(inputs[1]);
	
	System.out.println(Arrays.toString(startCoords));
	System.out.println(Arrays.toString(endCoords)); // * get rid after
    if (startCoords == null || endCoords == null) {
		System.out.println("got hit");
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // If not a piece to move, not a valid move for the piece, or not ur turn
    ChessPiece piece = pieceAt(startCoords[0], startCoords[1]);
    if (piece == null) {
        System.out.println("No piece at the starting position.");
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }
    
    // Check if it's the correct player's turn
    if (!piece.color.equalsIgnoreCase(turnPlayer.toString())) {
        System.out.println("It's not " + piece.color + "'s turn.");
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

    // Check if the move is valid for the piece
    if (!piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1])) {
        System.out.println("Invalid move for " + piece.getClass().getSimpleName());
        board.message = Message.ILLEGAL_MOVE;
        return board;
    }

	System.out.println(piece.isValidMove(startCoords[0], startCoords[1], endCoords[0], endCoords[1]));
	System.out.println(piece.pieceType);

    // castle method + en passant
    movePiece(startCoords[0], startCoords[1], endCoords[0], endCoords[1]);

    //promotion

    //testing purposes
    if(isInCheck(turnPlayer)){
        System.out.println(turnPlayer.name() + " Is Checking the Opponent!");
    }else{
        System.out.println(" No check or checkmate detected");
    }
        
    togglePlayerTurn(); 
	board.message = null;
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
public static boolean isInCheck(Player playerColor) {
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

    //establishes stack of all valid moves
    String color = (playerColor == Player.white) ? "black" : "white";
    if(isInCheck(playerColor)){
        for(int i = 0; i < board.piecesOnBoard.size(); i++){
            ReturnPiece currPiece = board.piecesOnBoard.get(i);
            if(((ChessPiece)currPiece).getColor().equals(color));
            
            int[] yPosArray = convertToCoords(((ChessPiece)currPiece).pieceFile.toString() + ((ChessPiece)currPiece).pieceRank);
            int yPos = yPosArray[0];
            int xPos = currPiece.pieceRank;

            for(int rank = 1; rank < 9; rank++){
                for(int file = 1; file < 9; file++){
                    if(((ChessPiece)currPiece).isValidMove(xPos, yPos, rank, file) == true){
                        int[] coords = {xPos, yPos, rank, file};
                        turnLogs.push(coords);
                    }
                }
            }

            while(!turnLogs.isEmpty()){
                int[] tempMove = turnLogs.pop();
                ReturnPiece tempCapture = pieceAt(tempMove[0], tempMove[1]);

                if(tempCapture != null){
                    board.piecesOnBoard.remove(tempCapture);
                }

                movePiece(tempMove[0], tempMove[1], tempMove[2], tempMove[3]);

                // Check if the move resolves check
                if (!isInCheck(playerColor)) {
                    if (tempCapture != null) {
                        board.piecesOnBoard.add(tempCapture);
                    }
                    return false;
                }

                movePiece(tempMove[3], tempMove[2], tempMove[1], tempMove[0]);
                if(tempCapture != null){
                    board.piecesOnBoard.add(tempCapture);
                }
            }
        }   
    }

    return true;
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
}


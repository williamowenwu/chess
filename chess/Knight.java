package chess;

public class Knight extends ChessPiece {
    public Knight(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color); // Correctly call the parent constructor
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        // Calculate differences in position
        int fileDiff = Math.abs(endX - startX);
        int rankDiff = Math.abs(endY - startY);

        // Check for L-shaped move validity: 2 squares in one direction and 1 square in the perpendicular direction
        if (!((fileDiff == 2 && rankDiff == 1) || (fileDiff == 1 && rankDiff == 2))) {
            return false; // Not a valid Knight move
        }

        // Since the Knight can jump over pieces, we only need to check the destination square
        ChessPiece targetPiece = Chess.pieceAt(endX, endY);

        // Move is valid if the target square is empty or contains an opponent's piece
        if (targetPiece == null) {
            return true; // The square is empty, move is valid
        } else if (!this.color.equalsIgnoreCase(targetPiece.getColor())) {
            return true; // The square contains an opponent's piece, capture is valid
        }

        return false; // The square contains a piece of the same color
    }
}

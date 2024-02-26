package chess;

public class Pawn extends ChessPiece {
    private boolean firstMove = true;

    public Pawn(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        int direction = this.pieceType == PieceType.WP ? 1 : -1; // White pawns move up (increasing rank), black pawns move down (decreasing rank)
        int startRank = startY; // Assuming startY is the rank, adjust based on your coordinate system
        int endRank = endY; // Adjust based on your coordinate system
        int fileDifference = Math.abs(startX - endX); // Assuming startX and endX represent file positions
        int rankDifference = endRank - startRank;

        // Normal move
        if (fileDifference == 0 && rankDifference == direction) {
            if (Chess.pieceAt(endX, endY) == null) { // The end position must be empty
                firstMove = false; // The pawn has moved, so it's no longer its first move
                return true; // Move is valid
            }
        }


        // First move allows two squares forward
        if (firstMove && fileDifference == 0 && rankDifference == 2 * direction) {
            // If both the square directly ahead and the destination square are open (empty)
            if (Chess.pieceAt(startX, startY + direction) == null && Chess.pieceAt(endX, endY) == null) {
                firstMove = false;
                return true;
            }
        }

        // Capture move
        if (fileDifference == 1 && rankDifference == direction) {
            ChessPiece targetPiece = Chess.pieceAt(endX, endY);
            if (targetPiece != null && !this.color.equals(targetPiece.color)) { // There must be an enemy piece to capture
                return true; // Capture move is valid
            }
        }

        return false;
    }
}

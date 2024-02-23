package chess;

public class King extends ChessPiece {
    private boolean firstMove = true;

    public King(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        // King can move one square in any direction
        int fileDifference = Math.abs(endX - startX);
        int rankDifference = Math.abs(endY - startY);

        // Check for standard king movement (1 square in any direction)
    if ((fileDifference <= 1 && rankDifference <= 1) && !(fileDifference == 0 && rankDifference == 0)) {
        ChessPiece targetPiece = Chess.pieceAt(endX, endY);
        
        // If the square is empty (openSquare logic replaced by checking if pieceAt returns null)
        if (targetPiece == null) {
            return true;
        }
        // If moving to an occupied square, check if it's an opponent's piece
        else if (!this.color.equals(targetPiece.getColor())) {
            return true;
        }
    }
        //* */ Handle castling logic here (omitted for brevity)

        return false;
    }

    public boolean hasFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean hasMoved) {
        this.firstMove = hasMoved;
    }
}

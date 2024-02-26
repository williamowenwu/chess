package chess;

public class Bishop extends ChessPiece {
    public Bishop(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color); // Assuming the ChessPiece constructor is adjusted for String color
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        int fileDifference = Math.abs(endX - startX);
        int rankDifference = Math.abs(endY - startY);

        // Bishop moves diagonally, so the absolute difference between x and y must be the same
        if (fileDifference != rankDifference) {
            return false; // Not a valid diagonal move
        }

        int stepX = Integer.compare(endX, startX);
        int stepY = Integer.compare(endY, startY);

        // Check each square along the path for obstacles
        int currentX = startX + stepX;
        int currentY = startY + stepY;
        while (currentX != endX || currentY != endY) {
            ChessPiece piece = Chess.pieceAt(currentX, currentY);
            if (piece != null) {
                return false; // Obstacle in the path
            }
            currentX += stepX;
            currentY += stepY;
        }

        // Check the destination square
        ChessPiece targetPiece = Chess.pieceAt(endX, endY);
        if (targetPiece == null) {
            return true; // The destination is open
        } else if (!this.color.equalsIgnoreCase(targetPiece.getColor())) {
            return true; // Can capture an opponent's piece
        }

        return false; // Destination occupied by a piece of the same color
    }
}

package chess;

public class Queen extends ChessPiece {
    public Queen(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color); // Correctly initialize the superclass
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        int fileDiff = Math.abs(endX - startX);
        int rankDiff = Math.abs(endY - startY);

        // Check if the move is diagonal, horizontal, or vertical
        boolean isDiagonal = fileDiff == rankDiff;
        boolean isStraight = startX == endX || startY == endY;
        if (!isDiagonal && !isStraight) {
            return false; // Not a valid Queen move
        }

        // Determine the direction of the move
        int stepX = Integer.compare(endX, startX);
        int stepY = Integer.compare(endY, startY);

        // Check each square along the path for obstacles
        int currentX = startX + stepX;
        int currentY = startY + stepY;
        while (currentX != endX || currentY != endY) {
            if (Chess.pieceAt(currentX, currentY) != null) {
                return false; // Obstacle in the path
            }
            currentX += (currentX != endX) ? stepX : 0;
            currentY += (currentY != endY) ? stepY : 0;
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

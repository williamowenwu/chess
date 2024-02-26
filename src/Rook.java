package chess;

public class Rook extends ChessPiece {
    private boolean firstMove = true;

    public Rook(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        super(pieceType, pieceFile, pieceRank, color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY) {
        // Rook moves horizontally or vertically, so one coordinate must remain the same
        if (startX != endX && startY != endY) {
            return false; // Not a valid rook move
        }

        int stepX = Integer.compare(endX, startX);
        int stepY = Integer.compare(endY, startY);

        // Check each square along the path for obstacles
        int currentX = startX + stepX;
        int currentY = startY + stepY;
        while (currentX != endX || currentY != endY) {
            if (Chess.pieceAt(currentX, currentY) != null) {
                return false; // Obstacle in the path
            }
            currentX += stepX;
            currentY += stepY;
        }

        // Check the destination square
        ChessPiece targetPiece = Chess.pieceAt(endX, endY);
        if (targetPiece == null) {
            firstMove = false; // The path is clear, and it's a valid move
            return true;
        } else if (this.color != targetPiece.getColor()) {
            firstMove = false; // Capture is possible
            return true;
        }

        return false; // Destination occupied by a piece of the same color
    }

    public boolean hasFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean hasMoved) {
        this.firstMove = hasMoved;
    }
}

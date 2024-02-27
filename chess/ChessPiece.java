package chess;

public abstract class ChessPiece extends ReturnPiece {
    protected String color;
    

    public ChessPiece(PieceType pieceType, PieceFile pieceFile, int pieceRank, String color) {
        this.pieceType = pieceType;
        this.pieceFile = pieceFile;
        this.pieceRank = pieceRank;
        this.color = color; // Initialize the color attribute in ChessPiece
    }

    public String getColor() {
        return this.color;
    }

    public PieceFile getPieceFile(){
        return this.pieceFile;
    }

    public int getPieceRank(){
        return this.pieceRank;
    }

    abstract boolean isValidMove(int startX, int startY, int endX, int endY);
}

/**
 * @author  Matheson Longton
 *
 * This extension of the Tile class is only suitable for a standard rectangular grid.  The toFullString method includes its coordinates, which are useful for user output.
 */
public class SquareTile extends Tile {
    public int x, y;
    
    /**
     * @param mined true if the Tile has a mine.
     * @param isStart true if the Tile is initially revealed.
     * @param numAdjacent The number of adjacent Tiles with mines.
     * @param xCoord The horizontal coordinate of thie Tile on the Board.
     * @param yCoord The vertical coordinate of thie Tile on the Board.
     */
    public SquareTile(boolean mined, boolean isStart, int numAdjacent, int xCoord, int yCoord) {
        super(mined, isStart, numAdjacent);
        x = xCoord;
        y = yCoord;
    }
    
    public String toFullString() {
        return "("+x+","+y+":"+toString()+")";
    }
}
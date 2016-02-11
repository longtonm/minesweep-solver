import java.lang.*;
import java.util.*;

/**
 * @author  Matheson Longton
 *
 * A class to represent a generic tile of a Minesweeper board.  Each tile either has a mine on it or must present the number of adjacent tiles with mines once it is revealed.  This class does not assume any organizational structure such as a grid, and could be used to play Minesweeper on a more general graph.
 */
public class Tile {
    private boolean hasMine, revealed;
    public boolean flagged;
    private int adjacent;
    /**
     * A list of all Tiles which are adjacent to this one.  Since Tiles must be created one at a time, this can be filled after the Tile is created.
     */
    public ArrayList<Tile> neighbours;
    /**
     * An estimate of the maximum number of neighbours each Tile has.  Used as the initial size of the list of neighbours, this does not prevent it from growing as needed.
     */
    public static int numNeighbours = 8;
    
    /**
     * Create a Tile.
     *
     * @param   mined true if the Tile has a mine.
     * @param   isStart true if the Tile is initially revealed.
     * @param   numAdjacent The number of neighbours with mines, or -1 if this tile is also mined.
     */
    public Tile(boolean mined, boolean isStart, int numAdjacent) {
        this.hasMine = mined;
        this.revealed = isStart;
        this.flagged = false;
        this.adjacent = numAdjacent;
        this.neighbours = new ArrayList<Tile>(numNeighbours);
    }
    
    /**
     * @return true if this Tile is revealed and will display the number of mined neighbours.
     */
    public boolean isRevealed() {
        return revealed;
    }
    
    /**
     * Get the number of adjacent Tiles with mines.
     *
     * @return  The number of adjacent tiles with mines, or -1 if this Tile is not revealed.
     */
    public int adjacentMines() {
        if (revealed) {
            return adjacent;
        }
        else {
            return -1;
        }
    }
    
    /**
     * Indicate that this Tile has a mine.
     *
     * @return  true if the Tile was not already flagged.
     */
    public boolean flag() {
        if (flagged) {
            return false;
        }
        else {
            return flagged = true;
        }
    }
    
    /**
     * Reveal this Tile.
     *
     * @throws BoomException if this Tile has a mine.
     * @return The number of adjacent Tiles with mines.
     */
    public int reveal() throws BoomException {
        revealed = true;
        if (!hasMine) {
            return adjacent;
        }
        else {
            throw new BoomException(); //if BoomException becomes more than just a shell, throw new BoomException(this);
        }
    }
    
    /**
     * Get a list of this Tile's neighbours which are not revealed.
     *
     * @return An ArrayList containing all Tiles which are adjacent to this but not themselves revealed.
     */
    public Collection<Tile> hiddenNeighbours() {
        ArrayList<Tile> hN = new ArrayList<Tile>(neighbours);
        for (int i = 0; i < hN.size(); ) {
            if (hN.get(i).isRevealed()) {
                hN.remove(i);
            }
            else {
                i++;
            }
        }
        return hN;
    }
    
    public String toString() {
        if (revealed && adjacent < 0) return "X";
        else if (revealed && !flagged) return (adjacent==0?" ":""+adjacent);
        else if (revealed && flagged) return "?";
        else if (flagged) return "*";
        else return ".";
    }
    
    public static String toString(Tile t) {
        if (t == null) {
            return "#";
        }
        return t.toString();
    }
}
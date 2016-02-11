import java.lang.*;
import java.util.*;

/**
 * @author  Matheson Longton
 *
 * This class describes a hypothetical arrangement of mines in a given set of Tiles.  Many of these are used in listing all possible arrangements of mines in the tiles which we have any information about.
 */
public class MinePossibility {
    private int size;
    private MineState[] mineLocations;
    private ArrayList<Tile> tileFinder;
    
    /**
     * Define a MinePossibility based on a set of Tiles.  All tiles will initially be marked as unknown.
     *
     * @param   edge The collection of Tiles this MinePossibility will describe.
     */
    public MinePossibility(Collection<Tile> edge) {
        size = edge.size();
        tileFinder = new ArrayList<Tile>(edge);
        mineLocations = new MineState[size];
        for (int i = 0; i < size; i++) {
            mineLocations[i] = MineState.U;
        }
    }
    
    /**
     * Get the index of a Tile in this MinePossibility.
     *
     * @param   t The Tile to locate.
     * @return  The index of the Tile.
     */
    public int getIndex(Tile t) {
        return tileFinder.indexOf(t);
    }
    
    /**
     * Get the Tile at a given index.
     *
     * @param   i The index to look up.
     * @return  The Tile at index i.
     */
    public Tile getTile(int i) {
        return tileFinder.get(i);
    }
    
    /**
     * Set the state of a Tile in this possibility.
     *
     * @param   i The index of the Tile to set the state of.
     * @param   x The state to assign.
     * @return  true if the index is valid.
     */
    public boolean set(int i, MineState x) {
        if (i<0 || i>=size) { return false; }
        mineLocations[i] = x;
        return true;
    }
    
    /**
     * Set the state of a Tile in this possibility.
     *
     * @param   t The Tile to set the state of.
     * @param   x The state to assign.
     * @return  true if the index is valid.
     */
    public boolean set(Tile t, MineState x) {
        return set(getIndex(t),x);
    }
    
    /**
     * Get the state of a Tile.
     *
     * @param   i The index of the Tile to look up.
     * @return  The state of the Tile at index i.
     */
    public MineState get(int i) {
        return mineLocations[i];
    }
    
    /**
     * Get the state of a Tile.
     *
     * @param   t The Tile to look up.
     * @return  The state of the Tile t.
     */
    public MineState get(Tile t) {
        return mineLocations[getIndex(t)];
    }
    
    /**
     * @return  The number of Tiles in this MinePossibility.
     */
    public int size() {
        return size;
    }
    
    /**
     * Determine if another MinePossibility is compatible with this one, or if they are contradictory.
     * 
     * @param   other The MinePossibility to compare with this one.
     * @return  If the two MinePossibilities are logically compatible then a new MinePossibility describing the situation where both are true will be returned.  If they are not compatible then null is returned.
     */
    public MinePossibility compatible(MinePossibility other) {
        if (size != other.size) return null;
        MinePossibility combined = new MinePossibility(tileFinder);
        for (int i = 0; i < size; i++) {
            int otherI = other.getIndex(tileFinder.get(i));
            if (otherI < 0) {
                return null;
            }
            if (mineLocations[i] == MineState.U) {
                combined.set(i,other.get(otherI));
            }
            else if (other.get(otherI) == MineState.U) {
                combined.set(i,mineLocations[i]);
            }
            else if (mineLocations[i] == other.get(otherI)) {
                combined.set(i,mineLocations[i]);
            }
            else {
                return null;
            }
        }
        return combined;
    }
    
    public String toString() {
        String str = "[";
        for (int i = 0; i < size; i++) {
            if (tileFinder.get(i) instanceof SquareTile) {
                str = str + ((SquareTile)tileFinder.get(i)).toFullString() + "=";
            }
            else {
                str = str + tileFinder.get(i).toString() + "=";
            }
            str = str + mineLocations[i];
            if (i < size-1) str = str + ", ";
        }
        str = str + "]";
        return str;
    }
}
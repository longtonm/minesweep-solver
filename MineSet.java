import java.lang.*;
import java.util.*;

/**
 * @author  Matheson Longton
 *
 * The MineSet is a key part of the logic in solving Minesweeper puzzles.  Whenever the number of mines in a given set of tiles is known, or even restricted, that information can be described by a MineSet.  A simple example is the MineSet implied by any revealed tile: the collection of tiles which are its neighbours has a known number of mines.  The comparison of overlapping MineSets using the splitWith method is the building block of the algorithm to solve a board.
 */
public class MineSet {
    private Collection<Tile> tiles;
    public SortedSet<Integer> possibleMines;
    
    /**
     * Create a MineSet in which a collection of Tiles may have a varying number of mines.
     *
     * @param   pM A SortedSet containing the possible numbers of mines in these Tiles.
     * @param   t The tiles described by this MineSet.
     */
    public MineSet(SortedSet<Integer> pM, Collection<Tile> t) { //\todo{might be a good idea to clone the tile collection}
        tiles = t;
        possibleMines = new TreeSet<Integer>(pM);
    }
    
    /**
     * Create a MineSet in which a collection of Tiles has a given number of mines.
     *
     * @param   nM The number of mines in these Tiles.
     * @param   t The tiles described by this MineSet.
     */
    public MineSet(int nM, Collection<Tile> t) {
        tiles = t;
        possibleMines = new TreeSet<Integer>();
        possibleMines.add(nM);
    }
    
    /**
     * Create a MineSet in which a collection of Tiles has a given number of mines.
     *
     * @param   nM The number of mines in these Tiles.
     * @param   t An iterator of tiles described by this MineSet.
     */
    public MineSet(int nM, Iterator<Tile> t) {
        tiles = new LinkedList<Tile>();
        while (t.hasNext()) {
            tiles.add(t.next());
        }
        possibleMines = new TreeSet<Integer>();
        possibleMines.add(nM);
    }
    
    /**
     * Create an empty MineSet, describing no Tiles.
     */
    public MineSet() {
        tiles = new LinkedList<Tile>();
        possibleMines = new TreeSet<Integer>();
    }
    
    /**
     * Remove any known Tiles from this MineSet, and adjust the number of mines accordingly.
     *
     * @return true if this MineSet was changed, false otherwise.
     */
    public boolean removeKnown() {
        boolean changed = false;
        int flagsRemoved = 0;
        Iterator<Tile> tileIt = tiles.iterator();
        while (tileIt.hasNext()) {
            Tile t = tileIt.next();
            if (t.isRevealed()) {
                changed = true;
                tileIt.remove();
            }
            else if (t.flagged) {
                changed = true;
                tileIt.remove();
                flagsRemoved++;
            }
        }
        SortedSet<Integer> newPM = new TreeSet<Integer>();
        for (int n : possibleMines) {
            if (n >= flagsRemoved) {
                newPM.add(n-flagsRemoved);
            }
        }
        possibleMines = newPM;
        return changed;
    }
    
    /**
     * Determine if two MineSets describe the same Tiles.
     *
     * @param   other The MineSet to compare.
     * @return  true if the two MineSets have the same Tiles, false otherwise.
     */
    public boolean equalTiles(MineSet other) {
        if (tiles.size() != other.tiles.size()) {
            return false;
        } else if (!other.tiles.containsAll(tiles) || !tiles.containsAll(other.tiles)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return An iterator for the Tiles in this MineSet.
     */
    public Iterator<Tile> tileIterator() {
        return tiles.iterator();
    }
    
    /**
     * @return The number of Tiles in this MineSet.
     */
    public int tileCount() {
        return tiles.size();
    }
    
    /**
     * @param t The tile to search this MineSet for.
     * @return true if t is in this MineSet, false otherwise.
     */
    public boolean hasTile(Tile t) {
        return tiles.contains(t);
    }
    
    /**
     * Compare this MineSet to another and find MineSets describing the intersection and difference of Tiles.
     *
     * By splitting the Tiles of two MineSets into three groups, the intersection and differences, we can create three new MineSets describing the number of Tiles in each of those groups.
     *
     * @param   other The MineSet to compare.
     * @return  An array of the three MineSets.  The intersection is always the first item, then this-other, and finally other-this.
     */
    public MineSet[] splitWith(MineSet other) {
        MineSet aMINUSb = new MineSet(), bMINUSa = new MineSet(), aANDb = new MineSet();
        for (Tile t : tiles) {
            if (other.hasTile(t)) {
                aANDb.tiles.add(t);
            }
            else {
                aMINUSb.tiles.add(t);
            }
        }
        for (Tile t : other.tiles) {
            if (!this.hasTile(t)) {
                bMINUSa.tiles.add(t);
            }
        }
        for (int nA : this.possibleMines) {
            for (int nB : other.possibleMines) {
                for (int nAB = Math.max(nA-aMINUSb.tileCount(),nB-bMINUSa.tileCount()); nAB <= aANDb.tileCount(); nAB++) {
                    if (nAB >=0 && nA >= nAB && nB >= nAB) {
                        aANDb.possibleMines.add(nAB);
                        aMINUSb.possibleMines.add(nA-nAB);
                        bMINUSa.possibleMines.add(nB-nAB);
                    }
                }
            }
        }
        MineSet[] result = {aANDb, aMINUSb, bMINUSa};
        return result;
    }
    
    public String toString() {
        String str = "Tiles [";
        for (Tile t : tiles) {
            if (t instanceof SquareTile) {
                str = str + ((SquareTile)t).toFullString() + ", ";
            }
            else {
                str = str + t.toString() + ", ";
            }
        }
        if (tiles.size() > 0) str = str + "\b\b";
        str = str + "] with (";
        for (Integer x : possibleMines) {
            str = str + x + ", ";
        }
        str = str + "\b\b) mines";
        return str;
    }
}
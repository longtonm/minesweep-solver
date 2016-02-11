import java.lang.*;
import java.util.*;

/**
 * @author  Matheson Longton
 *
 * The MineSet class often appears in collections, so this MineSetList class is a linked list of MineSets.  It includes the additional useful capability to update an existing MineSet instead of adding MineSets with identical tiles.
 */
public class MineSetList extends LinkedList<MineSet> {
    
    /**
     * Find the MineSet in this collection with the same tiles as a given MineSet.
     *
     * @param   x The MineSet to search for.
     * @return  A MineSet in this list with the same Tiles as x, or null if no such MineSet exists.
     */
    public MineSet containsTiles(MineSet x) {
        for (MineSet y : this) {
            if (x.equalTiles(y)) {
                return y;
            }
        }
        return null;
    }
    
    /**
     * Add a MineSet to this list, or update an existing MineSet with the same Tiles.
     *
     * @param   x The MineSet to add.
     * @return  If x is added, it is returned.  If an existing MineSet with the same Tiles is updated based on x then that MineSet is returned.  If an existing MineSet was already identical to x, then null is returned.
     */
    public MineSet addOrUpdate(MineSet x) {
        MineSet toUpdate = this.containsTiles(x);
        if (toUpdate == null) {
            this.add(x);
            return x;
        }
        else {
            boolean possibilityRemoved = false;
            for (Iterator<Integer> it = toUpdate.possibleMines.iterator(); it.hasNext();) {
                int n = it.next();
                if (!x.possibleMines.contains(n)) {
                    it.remove();
                    possibilityRemoved = true;
                }
            }
            return (possibilityRemoved ? toUpdate : null);
        }
    }
    
    /**
     * Perform addOrUpdate with each of a number of MineSets.
     *
     * @param   x The MineSet collection to be added.
     * @return  true if this collection was changed, false otherwise.
     */
    public boolean addOrUpdateAll(Collection<MineSet> x) { //this could change to return a LinkedList<MineSet> containing the results of the addOrUpdates
        boolean changed = false;
        if (x == null) return false;
        for (MineSet y : x) {
            changed = (this.addOrUpdate(y) != null) || changed;
            this.addOrUpdate(y);
        }
        return changed;
    }
    
    /**
     * Create an empty MineSetList.
     */
    public MineSetList() {
        super();
    }
    
    /**
     * Create a MineSetList containing a single MineSet.
     *
     * @param init A MineSet to place in the new list.
     */
    public MineSetList(MineSet init) {
        super();
        this.add(init);
    }
}
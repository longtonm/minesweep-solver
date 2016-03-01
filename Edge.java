import java.lang.*;
import java.util.*;

/**
 * @author  Matheson Longton
 *
 * An Edge object provides a way to store and study all of the information which is known about a Minesweeper board.  It collects MineSets describing its knowledge and allows them to be processed in order to find new information and hopefully find tiles which become known.
 */
public class Edge {
    private HashMap<Tile, MineSetList> setsForTile; //would be bad if changes to a Tile altered the hashCode
    private MineSetList unfinished;
    protected Board ownerBoard;
    
    /**
     * Create an Edge based on a single revealed Tile
     *
     * Creates an Edge object containing only the information provided by a single revealed tile: what neighbours it has and how many of them have mines.
     *
     * @param   revealed The tile to base this Edge on.
     * @param   estSize The expected number of Tiles that this Edge will contain, used as the size of an internal hash.
     * @param   b The Board containing the tiles this Edge will study.
     */
    public Edge(Tile revealed, int estSize, Board b) {
        ownerBoard = b;
        if (!revealed.isRevealed()) {
            setsForTile = new HashMap<Tile,MineSetList>();
            setsForTile.put(revealed,null);
            unfinished = new MineSetList();
            return;
        }
        Collection<Tile> beginning = revealed.hiddenNeighbours();
        MineSet initMS = new MineSet(revealed.adjacentMines(), beginning);
        initMS.removeKnown();
        unfinished = new MineSetList(initMS);
        setsForTile = new HashMap<Tile,MineSetList>(estSize);
        for (Tile tl : beginning) {
            setsForTile.put(tl,new MineSetList(initMS));
        }
    }
    
    /**
     * Get a collection of Tiles which this Edge has some information about.  Once Tiles are known, they are removed from this.
     *
     * @return  A Set containing all of the Tiles in this Edge.
     */
    public Set<Tile> edgeTiles() {
        return setsForTile.keySet();
    }
    
    /**
     * Merge all of the information of another Edge into this one.
     *
     * @param   other The Edge with information to be added.
     */
    public void add(Edge other) {
        for (Tile x : other.setsForTile.keySet()) {
            if (!setsForTile.containsKey(x)) {
                setsForTile.put(x,other.setsForTile.get(x));
            }
            else {
                for (MineSet y : other.setsForTile.get(x)) {
                    if (setsForTile.get(x).addOrUpdate(y) != null) {
                        unfinished.addOrUpdate(y);
                    }
                }
            }
        }
        unfinished.addOrUpdateAll(other.unfinished);
    }
    
    /**
     * @return true if it might be possible to learn something new about the Tiles in this Edge, or false if the Edge is exhausted.
     */
    public boolean hasWork() {
        return !unfinished.isEmpty();
    }
    
    /**
     * Perform comparisons of MineSets until there is nothing left for this Edge to do.
     */
    public void compareAll() {
        while (hasWork()) {
            compareOne();
        }
    }
    
    /**
     * Compare one MineSet in this Edge to its neighbours.
     *
     * @return  true if a Tile was flagged or revealed, false otherwise.
     */
    public boolean compareOne() {
        boolean doPrint = false;
        if (unfinished.isEmpty()) {
            return false;
        }
        MineSet x = unfinished.poll();
        if (doPrint) { //first debugging section
            System.out.println("MineSet being compared: "+x);
            MineSetList meaningful = new MineSetList();
            Iterator<Tile> tileIt = x.tileIterator();
            while (tileIt.hasNext()) {
                Tile a = tileIt.next();
                meaningful.addOrUpdateAll(setsForTile.get(a));
            }
            System.out.println("Adjacent sets:");
            for (MineSet y : meaningful) {
                System.out.println(y);
            }
            System.out.println();
        }
        boolean temp = compareOne(x);
        if (doPrint) { //second debugging section
            for (Tile a : setsForTile.keySet()) {
                for (MineSet b : setsForTile.get(a)) {
                    Iterator<Tile> tileIt = b.tileIterator();
                    while (tileIt.hasNext()) {
                        Tile c = tileIt.next();
                        if (c.isRevealed() || c.flagged) {
                            System.out.println("****** There is a known tile in the edge: "+((c instanceof SquareTile)?((SquareTile)c).toFullString():c)+" ******");
                        }
                    }
                }
            }
        } //end debgging section
        return temp;
    }
    
    /**
     * Compare a given MineSet to any neighbours it may have in this Edge.
     *
     * @param   x The MineSet to compare to MineSets known by this Edge.  Normally x should also be a part of this Edge.
     * @return  true if a Tile was flagged or revealed, false otherwise.
     */
    public boolean compareOne(MineSet x) {
        boolean madeChange = false; //true only for a changed board, not just any new knowledge
        //step 1: gather all neighbouring information for comparison
        MineSetList xNeighbours = new MineSetList();
        Iterator<Tile> it = x.tileIterator();
        while (it.hasNext()) {
            Tile xTile = it.next();
            xNeighbours.addOrUpdateAll(setsForTile.get(xTile));
        }
        //step 2: compare sets to generate and identify new information
        HashSet<Tile> newClearTile = new HashSet<Tile>(), newFlaggedTile = new HashSet<Tile>(); //could use TreeSet if tiles are comparable
        MineSetList toProcess = new MineSetList();
        for (MineSet y : xNeighbours) {
            MineSet[] xyParts = x.splitWith(y);
            for (int i = 0; i < 3; i++) {
                if (xyParts[i].possibleMines.size() == 1) {
                    if (xyParts[i].possibleMines.first() == 0) {
                        it = xyParts[i].tileIterator();
                        while (it.hasNext()) {
                            Tile toReveal = it.next();
                            //toReveal.reveal();
                            newClearTile.add(toReveal);
                        }
                    }
                    else if (xyParts[i].possibleMines.first() == xyParts[i].tileCount()) {
                        it = xyParts[i].tileIterator();
                        while (it.hasNext()) {
                            Tile toFlag = it.next();
                            newFlaggedTile.add(toFlag);
                        }
                    }
                    else if (!(xyParts[i].equalTiles(x) && xyParts[i].possibleMines.size() == x.possibleMines.size()) && !(xyParts[i].equalTiles(y) && xyParts[i].possibleMines.size() == y.possibleMines.size())) {
                        toProcess.addOrUpdate(xyParts[i]);
                    }
                }
                else { //xyParts[i].possibleMines.size() > 1
                    if (!(xyParts[i].equalTiles(x) && xyParts[i].possibleMines.size() == x.possibleMines.size()) && !(xyParts[i].equalTiles(y) && xyParts[i].possibleMines.size() == y.possibleMines.size())) {
                        toProcess.addOrUpdate(xyParts[i]);
                    }
                }
            }
        }
        //step 3: make updates based on new information and add new sets to the edge
        MineSetList toRemoveKnown = new MineSetList();
        for (Tile finishedTile : newClearTile) {
            finishedTile.reveal();
            toProcess.addOrUpdate(new MineSet(finishedTile.adjacentMines(),finishedTile.hiddenNeighbours()));
            toRemoveKnown.addAll(setsForTile.get(finishedTile));
            setsForTile.remove(finishedTile);
            ownerBoard.knownTile(finishedTile);
            madeChange = true;
        }
        for (Tile finishedTile : newFlaggedTile) {
            finishedTile.flag();
            toRemoveKnown.addAll(setsForTile.get(finishedTile));
            setsForTile.remove(finishedTile);
            ownerBoard.knownTile(finishedTile);
            madeChange = true;
        }
        for (MineSet toClean : toRemoveKnown) {
            toClean.removeKnown();
            unfinished.addOrUpdate(toClean);

        }
        for (MineSet toConsider : toProcess) {
            toConsider.removeKnown();
            addUnfinished(toConsider);
        }
        return madeChange;
    }
    
    /**
     * Add a MineSet to this Edge and include it in the list of unfinished MineSets if it contains any new information.
     *
     * @param   x The MineSet to add.
     * @return  true if this Edge was changed, false otherwise.
     */
    public boolean addUnfinished(MineSet x) {
        MineSet actualUpdated =null;
        x.removeKnown();
        Iterator<Tile> it = x.tileIterator();
        while (it.hasNext()) {
            Tile y = it.next();
            if (!setsForTile.containsKey(y)) {
                setsForTile.put(y,new MineSetList());
            }
            MineSet tempMS = setsForTile.get(y).addOrUpdate(x);
            if (actualUpdated == null) {
                actualUpdated = tempMS;
            }
            else if (tempMS != null && tempMS != actualUpdated) {
                System.err.println("Duplicate MineSets exist for tile "+y);
            }
        }
        if (actualUpdated != null) {
            return (unfinished.addOrUpdate(actualUpdated) != null);
        }
        return false;
    }
    
    /**
     * Used to indicate a Tile which is known (usually because it was guessed) and should be removed from the Edge.
     *
     * @param x The Tile to remove from this Edge.
     */
    public void knownTile(Tile x) {
        if (!setsForTile.containsKey(x)) {
            return;
        }
        for (MineSet y : setsForTile.get(x)) {
            y.removeKnown();
            unfinished.addOrUpdate(y);
        }
        if (x.isRevealed() || x.flagged) {
            setsForTile.remove(x);
        }
    }
    
    /**
     * Find all possible ways that mines could be placed on the Tiles of this Edge.
     *
     * @return A list of MinePossibility objects, each one representing one allowed arrangement of mines on the Tiles of this Edge.
     */
    public Collection<MinePossibility> allPossibleEdges() {
        MineSetList allInfo = new MineSetList();
        ArrayList<Tile> tileOrder = new ArrayList<Tile>(setsForTile.size());
        //putting MineSets in a contiguous order gives faster analysis than a random order
        LinkedList<Tile> keys = new LinkedList<Tile>(setsForTile.keySet()), linkedTiles = new LinkedList<Tile>();
        while (!keys.isEmpty()) {
            Tile t;
            if (linkedTiles.isEmpty()) {
                t = keys.poll();
            }
            else {
                t = linkedTiles.poll();
                keys.remove(t);
            }
            tileOrder.add(t);
            for (MineSet m : setsForTile.get(t)) {
                allInfo.addOrUpdate(m);
                for (Iterator<Tile> tileIt = m.tileIterator(); tileIt.hasNext();) {
                    Tile s = tileIt.next();
                    if (keys.contains(s) && !linkedTiles.contains(s)) {
                        linkedTiles.add(s);
                    }
                }
            }
        }
        LinkedList<MinePossibility> allValid = new LinkedList<MinePossibility>();
        allValid.add(new MinePossibility(tileOrder));
        for (MineSet thisInfo : allInfo) {
            LinkedList<MinePossibility> possForThisSet = new LinkedList<MinePossibility>();
            ArrayList<Tile> tilesThisSet = new ArrayList<Tile>(thisInfo.tileCount());
            Iterator<Tile> tileIt = thisInfo.tileIterator();
            while (tileIt.hasNext()) {
                tilesThisSet.add(tileIt.next());
            }
            for (int minesToAssign : thisInfo.possibleMines) {
                for (ArrayList<Tile> tLst : combinations(minesToAssign,tilesThisSet)) {
                    MinePossibility x = new MinePossibility(tileOrder);
                    for (Tile t : tilesThisSet) {
                        x.set(t,MineState.O);
                    }
                    for (Tile t : tLst) {
                        x.set(t,MineState.X);
                    }
                    possForThisSet.add(x);
                }
            }
            LinkedList<MinePossibility> newAllValid = new LinkedList<MinePossibility>();
            for (MinePossibility x : allValid) {
                for (MinePossibility y : possForThisSet) {
                    MinePossibility z = x.compatible(y);
                    if (z != null) {
                        newAllValid.add(z);
                    }
                }
            }
            allValid = newAllValid;
        }
        return allValid;
    }
    
    /**
     * Find all ways of selecting k objects from a list, independent of order.
     *
     * @param   k The number of items to select.
     * @param   lst The list of objects to choose from.  Duplicates are allowed and will permit repeated items to be selected more than once.
     * @param   <E> The type of elements in lst.
     * @return A LinkedList, each item of which is an ArrayList of items from lst that have been selected.
     */
    public static <E> LinkedList<ArrayList<E>> combinations(int k, ArrayList<E> lst) {
        LinkedList<ArrayList<E>> results = new LinkedList<ArrayList<E>>();
        int n = lst.size();
        if (k < 0 || k > n) return results;
        if (k == 0) {
            results.add(new ArrayList<E>(k));
            return results;
        }
        int[] choices = new int[k];
        int whichChoice = 0, index = 0;
        while (whichChoice >= 0) {
            if (index <= n - (k - whichChoice)) { //this choice can be placed on this item
                choices[whichChoice] = index;
                if (whichChoice < k-1) { //haven't placed the last choice yet
                    index = choices[whichChoice++]+1;
                }
                else { //we have one completed combination
                    ArrayList<E> finishedOne = new ArrayList<E>(k);
                    for (int i = 0; i < k; i++) {
                        finishedOne.add(lst.get(choices[i]));
                    }
                    results.add(finishedOne);
                    index++;
                }
            }
            else { //time to backtrack
                if (whichChoice > 0) index = choices[--whichChoice] + 1;
                else whichChoice--;
            }
        }
        return results;
    }
}
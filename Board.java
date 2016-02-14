import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.*;

//It would be interesting to allow for more abstract graphs, but for now this will be a 2-D rectangular grid
//Only one Edge is used, but more are possible
/**
 * @author  Matheson Longton
 *
 * A Minesweeper board.  This contains a grid of all the tiles, and an Edge object used for studying them.
 * The statGuess method belongs to the board class because it requires knowledge of non-edge tiles.
 */
public abstract class Board {
    protected Edge working;
    protected Collection<Tile> remainingTiles, completedTiles;
    protected int remainingN;
    /**
     * The total number of mines in this Board.
     */
    protected int N;
    
    /**
     * Reveal the tile with the lowest probability of being mined
     *
     * Perform a statistical analysis in order to find the probability that each tile has a mine, and then use that to guess and reveal the tile with the lowest probability.
     *
     * @return  true if a guess was made, false if the Board is already solved.
     * @throws  BoomException if the guess was wrong and a mine was hit.
     */
    public boolean statGuess() {
        Collection<Tile> bulk = new HashSet<Tile>(remainingTiles.size()*4/3);
        Iterator<Tile> tileIt = remainingTiles.iterator();
        while (tileIt.hasNext()) {
            Tile x = tileIt.next();
            if (x.isRevealed()) {
                completedTiles.add(x);
                tileIt.remove();
            }
            else if (x.flagged) {
                remainingN--;
                completedTiles.add(x);
                tileIt.remove();
            }
            else {
                bulk.add(x);
            }
        }
        if (remainingTiles.size() == 0) {
            return false;
        }
        Collection<MinePossibility> edgeMicro;
        if (working != null) {
            for (Tile t : working.edgeTiles()) {
                bulk.remove(t);
            }
            edgeMicro = working.allPossibleEdges();
        }
        else {
            edgeMicro = new LinkedList<MinePossibility>();
        }
        if (edgeMicro.size() == 0) {
            Tile guessTile = pickEqualOdds(bulk);
            alertGuess(guessTile,(1-(float)remainingN/bulk.size()));
            guessTile.reveal();
            Edge freshEdge = new Edge(guessTile,2*(int)Math.sqrt(N));
            if (working == null) {
                working = freshEdge;
            }
            else {
                working.add(freshEdge);
                working.knownTile(guessTile);
            }
            return true;
        }
        BigDecimal totalMicro = new BigDecimal(0);
        int bulkSize = bulk.size();
        HashMap<Tile,BigDecimal> nStatesMined = new HashMap<Tile,BigDecimal>(working.edgeTiles().size()*4/3+2);
        nStatesMined.put(null,new BigDecimal(0));
        for (Tile t : working.edgeTiles()) {
            nStatesMined.put(t,new BigDecimal(0));
        }
        for (MinePossibility x : edgeMicro) {
            int minesInBulk, minesInEdge = 0;
            boolean[] thisEdgeMicro = new boolean[x.size()];
            for (int i = 0; i < thisEdgeMicro.length; i++) {
                if (x.get(i) == MineState.X) {
                    thisEdgeMicro[i] = true;
                    minesInEdge++;
                }
            }
            if (minesInEdge > remainingN) {
                continue;
            }
            else {
                minesInBulk = remainingN - minesInEdge;
            }
            BigDecimal bulkStates = binomCoeff(bulkSize,minesInBulk);
            for (int i = 0; i < thisEdgeMicro.length; i++) {
                if (thisEdgeMicro[i]) {
                    nStatesMined.put(x.getTile(i),nStatesMined.get(x.getTile(i)).add(bulkStates,MathContext.DECIMAL64));
                }
            }
            if (bulkSize > 0) {
                nStatesMined.put(null,nStatesMined.get(null).add(new BigDecimal((double)minesInBulk/bulkSize).multiply(bulkStates,MathContext.DECIMAL64),MathContext.DECIMAL64));
            }
            totalMicro = totalMicro.add(bulkStates,MathContext.DECIMAL64);
        }
        if (bulkSize == 0) nStatesMined.put(null,totalMicro.add(BigDecimal.ONE,MathContext.DECIMAL64));
        LinkedList<Tile> bestOdds = new LinkedList<Tile>();
        bestOdds.add(null);
        for (Tile t : nStatesMined.keySet()) {
            if (nStatesMined.get(t).compareTo(nStatesMined.get(bestOdds.peek())) < 0) {
                bestOdds = new LinkedList<Tile>();
                bestOdds.add(t);
            }
            else if (nStatesMined.get(t).equals(nStatesMined.get(bestOdds.peek())) && t != null) {
                bestOdds.add(t);
            }
        }
        if (bestOdds.remove(null)) {
            bestOdds.addAll(bulk);
        }
        Tile chosenTile = pickEqualOdds(bestOdds);
        alertGuess(chosenTile,(1-nStatesMined.get(nStatesMined.containsKey(chosenTile)?chosenTile:null).divide(totalMicro,MathContext.DECIMAL64).doubleValue()));
        chosenTile.reveal();
        Edge freshEdge = new Edge(chosenTile,2*(int)Math.sqrt(N));
        if (working == null) {
            working = freshEdge;
        }
        else {
            working.add(freshEdge);
            working.knownTile(chosenTile);
        }
        return true;
    }
    
    /**
     * Select one tile to reveal when several have equal probability of being mined.
     *
     * @param   tileChoices A Collection of Tiles to choose from.
     * @return  A Tile more likely to be useful than if we selected randomly.
     */
    public Tile pickEqualOdds(Collection<Tile> tileChoices) {
        if (tileChoices.size() < 1) return null;
        int numEdgeNeighbours = 0, numBulkNeighbours = Integer.MAX_VALUE;
        ArrayList<Tile> bestTiles = new ArrayList<Tile>(1);
        Collection<Tile> edgeTiles;
        if (working == null) edgeTiles = new LinkedList<Tile>();
        else edgeTiles = working.edgeTiles();
        for (Tile t : tileChoices) {
            int eCtr = 0, bCtr = 0;
            for (Tile u : t.neighbours) {
                if (!(u.isRevealed() || u.flagged)) {
                    if (edgeTiles.contains(u)) {
                        eCtr++;
                    }
                    else {
                        bCtr++;
                    }
                }
            }
            if (eCtr>numEdgeNeighbours || (eCtr==numEdgeNeighbours && bCtr<numBulkNeighbours)) {
                numEdgeNeighbours = eCtr; numBulkNeighbours = bCtr;
                bestTiles = new ArrayList<Tile>(tileChoices.size());
                bestTiles.add(t);
            }
            else if (eCtr == numEdgeNeighbours && bCtr == numBulkNeighbours) {
                bestTiles.add(t);
            }
        }
        return bestTiles.get((int)(Math.random()*bestTiles.size()));
    }
    
    /**
     * Used to alert the subclass that a guess is about to be made.  This can be used to display information about which Tile is being revealed.
     *
     * @param   t The Tile which is about to be guessed.
     * @param   odds The probability that the guess is safe.
     */
    public abstract void alertGuess(Tile t, double odds);
    
    //from RosettaCode.org
    public static double binomCoeff(double n, double k) {
        double result = 1;
        for (int i = 1; i < k + 1; i++) {
            result *= (n - i + 1) / i;
        }
        return result;
    }
    
    /**
     * Calculate binomial coefficients, used as n choose k.
     *
     * @param n The n in n choose k.
     * @param k The k in n choose k.  This should be less than n.
     * @return n choose k
     */
    public static BigDecimal binomCoeff(int n, int k) {
        BigDecimal result = BigDecimal.ONE;
        for (int i = 1; i < k + 1; i++) {
            result = result.multiply(new BigDecimal(n-i+1),MathContext.DECIMAL64);
            result = result.divide(new BigDecimal(i),MathContext.DECIMAL64);
        }
        return result;
    }
}
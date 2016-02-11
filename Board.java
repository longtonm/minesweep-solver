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
public class Board {
    protected SquareTile[][] grid;
    protected Edge working;
    protected Collection<Tile> remainingTiles, completedTiles;
    protected int remainingN;
    /**
     * The total number of mines in this Board.
     */
    public final int N;
    /**
     * The dimensions of the Board.
     */
    public final int width, height;
    
    /**
     * Create a simple rectangular Board
     *
     * @param   width The Board's width
     * @param   height The Board's height
     * @param   n The number of mines to be placed
     */
    public Board(int width, int height, int n) {
        N = n;
        remainingN = N;
        this.width = width;
        this.height = height;
        int nMines = 0;
        boolean[][] mineGrid = new boolean[width][height];
        while (nMines < N) {
            int x = (int)(Math.random()*width);
            int y = (int)(Math.random()*height);
            if (!mineGrid[x][y]) {
                mineGrid[x][y] = true;
                nMines++;
            }
        }
        grid = new SquareTile[width][height];
        remainingTiles = new HashSet<Tile>(width*height*4/3+1);
        completedTiles = new LinkedList<Tile>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int nAdjacent;
                if (mineGrid[i][j]) {
                    nAdjacent = -1;
                }
                else {
                    nAdjacent = 0;
                    for (int dx = -1; dx < 2; dx++) {
                        for (int dy = -1; dy < 2; dy++) {
                            if (i+dx>=0 && j+dy>=0 && i+dx<width && j+dy<height && mineGrid[i+dx][j+dy]) {
                                nAdjacent++;
                            }
                        }
                    }
                }
                grid[i][j] = new SquareTile(mineGrid[i][j],false,nAdjacent,i,j);
                remainingTiles.add(grid[i][j]);
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int dx = -1; dx < 2; dx++) {
                    for (int dy = -1; dy < 2; dy++) {
                        if (i+dx>=0 && i+dx<width && j+dy>=0 && j+dy<height) {
                            grid[i][j].neighbours.add(grid[i+dx][j+dy]);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create a Board from a file
     *
     * Create a Board from a text file formatted as if copied from the output of this program
     *
     * @param   filename The path to read from
     * @throws  IOException If the file "filename" cannot be properly opened or read.
     */
    public Board(String filename) throws IOException {
        FileReader rawfile = new FileReader(filename);
        ArrayList<Integer> file = new ArrayList<Integer>();
        int len = 0, cur;
        int i = 0, j = 0;
        while ((cur = rawfile.read()) > -1) {
            file.add(cur);
        }
        rawfile.close();
        for (int chr : file) {
            if ((char)chr == '\n') break;
            len++;
        }
        grid = new SquareTile[len][1];
        int mineCount = 0;
        remainingTiles = new HashSet<Tile>(len*len); //guessing a squareish grid, the hash can expand if needed
        completedTiles = new LinkedList<Tile>();
        if (len == 0) {
            grid = new SquareTile[1][1];
            grid[0][0] = new SquareTile(true,false,0,0,0);
        }
        for (int chr : file) {
            switch ((char)chr) {
                case '\n':
                    i = -1;
                    SquareTile[][] replacementGrid = new SquareTile[len][++j + 1];
                    for (int k = 0; k < len; k++) {
                        for (int l = 0; l < j; l++) {
                            replacementGrid[k][l] = grid[k][l];
                        }
                    }
                    grid = replacementGrid;
                    break;
                case ' ': case '0':
                    grid[i][j] = new SquareTile(false,false,0,i,j);
                    break;
                case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': 
                    grid[i][j] = new SquareTile(false,false,chr-48,i,j);
                    break;
                case '@':
                    grid[i][j] = new SquareTile(false,true,0,i,j);
                    break;
                case '*':
                case 'X':
                    grid[i][j] = new SquareTile(true,false,-1,i,j);
                    mineCount++;
                    break;
            }
            if ((char)chr != '\n') remainingTiles.add(grid[i][j]);
            i++;
        }
        width = len;
        height = grid[0].length;
        N = mineCount;
        remainingN = N;
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                for (int dx = -1; dx < 2; dx++) {
                    for (int dy = -1; dy < 2; dy++) {
                        if (i+dx>=0 && i+dx<width && j+dy>=0 && j+dy<height) {
                            grid[i][j].neighbours.add(grid[i+dx][j+dy]);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reveal the tile with the lowest probability of being mined
     *
     * Perform a statistical analysis in order to find the probability that each tile has a mine, and then use that to guess and reveal the tile with the lowest probability.
     *
     * @param   printProbability Set to true to have the best probability written to stdout.
     * @return  true if a guess was made, false if the Board is already solved.
     * @throws  BoomException if the guess was wrong and a mine was hit.
     */
    public boolean statGuess(boolean printProbability) {
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
            if (printProbability) {
                System.out.println("No working edge, so we guess in the bulk with "+((1-(float)remainingN/bulk.size())*100)+"% chance of success.");
            }
            Tile guessTile = pickEqualOdds(bulk);
            guessTile.reveal();
            Edge freshEdge = new Edge(guessTile,2*Math.max(width,height));
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
        if (printProbability) {
            boolean printed = false;
            for (int i = 0; !printed && i < grid.length; i++) {
                for (int j = 0; !printed && j < grid[i].length; j++) {
                    if (grid[i][j] == chosenTile) {
                        System.out.println("Guessing "+i+","+j+" with "+((1-nStatesMined.get(nStatesMined.containsKey(chosenTile)?chosenTile:null).divide(totalMicro,MathContext.DECIMAL64).doubleValue())*100)+"% chance of success.");
                        printed = true;
                    }
                }
            }
        }
        chosenTile.reveal();
        Edge freshEdge = new Edge(chosenTile,2*Math.max(width,height));
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
     * Write this Board to stdout
     */
    public void printBoard() { //\todo{maybe update this to use Tile.toString()}
        Tile t;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                t = grid[i][j];
                if (t == null) System.out.print("#");
                else if (t.isRevealed() && t.adjacentMines() < 0) System.out.print("X");
                else if (t.isRevealed() && !t.flagged) System.out.print((t.adjacentMines()==0?" ":""+t.adjacentMines()));
                else if (t.isRevealed() && t.flagged) System.out.print("X");
                else if (t.flagged) System.out.print("*");
                else System.out.print(".");
            }
            System.out.println();
        }
        System.out.println();
        System.out.flush();
    }
    
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
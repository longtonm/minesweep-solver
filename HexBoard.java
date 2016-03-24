import java.lang.*;
import java.util.*;
import java.math.*;

/**
 * @author  Matheson Longton
 *
 * A Minesweeper board with a hexagonal grid.  Each tile is adjacent to its six neighbours.
 */
public class HexBoard extends TwoDBoard implements TextBoard {
    
    /**
     * Create a board with a hexagonal grid
     *
     * @param   width The Board's width
     * @param   height The Board's height
     * @param   n The number of mines to be placed
     * @param   probInfo Set to true to print guessing information to stdout.
     */
    public HexBoard(int width, int height, int n, boolean probInfo) {
        super(width, height, n);
        this.printProbability = probInfo;
        int nMines = 0;
        ArrayList<ArrayList<Boolean>> mineGrid = setRandomMines(N);
        grid = initGrid(width, height, Tile.class);
        remainingTiles = new HashSet<Tile>(width*height*4/3+1);
        completedTiles = new LinkedList<Tile>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int nAdjacent;
                if (getGrid(i,j,mineGrid)) {
                    nAdjacent = -1;
                }
                else {
                    nAdjacent = getAdjacent(i,j,mineGrid);
                }
                SquareTile newTile = new SquareTile(getGrid(i,j,mineGrid),false,nAdjacent,i,j);
                setGrid(i,j,newTile);
                remainingTiles.add(newTile);
            }
        }
        linkNeighbours();
    }
    
    /**
     * Create a Board from a text file.
     *
     * At present, the spaces that are inserted in the output to give a hexagonal grid should be removed.  This may change in future versions.
     *
     * @param   file An array containing the contents of the file as produced by TextBoard.readBoard(fileName).
     * @param   probInfo Set to true to print guessing information to stdout.
     */
    public HexBoard(char[][] file, boolean probInfo) {
        super(file[0].length, file.length, TextBoard.mineCount(file));
        this.printProbability = probInfo;
        grid = initGrid(width, height, Tile.class);
        int mineCount = 0;
        remainingTiles = new HashSet<Tile>((int)(TextBoard.tileCount(file)*4/3+1));
        completedTiles = new LinkedList<Tile>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                SquareTile newTile = TextBoard.makeTile(file[j][i],i,j);
                if (newTile != null) {
                    setGrid(i,j,newTile);
                    remainingTiles.add(newTile);
                }
            }
        }
        linkNeighbours();
    }
    
    /**
     * Get a list of all grid points that are neighbours of a given point.
     *
     * @param i The x coordinate of the site to find neighbours of.
     * @param i The y coordinate of the site to find neighbours of.
     * @return A list of GridCoordinate objects representing the neighbours.
     */
    public LinkedList<GridCoordinate> neighbourCoordinates(int i, int j) {
        LinkedList<GridCoordinate> result = new LinkedList<GridCoordinate>();
        for (int y = j-1; y < j+2; y++) {
            if (y < 0 || y >= height) continue;
            for (int x = (y==j ? i-1 : i-1+(j%2)); x < (y==j ? i+2 : i+1+(j%2)); x++) {
                if (x < 0 || x >= width) continue;
                if (x != i || y != j) {
                    result.add(new GridCoordinate(x,y));
                }
            }
        }
        return result;
    }
    
    /**
     * Write this Board to stdout
     */
    public void printBoard() {
        Tile t;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                t = getGrid(i,j);
                if (j % 2 == 0) {
                    System.out.print(Tile.toString(t)+" ");
                }
                else {
                    System.out.print(" "+Tile.toString(t));
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.flush();
    }
}
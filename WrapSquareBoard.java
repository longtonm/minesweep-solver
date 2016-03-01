import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * @author  Matheson Longton
 *
 * A Minesweeper board.  This contains a grid of all the tiles, and an Edge object used for studying them.
 * The WrapSquareBoard represents a board with a standard square grid (as in StandardBoard) that wraps around so that there are no sides.
 */
public class WrapSquareBoard extends TwoDBoard {
        
    /**
     * Create a wrapped rectangular board
     *
     * @param   width The Board's width
     * @param   height The Board's height
     * @param   n The number of mines to be placed
     * @param   probInfo Set to true to print guessing information to stdout.
     */
    public WrapSquareBoard(int width, int height, int n, boolean probInfo) {
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
     * Get the item at (x%width, y%height) in a grid.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @param matrix The grid to look in.
     * @param <E> THe type stored by this grid.
     * @return The item at (x,y).
     */
    public <E> E getGrid(int x, int y, List<? extends List<E>> matrix) {
        return matrix.get((x+width)%width).get((y+height)%height);
    }
    /**
     * Set the item at (x%width, y%height) in a grid.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @param item The item to replace the value at (x,y).
     * @param matrix The grid to place the item in.
     * @param <E> THe type stored by this grid.
     * @return The item previously at (x%width, y%height), as specified by the List interface.
     */
    public <E> E setGrid(int x, int y, E item, List<? extends List<E>> matrix) {
        return matrix.get((x+width)%width).set((y+height)%height,item);
    }
    
    /**
     * Write this Board to stdout
     */
    public void printBoard() {
        Tile t;
        for (int j = 0; j <= height; j++) {
            for (int i = 0; i <= width; i++) {
                t = getGrid(i,j);
                System.out.print(Tile.toString(t));
            }
            System.out.println();
        }
        System.out.println();
        System.out.flush();
    }
}
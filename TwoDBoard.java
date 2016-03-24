import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * @author  Matheson Longton
 *
 * A two-dimensional Minesweeper board.
 * This contains a grid of all the tiles, which is assumed to be some form of two-index lattice.  The actual adjacency of lattice points should be overridden by the subclass.  If not all lattice points are needed (for example a non-rectangular board with a standard grid) then gridGet and gridSet may throw an IndexOutOfBoundsException even when a point exists in the underlying array.
 */
public abstract class TwoDBoard extends Board {
    protected ArrayList<ArrayList<Tile>> grid;
    /**
     * The dimensions of the Board.
     */
    public final int width, height;
    /**
     * Indicates whether information about guesses should be written to stdout.
     */
    public boolean printProbability;
    
    /**
     * Partial constructor for subclasses to set basic properties.
     *
     * @param width The board's width
     * @param height The board's height
     * @param n The number of mines
     */
    protected TwoDBoard(int width, int height, int n) {
        super(n);
        this.width = width;
        this.height = height;
    }
    
    /**
     * Randomly distributes mines on a boolean grid matching the underlying lattice of this board.
     * While the returned grid is width*height, mines will only be placed where the getGrid and setGrid methods do not throw an IndexOutOfBoundsException.
     *
     * @param nMines The number of mines to be randomly distributed.
     * @return A two-dimensional ArrayList with nMines elements set to true.
     */
    public ArrayList<ArrayList<Boolean>> setRandomMines(int nMines) {
        ArrayList<ArrayList<Boolean>> mGrid = initGrid(width, height, Boolean.class, Boolean.FALSE);
        int n = 0;
        while (n < nMines) {
            int x = (int)(Math.random()*width);
            int y = (int)(Math.random()*height);
            try { //Not very efficient, but quite general.  Subclasses could override it for performance.
                if (!getGrid(x,y,mGrid)) {
                    setGrid(x,y,Boolean.TRUE,mGrid);
                    n++;
                }
            }
            catch (IndexOutOfBoundsException e) {}
        }
        return mGrid;
    }
    
    /**
     * Find the number of adjacent mines to any site in a boolean grid.
     *
     * @param x The x coordinate of the site to check neighbours of.
     * @param y The y coordinate of the site to check neighbours of.
     * @param matrix The boolean grid to search for mines.
     * @return The number of neighbours of (x,y) with mines.
     */
    public int getAdjacent(int x, int y, List<? extends List<Boolean>> matrix) {
        int n = 0;
        for (GridCoordinate z : neighbourCoordinates(x,y)) {
            if (getGrid(z.x,z.y,matrix)) {
                n++;
            }
        }
        return n;
    }
    
    /**
     * Connect each Tile in this board's underlying grid to all of its neighbours.
     * This adds each Tile's neighbours to the neighbours List in the Tile.
     */
    public void linkNeighbours() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (GridCoordinate z : neighbourCoordinates(i,j)) {
                    if (getGrid(i,j) != null && getGrid(z.x,z.y) != null) {
                        getGrid(i,j).neighbours.add(getGrid(z.x,z.y));
                    }
                }
            }
        }
    }

    /**
     * Get a list of all grid points that are neighbours of a given point.
     *
     * @param i The x coordinate of the site to find neighbours of.
     * @param j The y coordinate of the site to find neighbours of.
     * @return A list of GridCoordinate objects representing the neighbours.
     */
    public abstract LinkedList<GridCoordinate> neighbourCoordinates(int i, int j);
    
    /**
     * Get the tile at a specified location in this board's underlying grid.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @return The tile at (x,y).
     * @throws IndexOutOfBoundsException if the coordinates (x,y) are not an allowed location for this board.
     */
    public Tile getGrid(int x, int y) {
        return getGrid(x, y, grid);
    }
    /**
     * Get the item at a specified location in a grid with the same structure as that underlying this board.
     * Subclasses should override this method with any restrictions on allowed indices, and those restrictions will take effect for the board's grid as well.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @param matrix The grid to look in.
     * @param <E> THe type stored by this grid.
     * @return The item at (x,y).
     * @throws IndexOutOfBoundsException if the coordinates (x,y) are not an allowed location for this board.
     */
    public <E> E getGrid(int x, int y, List<? extends List<E>> matrix) { //or List<? extends List<E>> ?
        return matrix.get(x).get(y);
    }
    /**
     * Set the tile at a specified location in this board's underlying grid.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @param item The tile to replace the value at (x,y).
     * @return The tile previously at (x,y), as specified by the List interface.
     * @throws IndexOutOfBoundsException if the coordinates (x,y) are not an allowed location for this board.
     */
    protected Tile setGrid(int x, int y, Tile item) {
        return setGrid(x,y,item,grid);
    }
    /**
     * Set the item at a specified location in a grid with the same structure as that underlying this board.
     * Subclasses should override this method with any restrictions on allowed indices, and those restrictions will take effect for the board's grid as well.
     *
     * @param x The first coordinate.
     * @param y The second coordinate.
     * @param item The item to replace the value at (x,y).
     * @param matrix The grid to place the item in.
     * @param <E> The type stored by this grid.
     * @return The item previously at (x,y), as specified by the List interface.
     * @throws IndexOutOfBoundsException if the coordinates (x,y) are not an allowed location for this board.
     */
    public <E> E setGrid(int x, int y, E item, List<? extends List<E>> matrix) {
        return matrix.get(x).set(y,item);
    }
    /**
     * Create a two-dimensional grid and initialize it with null.
     *
     * @param width The range for the first index of this grid.
     * @param height The range for the second coordinate of this grid.
     * @param type The class to be stored by this grid.
     * @param <E> The type to be stored by this grid.
     * @return An empty width*height grid with datatype type.
     */
    public static <E> ArrayList<ArrayList<E>> initGrid(int width, int height, Class<E> type) {
        return initGrid(width, height, type, null);
    }
    /**
     * Create a two-dimensional grid and initialize it with a constant value.
     *
     * @param width The range for the first index of this grid.
     * @param height The range for the second coordinate of this grid.
     * @param type The class to be stored by this grid.
     * @param defaultValue The value to place in every cell of the grid.
     * @param <E> The type to be stored by this grid.
     * @return A width*height grid with datatype type and defaultValue in each cell.
     */
    public static <E> ArrayList<ArrayList<E>> initGrid(int width, int height, Class<E> type, E defaultValue) {
        ArrayList<ArrayList<E>> matrix = new ArrayList<ArrayList<E>>(width);
        for (int i = 0; i < width; i++) {
            matrix.add(i, new ArrayList<E>(height));
            for (int j = 0; j < height; j++) {
                matrix.get(i).add(j,defaultValue);
            }
        }
        return matrix;
    }
    
    /**
     * If information about guessing is to be shown, then do so.
     *
     * @param   t The Tile which is about to be guessed.
     * @param   odds The probability that the guess is safe.
     */
    public void alertGuess(Tile t, double odds) {
        if (printProbability) {
            boolean printed = false;
            for (int i = 0; !printed && i < width; i++) {
                for (int j = 0; j < height && !printed; j++) {
                    if (getGrid(i,j) == t) {
                        System.out.println("Guessing "+i+","+j+" with "+(odds*100)+"% chance of success.");
                        printed = true;
                    }
                }
            }
        }
    }
        
    public class GridCoordinate {
        public int x,y;
        public GridCoordinate(int i, int j) {
            x = i;
            y = j;
        }
    }
}
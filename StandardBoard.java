import java.lang.*;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * @author  Matheson Longton
 *
 * A Minesweeper board.  This contains a grid of all the tiles, and an Edge object used for studying them.
 * The statGuess method belongs to the board class because it requires knowledge of non-edge tiles.
 */
public class StandardBoard extends Board {
    protected SquareTile[][] grid;
    /**
     * The dimensions of the Board.
     */
    public final int width, height;
    /**
     * Indicates whether information about guesses should be written to stdout.
     */
    public boolean printProbability;
    
    /**
     * Create a simple rectangular board
     *
     * @param   width The Board's width
     * @param   height The Board's height
     * @param   n The number of mines to be placed
     * @param   probInfo Set to true to print guessing information to stdout.
     */
    public StandardBoard(int width, int height, int n, boolean probInfo) {
        this.printProbability = probInfo;
        this.N = n;
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
     * @param   probInfo Set to true to print guessing information to stdout.
     * @throws  IOException If the file "filename" cannot be properly opened or read.
     */
    public StandardBoard(String filename, boolean probInfo) throws IOException {
        this.printProbability = probInfo;
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
     * If information about guessing is to be shown, then do so.
     *
     * @param   t The Tile which is about to be guessed.
     * @param   odds The probability that the guess is safe.
     */
    public void alertGuess(Tile t, double odds) {
        if (printProbability) {
            boolean printed = false;
            for (int i = 0; !printed && i < grid.length; i++) {
                for (int j = 0; j < grid[i].length && !printed; j++) {
                    if (grid[i][j] == t) {
                        System.out.println("Guessing "+i+","+j+" with "+(odds*100)+"% chance of success.");
                        printed = true;
                    }
                }
            }
        }
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
}
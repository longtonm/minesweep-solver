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
public class StandardBoard extends TwoDBoard {
    
    /**
     * Create a simple rectangular board
     *
     * @param   width The Board's width
     * @param   height The Board's height
     * @param   n The number of mines to be placed
     * @param   probInfo Set to true to print guessing information to stdout.
     */
    public StandardBoard(int width, int height, int n, boolean probInfo) {
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
     * Create a Board from a file
     *
     * Create a Board from a text file formatted as if copied from the output of this program
     *
     * @param   filename The path to read from
     * @param   probInfo Set to true to print guessing information to stdout.
     * @throws  IOException If the file "filename" cannot be properly opened or read.
     *
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
        linkNeighbours();
    }*/
    
    /**
     * Write this Board to stdout
     */
    public void printBoard() { //\todo{maybe update this to use Tile.toString()}
        Tile t;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                t = getGrid(i,j);
                System.out.print(Tile.toString(t));
            }
            System.out.println();
        }
        System.out.println();
        System.out.flush();
    }
}
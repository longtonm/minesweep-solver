import java.util.*;
import java.io.*;

/**
 * @author  Matheson Longton
 *
 * This interface indicates that a Board supports being written to stdout as text.  It also includes static methods to help with reading a Board from a file.
 */
public interface TextBoard {
    
    /**
     * Write this Board to stdout
     */
    void printBoard();
    
    /**
     * Read a text file and convert it to a 2D array of characters representing the tiles on a board.
     * Since most grids are assumed to be rectangular, each line is padded with null characters to reach the length of the longest line.
     *
     * @param fileName The file to attempt to open.
     * @throws IOException if the file cannot be read for any reason.
     * @return An array of characters.
     */
    static char[][] readBoard(String fileName) throws IOException {
        FileReader flRdr = new FileReader(fileName);
        ArrayList<Character> rawFile = new ArrayList<Character>();
        int len = 0, cur;
        while ((cur = flRdr.read()) > -1) {
            rawFile.add((char)cur);
        }
        flRdr.close();
        if (!rawFile.get(rawFile.size()-1).equals('\n')) rawFile.add('\n');
        ArrayList<char[]> file = new ArrayList<char[]>();
        int maxLength = 0, nextRow;
        while ((nextRow = rawFile.indexOf('\n')) > -1) {
            char[] thisLine = new char[nextRow];
            for (int i = 0; i < nextRow; i++) {
                thisLine[i] = rawFile.remove(0);
            }
            if (thisLine.length > 0) {
                file.add(thisLine);
                if (thisLine.length > maxLength) maxLength = thisLine.length;
            }
            rawFile.remove(0);
        }
        for (int i = 0; i < file.size(); i++) {
            if (file.get(i).length < maxLength) {
                file.set(i,Arrays.copyOf(file.get(i),maxLength));
            }
        }
        return file.toArray(new char[1][1]);
    }
    
    /**
     * Checks a character array for characters representing mines and counts them.
     *
     * @param preBoard A char[][] representing the grid of a TwoDBoard.
     * @return The number of characters representing mines.
     */
    static int mineCount(char[][] preBoard) {
        int mCtr = 0;
        for (int i = 0; i < preBoard.length; i++) {
            for (int j = 0; j < preBoard[i].length; j++) {
                switch (preBoard[i][j]) {
                    case 'X': case '*':
                        mCtr++;
                        break;
                }
            }
        }
        return mCtr;
    }
    
    /**
     * Counts the total number of tiles in an array of characters.
     *
     * @param preBoard A char[][] representing the grid of a TwoDBoard.
     * @return The number of characters representing tiles.  Null tiles are not counted.
     */
    static int tileCount(char[][] preBoard) {
        int tCtr = 0;
        for (int i = 0; i < preBoard.length; i++) {
            for (int j = 0; j < preBoard[i].length; j++) {
                switch (preBoard[i][j]) {
                    case '#': case '\u0000':
                        break;
                    default:
                        tCtr++;
                        break;
                }
            }
        }
        return tCtr;
    }
    
    /**
     * Utility method to create a tile represented by a character.
     *
     * @param chr The character to make a tile from.
     * @param i The x coordinate of the tile in the grid.
     * @param j The y coordinate of the tile in the grid.
     * @return A new SquareTile instance which would be described by chr.  If the character does not represent a valid tile, then null is returned.
     */
    static SquareTile makeTile(char chr, int i, int j) {
        SquareTile newTile;
        switch (chr) {
            case ' ': case '0': case '@':
                newTile = new SquareTile(false,false,0,i,j);
                break;
            case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8':
                newTile = new SquareTile(false,false,chr-48,i,j);
                break;
                /*case '@':
                 newTile = new SquareTile(false,true,0,i,j);
                 break;*/
            case '*': case 'X':
                newTile = new SquareTile(true,false,-1,i,j);
                break;
            default:
                newTile = null;
                break;
        }
        return newTile;
    }
}
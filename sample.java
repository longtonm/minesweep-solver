import java.util.*;
import java.io.*;

/**
 * @author  Matheson Longton
 *
 * This simple example creates a Minesweeper Board and then solves it.
 */
public class sample {
    public static void main(String[] args) {
        int x=30, y=16, n=99;
        boolean printEachStep = false, failure = false, deterministicHelped = false;
        Board b = new StandardBoard(1,1,1,false);
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().equals("detail")) printEachStep = true;
            else if (args[i].toLowerCase().equals("load")) {
                char[][] file;
                try {
                    file = TextBoard.readBoard(args[++i]);

                }
                catch (IOException e) {
                    System.out.println(e);
                    file = new char[1][1];
                    file[0][0] = '*';
                }
                b = new StandardBoard(file,true);
            }
            else {
                try {
                    x = Integer.parseInt(args[i]);
                    y = Integer.parseInt(args[++i]);
                    n = Integer.parseInt(args[++i]);
                } catch (Exception e) {
                    x = 30; y = 16; n = 99;
                }
                b = new StandardBoard(x,y,n,true);
            }
        }
        if (args.length == 0) {
            System.out.println("Usage: java sample width height N");
            System.out.println("or java sample filename");
            System.out.println("Default is \"expert\" with dimensions 30 x 16 and 99 mines.");
            b = new StandardBoard(x,y,n,true);
        }
        while (!b.remainingTiles.isEmpty()) {
            if (b.working != null && b.working.hasWork()) {
                boolean doPrint = b.working.compareOne();
                deterministicHelped = deterministicHelped || doPrint;
                if (printEachStep && doPrint) tryPrint(b);
            }
            else {
                if (deterministicHelped) tryPrint(b);
                try {
                    b.statGuess();
                }
                catch (BoomException e) {
                    System.out.println("Hit a mine.  Lost.");
                    failure = true;
                    break;
                }
                tryPrint(b);
                deterministicHelped = false;
            }
        }
        if (failure) {
            for (Iterator<Tile> tileIt = b.remainingIterator(); tileIt.hasNext();) {
                Tile t = tileIt.next();
                if (!(t.flagged || t.isRevealed())) {
                    try {
                        t.reveal();
                    }
                    catch (BoomException e) {}
                }
            }
            tryPrint(b);
        }
        else {
            System.out.println("Won!");
        }
    }
    
    public static void tryPrint(Board b) {
        try {
            ((TextBoard)b).printBoard();
        }
        catch (ClassCastException e) {
            System.out.println("Output not possible for this type of board.");
        }
    }
}
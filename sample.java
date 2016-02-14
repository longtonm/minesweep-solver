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
        StandardBoard b = new StandardBoard(1,1,1,false);
        for (int i = 0; i < args.length; i++) {
            if (args[i].toLowerCase().equals("detail")) printEachStep = true;
            else if (args[i].toLowerCase().equals("load")) {
                try {
                    b = new StandardBoard(args[++i],true);
                }
                catch (IOException e) {
                    System.out.println(e);
                    b = new StandardBoard(1,1,1,false);
                }
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
                if (printEachStep && doPrint) b.printBoard();
            }
            else {
                if (deterministicHelped) b.printBoard();
                try {
                    b.statGuess();
                }
                catch (BoomException e) {
                    System.out.println("Hit a mine.  Lost.");
                    failure = true;
                    break;
                }
                b.printBoard();
                deterministicHelped = false;
            }
        }
        if (failure) {
            for (int i = 0; i < b.width; i++) {
                for (int j = 0; j < b.height; j++) {
                    if (!(b.grid[i][j].flagged || b.grid[i][j].isRevealed())) {
                        try {
                            b.grid[i][j].reveal();
                        }
                        catch (BoomException e) {}
                    }
                }
            }
            b.printBoard();
        }
        else {
            System.out.println("Won!");
        }
    }
}
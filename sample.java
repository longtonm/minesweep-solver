import java.util.*;
import java.io.*;
import org.apache.commons.cli.*;

/**
 * @author  Matheson Longton
 *
 * This simple example creates a Minesweeper Board and then solves it.
 */
public class sample {
    public static void main(String[] args) {
        boolean detail = false, deterministicHelped = false, failure = false;
        Board b = null;
        Options clOpts = defineCLOpts();
        CommandLineParser clParse = new DefaultParser();
        CommandLine cl;
        HelpFormatter hlp = new HelpFormatter();
        try {
            cl = clParse.parse(clOpts,args);
        }
        catch (ParseException e) {
            System.err.println("Invalid options: "+e.getMessage());
            hlp.printHelp("sample",clOpts,true);
            return;
        }
        if (cl.getOptions().length == 0) {
            hlp.printHelp("sample",clOpts,true);
        }
        if (cl.hasOption("h")) {
            hlp.printHelp("sample",clOpts,true);
            return;
        }
        if (cl.hasOption("d")) {
            detail = true;
        }
        if (cl.hasOption("f")) { //read board from a file
            char[][] file;
            try {
                file = TextBoard.readBoard(cl.getOptionValue("f"));
            }
            catch (IOException e) {
                System.err.println("Error opening file: "+e.getMessage());
                return;
            }
            if (cl.hasOption("x") && cl.hasOption("w")) {
                System.err.println("Sorry, wraparound hexagonal grids not supported quite yet.");
                return;
            }
            else if (cl.hasOption("x")) {
                b = new HexBoard(file,true);
            }
            else if (cl.hasOption("w")) {
                System.err.println("Sorry, reading wraparound boards from a file isn't supported yet.");
                //b = new WrapSquareBoard(file,true);
            }
            else {
                b = new StandardBoard(file,true);
            }
        }
        else { //generate new random board
            int x, y, n;
            if (cl.hasOption("s")) {
                String[] dimensions = cl.getOptionValues("s");
                try {
                    x = Integer.parseInt(dimensions[0]);
                    y = Integer.parseInt(dimensions[1]);
                }
                catch (Exception e) {
                    System.err.println("Error parsing dimensions: "+e.getMessage());
                    return;
                }
            }
            else {
                x = 30;
                y = 16;
            }
            int nTiles = x*y;
            if (cl.hasOption("p")) {
                try {
                    n = (int)(Double.parseDouble(cl.getOptionValue("p"))*nTiles/100);
                    //n = (int) ((Double)cl.getParsedOptionValue("p")*nTiles/100); apache commons cli appears to be broken here
                }
                catch (Exception e) {
                    System.err.println("Error parsing percentage of mines: "+e.getMessage());
                    return;
                }
            }
            else if (cl.hasOption("n")) {
                try {
                    n = Integer.parseInt(cl.getOptionValue("n"));
                    //n = (Integer)cl.getParsedOptionValue("n"); apache commons cli appears to be broken here
                }
                catch (Exception e) {
                    System.err.println("Error parsing number of mines: "+e.getMessage());
                    return;
                }
            }
            else {
                n = (int) (nTiles*0.206251);
            }
            if (cl.hasOption("x") && cl.hasOption("w")) {
                System.err.println("Sorry, wraparound hexagonal grids not supported quite yet.");
                return;
            }
            else if (cl.hasOption("x")) {
                b = new HexBoard(x,y,n,cl.hasOption("c"),true);
            }
            else if (cl.hasOption("w")) {
                b = new WrapSquareBoard(x,y,n,cl.hasOption("c"),true);
            }
            else {
                b = new StandardBoard(x,y,n,cl.hasOption("c"),true);
            }
        }
        while (!b.remainingTiles.isEmpty()) {
            if (b.working != null && b.working.hasWork()) {
                boolean doPrint = b.working.compareOne();
                deterministicHelped = deterministicHelped || doPrint;
                if (detail && doPrint) tryPrint(b);
            }
            else {
                if (deterministicHelped && b.remainingN > 0) tryPrint(b);
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
    
    /**
     * Print the board (to stdout) if it supports being written as text.
     *
     * @param b The board to be printed.
     */
    public static void tryPrint(Board b) {
        try {
            ((TextBoard)b).printBoard();
        }
        catch (ClassCastException e) {
            System.out.println("Output not possible for this type of board.");
        }
    }
    
    /**
     * Define the command line options allowed.
     *
     * @return An org.apache.commons.cli.Options instance containing all of the options allowed.
     */
    public static Options defineCLOpts() {
        Options opts = new Options();
        opts.addOption(Option.builder("f")
                       .longOpt("file")
                       .desc("Do not randomly generate a board.  Read it from a file instead.")
                       .hasArg()
                       .build());
        OptionGroup gridOpts = new OptionGroup();
        gridOpts.addOption(Option.builder("q")
                       .longOpt("square")
                       .desc("Use a standard square grid.  This is the default.")
                       .build());
        gridOpts.addOption(Option.builder("x")
                       .longOpt("hex")
                       .desc("Use a hexagonal grid.  Each tile will have six neighbours.")
                       .build());
        opts.addOptionGroup(gridOpts);
        opts.addOption(Option.builder("w")
                       .longOpt("wrap")
                       .desc("Make the board wrap-around.")
                       .build());
        OptionGroup nOpts = new OptionGroup();
        nOpts.addOption(Option.builder("p")
                        .longOpt("percentage")
                        .desc("Set the number of mines as a percentage of the number of tiles.")
                        .hasArg()
                        .type(Double.class)
                        .build());
        nOpts.addOption(Option.builder("n")
                        .longOpt("number")
                        .desc("Set the total number of mines.")
                        .hasArg()
                        .type(Integer.class)
                        .build());
        opts.addOptionGroup(nOpts);
        opts.addOption(Option.builder("s")
                       .longOpt("size")
                       .desc("Set the dimensions of the board: x*y.")
                       .numberOfArgs(2)
                       .build());
        opts.addOption(Option.builder("c")
                       .longOpt("clear")
                       .desc("Clear one safe tile to start.")
                       .build());
        opts.addOption(Option.builder("d")
                       .longOpt("detail")
                       .desc("Print the board each time a tile changes.")
                       .build());
        opts.addOption(Option.builder("h")
                       .longOpt("h")
                       .desc("Print this message.")
                       .build());
        return opts;
    }
}
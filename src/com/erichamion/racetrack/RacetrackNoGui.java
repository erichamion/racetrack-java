package com.erichamion.racetrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class RacetrackNoGui {

    private static final Map<Character, int[]> KEYMAP = new HashMap<>();
    private static final int ROW = 0;
    private static final int COL = 1;
    private static final Scanner STDIN = new Scanner(System.in);

    static {
        KEYMAP.put('1', new int[] {1, -1});
        KEYMAP.put('2', new int[] {1, 0});
        KEYMAP.put('3', new int[] {1, 1});
        KEYMAP.put('4', new int[] {0, -1});
        KEYMAP.put('5', new int[] {0, 0});
        KEYMAP.put('6', new int[] {0, 1});
        KEYMAP.put('7', new int[] {-1, -1});
        KEYMAP.put('8', new int[] {-1, 0});
        KEYMAP.put('9', new int[] {-1, 1});
    }

    public static void main(String[] args) {
        if (args.length == 0 || (args[0].equals("--gui") && args.length == 1)) {
            System.err.println("No filename given\n");
            printUsage(System.err);
            return;
        }

        boolean useGui = args[0].equals("--gui");
        String filename = args[useGui ? 1 : 0];

        Track track;
        try {
            track = new Track(new Scanner(new File(filename)));
        } catch (InvalidTrackFormatException e) {
            System.err.println(e.getMessage());
            return;
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file '" + filename + "'");
            return;
        }

        runTextGame(track);
    }

    private static void printUsage(PrintStream outStream) {
        outStream.println("Usage:");
        outStream.println("    <command> <filename>");
        outStream.println("Where <filename> is the path to a track file to load.");
    }

    private static void printDirections() {
        final String outStr = "Directions are based on the number pad:\n"
                + "7 8 9    7=up-left,   8=up,              9=up-right\n"
                + "4 5 6    4=left,      5=no acceleration, 6=right\n"
                + "1 2 3    1=down-left, 2=down,            3=down-right\n"
                + "\n"
                + "h for help\n"
                + "t to show track\n";
        System.out.println(outStr);
    }

    private static int[] getTextInput(String prompt, Track track) {
        int[] result = null;
        do {
            System.out.print(prompt + ": ");
            char inputChar = STDIN.nextLine().charAt(0);
            if (inputChar == 'h') {
                printDirections();
            } else if (inputChar == 't') {
                System.out.println(track.toString());
            } else if (KEYMAP.containsKey(inputChar)) {
                result = KEYMAP.get(inputChar).clone();
            }
        } while (result == null);

        return result;
    }

    private static void runTextGame(Track track) {
        while (track.getWinner() == Track.NO_WINNER) {
            System.out.println(track.toString());
            System.out.println("\nPLAYER " + (track.getCurrentPlayer() + 1) + ":");
            int[] acceleration = getTextInput("Acceleration direction (h for help)", track);
            track.doPlayerTurn(acceleration[COL], acceleration[ROW]);
        }
        System.out.println(track.toString());
        System.out.println();
        System.out.println("Player " + (track.getWinner() + 1) + " WINS!!");
    }

}

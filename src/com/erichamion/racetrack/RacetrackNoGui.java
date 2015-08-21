package com.erichamion.racetrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class RacetrackNoGui {

    private static final Map<Character, GridPoint> KEYMAP = new HashMap<>();
    private static final Scanner STDIN = new Scanner(System.in);
    private static final Map<Integer, PathFollower> mComputerPlayers = new HashMap<>();

    static {
        KEYMAP.put('1', new GridPoint(1, -1));
        KEYMAP.put('2', new GridPoint(1, 0));
        KEYMAP.put('3', new GridPoint(1, 1));
        KEYMAP.put('4', new GridPoint(0, -1));
        KEYMAP.put('5', new GridPoint(0, 0));
        KEYMAP.put('6', new GridPoint(0, 1));
        KEYMAP.put('7', new GridPoint(-1, -1));
        KEYMAP.put('8', new GridPoint(-1, 0));
        KEYMAP.put('9', new GridPoint(-1, 1));
    }



    public static void main(String[] args) {
        String filename = null;
        List<Integer> playerIndices = new ArrayList<>();

        for (String arg : args) {
            if (arg.length() == 1 && arg.charAt(0) >= '1' && arg.charAt(0) <= '9') {
                playerIndices.add(Integer.parseInt(arg) - 1);
            } else {
                filename = arg;
            }
        }

        if (filename == null) {
            System.err.println("No filename given\n");
            printUsage(System.err);
            return;
        }

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

        for (Integer playerIndex : playerIndices) {
            if (playerIndex >= track.getPlayerCount()) continue;

            PathFinder playerFinder = new PathFinder(track, playerIndex);
            PathFollower playerFollower = new PathFollower(track, playerFinder, playerIndex);
            mComputerPlayers.put(playerIndex, playerFollower);
        }


        runTextGame(track);
    }

    private static void printUsage(final PrintStream outStream) {
        outStream.println("Usage:");
        outStream.println("    <command> [n1 [n2...]] <filename>");
        outStream.println("Where n1, n2, etc. are player numbers 1-9 for computer control,");
        outStream.println("and <filename> is the path to a track file to load.");
        outStream.println("");
        outStream.println("Example: <command> 2 4 tracks/mytrack.txt");
        outStream.println("    Loads the track file 'tracks/mytrack.txt', and (as long as the track");
        outStream.println("    is for at least 4 players) designates players 2 and 4 as computer");
        outStream.println("    controlled. All other players are keyboard controlled.");
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

    private static GridPoint getTextInput(final String prompt, final Track track) {
        GridPoint result = null;
        do {
            System.out.print(prompt + ": ");
            String line = STDIN.nextLine();
            if (line.length() > 0) {
                char inputChar = line.charAt(0);
                if (inputChar == 'h') {
                    printDirections();
                } else if (inputChar == 't') {
                    System.out.println(track.toString());
                } else if (KEYMAP.containsKey(inputChar)) {
                    result = new GridPoint(KEYMAP.get(inputChar));
                }
            }
        } while (result == null);

        return result;
    }

    private static void runTextGame(final Track track) {
        while (track.getWinner() == Track.NO_WINNER) {
            System.out.println(track.toString());
            int currentPlayer = track.getCurrentPlayer();
            System.out.println("\nPLAYER " + (currentPlayer + 1) + ":");
            PathFollower follower = mComputerPlayers.get(currentPlayer);
            GridPoint acceleration;
            if (follower == null) {
                // Get human input
                acceleration = getTextInput("Acceleration direction (h for help)", track);
            } else {
                // Computer player
                System.out.print("Press Enter to continue.");
                STDIN.nextLine();
                acceleration = follower.getMove();
            }
            track.doPlayerTurn(acceleration);
        }
        System.out.println(track.toString());
        System.out.println();
        System.out.println("Player " + (track.getWinner() + 1) + " WINS!!");
    }

}

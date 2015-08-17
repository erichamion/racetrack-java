package com.erichamion.racetrack;



import java.util.*;

/**
 * Created by me on 8/14/15.
 */
public class Track {
    public static final int NO_WINNER = -1;

    private static final char CRASH_INDICATOR = 'X';
    private static final char[] PLAYER_INDICATORS = {'@', '$'};
    private static final int ROW = 0;
    private static final int COL = 1;

    private Player[] mPlayers = {null, null};
    private int mWidth = 0;
    private int mHeight = 0;
    private List<List<SpaceType>> mGrid = new ArrayList<>();
    private int mCurrentPlayer = 0;
    private int mWinner = NO_WINNER;

    /**
     * Initialize a Track from an input source.
     * @param scanner A java.util.Scanner connected to an input source
     *                that holds the track data. Track data must be a
     *                rectangular grid of text. Empty lines at the start
     *                are ignored. Processing stops at the first empty
     *                line following a non-empty line, or at the end of
     *                the stream. The first character in the first
     *                non-empty line is considered a wall. A space
     *                character (' ') is open track. Any of '<', '>', '^',
     *                or 'v' represent a finish line and indicate the
     *                direction the car needs to be moving in order to
     *                successfully cross. Any other character indicates
     *                the starting position for a car, and there must be
     *                two of these (one for each player - either the same
     *                or different characters).
     * @throws InvalidTrackFormatException
     */
    public Track(Scanner scanner) throws InvalidTrackFormatException {
        char borderChar = '\0';
        while (scanner.hasNextLine()) {
            String currentLine = scanner.nextLine();
            int lineLength = currentLine.length();
            if (lineLength == 0) {
                if (mHeight == 0) {
                    continue;
                } else {
                    break;
                }
            }

            if (mWidth == 0) {
                mWidth = lineLength;
            } else {
                if (lineLength != mWidth) {
                    throw new InvalidTrackFormatException("Track does not have a consistent width");
                }
            }

            if (borderChar == '\0') {
                borderChar = currentLine.charAt(0);
            }

            addGridRow(currentLine, borderChar);
        }

        // Final sanity checks
        if (mHeight == 0) throw new InvalidTrackFormatException("No track data supplied");
        // If first player is not set, then second player must also not be set. No need to check both.
        if (mPlayers[mPlayers.length - 1] == null) throw new InvalidTrackFormatException("Not enough player positions");

        mCurrentPlayer = 0;
    }

    /**
     * Return a String representation of the track, including the
     * player locations.
     * @return A String representation of the track
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int rowIndex = 0; rowIndex < mGrid.size(); rowIndex++) {
            List<SpaceType> currentRow = mGrid.get(rowIndex);
            for (int colIndex = 0; colIndex < currentRow.size(); colIndex++) {
                SpaceType currentSpace = currentRow.get(colIndex);
                boolean hasPlayer = false;
                for (int playerNum = 0; playerNum < mPlayers.length; playerNum++) {
                    Player player = mPlayers[playerNum];
                    if (player.getCol() == colIndex && player.getRow() == rowIndex) {
                        hasPlayer = true;
                        result.append(currentSpace == SpaceType.WALL ? CRASH_INDICATOR : PLAYER_INDICATORS[playerNum]);
                    }
                }
                if (!hasPlayer) {
                    result.append(currentSpace.value);
                }
            }
            result.append('\n');
        }

        return result.toString();
    }

    /**
     * Return the width (number of columns) of the track grid.
     * @return Width of the track grid
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Return the height (number of rows) of the track grid.
     * @return Height of the track grid
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Return the number of players (currently fixed at 2).
     * @return Number of players
     */
    public int getPlayerCount() {
        return mPlayers.length;
    }

    /**
     * Return the current player. Player numbers are zero-based, so the
     * first player is 0, and the last player is getPlayerCount() - 1.
     * @return The zero-based number of the current player
     */
    public int getCurrentPlayer() {
        return mCurrentPlayer;
    }

    /**
     * Return the winner of the game. If the game is still in progress,
     * returns NO_WINNER.
     * @return The winning player (zero-based, see getCurrentPlayer()),
     * or NO_WINNER if the game is still in progress
     */
    public int getWinner() {
        return mWinner;
    }

    /**
     * Accelerate the current player, and update the track state.
     * @param colAcceleration The current player's acceleration in the
     *                        column direction
     * @param rowAcceleration The current player's acceleration in the row
     *                        direction
     */
    public void doPlayerTurn(int colAcceleration, int rowAcceleration) {
        Player player = mPlayers[mCurrentPlayer];
        if (player.isCrashed() || mWinner != NO_WINNER) return;

        player.accelerate(colAcceleration, rowAcceleration);
        moveCurrentPlayer();

        if (player.isCrashed()) {
            // With just two players, we COULD just declare the other
            // player the winner. But what if we add more players?
            int winCandidate = -1;
            for (int i = 0; i < mPlayers.length; i++) {
                if (!mPlayers[i].isCrashed()) {
                    if (winCandidate == -1) {
                        winCandidate = i;
                    } else {
                        // More than one uncrashed players. Can't declare
                        // a winner.
                        winCandidate = -2;
                        break;
                    }
                }
                if (winCandidate >= 0) {
                    mWinner = winCandidate;
                }
            }
        }

        mCurrentPlayer = getNextPlayer();
    }


    /**
     * Gets the next player who is still in the game. Skips crashed
     * players.
     * @return The next active player
     */
    private int getNextPlayer() {
        int result = mCurrentPlayer;
        do {
            result += 1;
            if (result >= getPlayerCount()) {
                result = 0;
            }
        } while (mPlayers[result].isCrashed());
        return result;
    }

    private void moveCurrentPlayer() {
        Player player = mPlayers[mCurrentPlayer];

        // Check for collisions and for winning
        int[] startPoint = new int[2];
        startPoint[ROW] = player.getRow();
        startPoint[COL] = player.getCol();
        int[] endPoint = new int[2];
        endPoint[ROW] = player.getNextRow();
        endPoint[COL] = player.getNextCol();

        Set<int[]> pathPoints = getPath(startPoint, endPoint);
        int[] winPoint = null;
        int[] winDirection = {0, 0};
        for (int[] currentPoint : pathPoints) {
            switch(getSpace(currentPoint[ROW], currentPoint[COL])) {
                case TRACK:
                    // No problems here, do nothing
                    break;
                case WALL:
                    // Crash, and move directly to the location that
                    // caused the crash. No need to keep going
                    player.crash();
                    player.setPos(currentPoint[ROW], currentPoint[COL]);
                    return;
                case FINISH_UP:
                    // For all of the finishes, set up a potential win,
                    // but don't act on it yet. We still might crash.
                    winDirection[ROW] = -1;
                    winPoint = currentPoint;
                    break;
                case FINISH_DOWN:
                    winDirection[ROW] = 1;
                    winPoint = currentPoint;
                    break;
                case FINISH_LEFT:
                    winDirection[COL] = -1;
                    winPoint = currentPoint;
                    break;
                case FINISH_RIGHT:
                    winDirection[COL] = 1;
                    winPoint = currentPoint;
                    break;
            }
        }

        // Test for win
        if (winPoint != null) {
            boolean isValidWin = true;
            if ((winDirection[ROW] != 0 && !Util.isSignSame(winDirection[ROW], player.getRowVelocity())) ||
                    (winDirection[COL] != 0 && !Util.isSignSame(winDirection[COL], player.getColVelocity()))) {
                isValidWin = false;
            }
            if (isValidWin) {
                mWinner = mCurrentPlayer;
                player.setPos(winDirection[ROW], winDirection[COL]);
                return;
            }
        }


        player.move();
    }

    /**
     * Returns all of the grid spaces in the path between two spaces.
     * @param startPoint Starting point as an array of length 2, in row-
     *                   column order
     * @param endPoint Ending point as an array of length 2, with row
     *                 first
     * @return Intervening grid spaces, as a List of int arrays. Also
     * includes the starting and ending grid spaces. Each space is given
     * by a an array of length 2, with row first, followed by column.
     */
    private Set<int[]> getPath(final int[] startPoint, final int[] endPoint) {
        // First, pick the axis that has the largest movement.
        // For every grid boundary along that axis, test the line of
        // motion at both the center and the edges of the the cell,
        // identifying the position along the other axis.
        // For each of the identified positions, if the position is
        // within a grid cell, add that cell to the result set.
        // Do nothing if the position is on the boundary between two
        // cells. This means we can squeeze through diagonal corners under
        // the right conditions.

        final double EPS = 1e-8;

        Set<int[]> result = new HashSet<>();

        // If there's no movement, no need to do anything. Just return the
        // starting position.
        if (Arrays.equals(startPoint, endPoint)) {
            result.add(startPoint.clone());
            return result;
        }

        int[] difference = new int[2];
        difference[ROW] = endPoint[ROW] - startPoint[ROW];
        difference[COL] = endPoint[COL] - startPoint[COL];
        int[] distance = new int[2];
        distance[ROW] = Math.abs(difference[ROW]);
        distance[COL] = Math.abs(difference[COL]);

        int mainAxis = (distance[ROW] > distance[COL]) ? ROW : COL;
        int secondAxis = 1 - mainAxis;
        double slope = (double) difference[secondAxis] / difference[mainAxis];
        int stepDirection = (difference[mainAxis] > 0) ? 1 : -1;

        int mainCoord = startPoint[mainAxis];
        while (mainCoord != endPoint[mainAxis]) {
            // Integer coordinate - if applicable, add just the single
            // grid space.
            double secondCoord = Util.getHeightOfLine(slope, startPoint[mainAxis], startPoint[secondAxis], mainCoord);
            if (!Util.isHalfInteger(secondCoord, EPS)) {
                int[] newPoint = new int[2];
                newPoint[mainAxis] = mainCoord;
                newPoint[secondAxis] = (int) Math.round(secondCoord);
                result.add(newPoint);
            }
            // Half-integer coordinate - if applicable, add the grid
            // spaces to either side
            double mainHalfCoord = mainCoord + (stepDirection * 0.5);
            double secondHalfCoord = Util.getHeightOfLine(slope, startPoint[mainAxis], startPoint[secondAxis],
                    mainHalfCoord);
            if (!Util.isHalfInteger(secondHalfCoord, EPS)) {
                // Probably not the best names here, but I'm not sure what
                // would be better. If the main axis is the column axis,
                // and if the endPoint is to the right of the startPoint,
                // then the names leftPoint and rightPoint are accurate.
                int[] leftPoint = new int[2];
                int[] rightPoint = new int[2];
                leftPoint[secondAxis] = rightPoint[secondAxis] = (int) Math.round(secondHalfCoord);
                leftPoint[mainAxis] = mainCoord;
                rightPoint[mainAxis] = mainCoord + stepDirection;
                result.add(leftPoint);
                result.add(rightPoint);
            }

            mainCoord += stepDirection;
        }

        result.add(endPoint.clone());

        return result;
    }


    /**
     * Convert a string into a single row, adding it to the bottom of the
     * grid. Increments mHeight to account for the added row.
     * @param rowString A string containing a single row to add.
     * @param border The character to be interpreted as a wall/border.
     */
    private void addGridRow(String rowString, char border) throws InvalidTrackFormatException {
        int rowLength = rowString.length();
        List<SpaceType> row = new ArrayList<>(rowLength);
        for (int i = 0; i < rowLength; i++) {
            char currentChar = rowString.charAt(i);
            if (currentChar == border) {
                row.add(SpaceType.WALL);
            } else if (currentChar == SpaceType.TRACK.value) {
                row.add(SpaceType.TRACK);
            } else if (currentChar == SpaceType.FINISH_LEFT.value) {
                row.add(SpaceType.FINISH_LEFT);
            } else if (currentChar == SpaceType.FINISH_RIGHT.value) {
                row.add(SpaceType.FINISH_RIGHT);
            } else if (currentChar == SpaceType.FINISH_UP.value) {
                row.add(SpaceType.FINISH_UP);
            } else if (currentChar == SpaceType.FINISH_DOWN.value) {
                row.add(SpaceType.FINISH_DOWN);
            } else {
                // Unexpected character is a player, as long as we don't
                // have too many players. Since mHeight hasn't yet been
                // updated, the row is mHeight (not mHeight - 1).
                boolean tooManyPlayers = true;
                for (int playerNum = 0; playerNum < mPlayers.length; playerNum++) {
                    if (mPlayers[playerNum] == null) {
                        mPlayers[playerNum] = new Player(mHeight, i);
                        row.add(SpaceType.TRACK);
                        tooManyPlayers = false;
                        break;
                    }
                }
                if (tooManyPlayers) {
                    throw new InvalidTrackFormatException("Unexpected character in row " + Integer.toString(mHeight) +
                            " and column " + Integer.toString(i) + ": " + Character.toString(currentChar));
                }
            }
        }

        mGrid.add(row);
        mHeight++;
    }

    private SpaceType getSpace(int row, int col) {
        // Anything out of bounds causes the car to crash, just like a
        // wall
        if (row >= mHeight || row < 0 || col >= mWidth || col < 0) {
            return SpaceType.WALL;
        }

        return mGrid.get(row).get(col);
    }


    private enum SpaceType {
        WALL('#'),
        TRACK(' '),
        FINISH_UP('^'),
        FINISH_DOWN('v'),
        FINISH_LEFT('<'),
        FINISH_RIGHT('>');

        private final char value;

        SpaceType(char c) {
            value = c;
        }


    }
}

package com.erichamion.racetrack;

/**
 * Created by me on 8/16/15.
 */
public class Player {
    private int mRow;
    private int mCol;
    private int mColVelocity;
    private int mRowVelocity;
    private boolean mIsCrashed = false;

    public Player(int row, int col) {
        setPos(row, col);
    }

    public int getRow() {
        return mRow;
    }

    public int getCol() {
        return mCol;
    }

    /**
     * Return the row coordinate of the position that will apply after the
     * next move at the current velocity. Does not complete the move, so
     * the current position remains unchanged.
     * @return Row coordinate of the position after the next move.
     */
    public int getNextRow() {
        return mRow + mRowVelocity;
    }

    /**
     * Return the column coordinate of the position that will apply after
     * the next move at the current velocity. Does not complete the move,
     * so the current position remains unchanged.
     * @return Column coordinate of the position after the next move.
     */
    public int getNextCol() {
        return mCol + mColVelocity;
    }

    public void setPos(int row, int col) {
        mRow = row;
        mCol = col;
    }

    public int getColVelocity() {
        return mColVelocity;
    }

    public int getRowVelocity() {
        return mRowVelocity;
    }

    public void accelerate(int colAcceleration, int rowAcceleration) {
        mColVelocity += colAcceleration;
        mRowVelocity += rowAcceleration;
    }

    public void move() {
        mRow += mRowVelocity;
        mCol += mColVelocity;
    }

    public void crash() {
        mIsCrashed = true;
    }

    public boolean isCrashed() {
        return mIsCrashed;
    }

}

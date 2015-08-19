package com.erichamion.racetrack;

/**
 * Created by me on 8/16/15.
 */
public class Player {
    private GridPoint mPosition;
    private GridPoint mVelocity = new GridPoint(0, 0);
    private boolean mIsCrashed = false;

    public Player(final GridPoint position) {
        setPos(position);
    }

    public Player(int row, int col) {
        mPosition = new GridPoint(row, col);
    }

    public GridPoint getPos() {
        return new GridPoint(mPosition);
    }

    /**
     * Return the position that will apply after the next move at the
     * current velocity. Does not complete the move, so the current
     * position remains unchanged.
     * @return Expected position after the next move
     */
    public GridPoint getNextPos() {
        return GridPoint.add(mPosition, mVelocity);
    }

    public void setPos(final GridPoint pos) {
        mPosition = new GridPoint(pos);
    }

    public GridPoint getVelocity() {
        return new GridPoint(mVelocity);
    }

    public void accelerate(GridPoint acceleration) {
        mVelocity = GridPoint.add(mVelocity, acceleration);
    }

    public void move() {
        mPosition = GridPoint.add(mPosition, mVelocity);
    }

    public void crash() {
        mIsCrashed = true;
    }

    public boolean isCrashed() {
        return mIsCrashed;
    }

}

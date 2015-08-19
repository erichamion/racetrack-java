package com.erichamion.racetrack;

/**
 * Created by me on 8/17/15.
 */
public final class GridPoint implements Cloneable {
    private int mRow;
    private int mCol;


    public enum Axis {
        ROW,
        COL
    }


    public static GridPoint add(final GridPoint obj1, final GridPoint obj2) {
        return new GridPoint(obj1.getRow() + obj2.getRow(), obj1.getCol() + obj2.getCol());
    }

    /**
     * Subtracts obj2 from obj1.
     * @param obj1 The GridPoint from which to subtract obj2
     * @param obj2 The GridPoint to subtract from obj1
     * @return The result of (obj1 - obj2)
     */
    public static GridPoint subtract(final GridPoint obj1, final GridPoint obj2) {
        return new GridPoint(obj1.getRow() - obj2.getRow(), obj1.getCol() - obj2.getCol());
    }



    public GridPoint(int row, int col) {
        mRow = row;
        mCol = col;
    }

    public GridPoint(GridPoint other) {
        mRow = other.getRow();
        mCol = other.getCol();
    }

    public GridPoint() {
        mRow = 0;
        mCol = 0;
    }

    public int getRow() {
        return mRow;
    }

    public void setRow(int row) {
        this.mRow = row;
    }

    public int getCol() {
        return mCol;
    }

    public void setCol(int col) {
        this.mCol = col;
    }

    public int getValueOnAxis(Axis axis) {
        return (axis == Axis.ROW) ? mRow : mCol;
    }

    public void setValueOnAxis(Axis axis, int value) {
        if (axis == Axis.ROW) {
            mRow = value;
        } else {
            mCol = value;
        }
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GridPoint)) throw new ClassCastException();
        final GridPoint otherGridPoint = (GridPoint) other;
        return mRow == otherGridPoint.getRow() && mCol == otherGridPoint.getCol();
    }

    @Override
    public String toString() {
        return "R " + Integer.toString(mRow) + ", C " + Integer.toString(mCol);
    }
}

package com.erichamion.racetrack;

import java.util.Iterator;

/**
 * Created by me on 8/16/15.
 */
public class Util {
    /**
     * Compares two double values for approximate equality
     * @param value1 The first value to compare
     * @param value2 The second value to compare
     * @param epsilon If the difference between the values is less than
     *                epsilon, they will be considered equal
     * @return -1 if value1 is less than value2, 1 if value1 is greater
     * than value2, or 0 if the values are approximately equal (within
     * epsilon)
     */
    public static int doubleCompare(double value1, double value2, double epsilon) {
        double diff = value1 - value2;
        if (Math.abs(diff) < epsilon) {
            return 0;
        } else if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Determine whether a value lies halfway between two consecutive
     * integers (that is, whether the value is (n + 0.5) for some integer
     * n. Uses an approximate comparison.
     * @param value The value to check
     * @param epsilon If value is different from (n + 0.5) by an amount
     *                less than epsilon, it will be considered equal
     * @return Returns true if value is within epsilon of (n + 0.5), false
     * otherwise
     */
    public static boolean isHalfInteger(double value, double epsilon){
        return doubleCompare(Math.floor(value), value - 0.5, epsilon) == 0;
    }

    /**
     * Get the Y-coordinate of a line at the specified X-coordinate, given
     * a slope and a starting point.
     * @param slope The slope of the line
     * @param startX The X-coordinate (independent axis coordinate of the
     *               starting point
     * @param startY The Y-coordinate (dependent axis coordinate of the
     *               starting point
     * @param x The X-coordinate for which to find the corresponding Y
     * @return The height of the line at the given x
     */
    public static double getHeightOfLine(double slope, int startX, int startY, double x) {
        return slope * (x - startX) + startY;
    }

    public static boolean isSignSame(int value1, int value2) {
        int sign1 = (value1 == 0) ? 0 : value1 / Math.abs(value1);
        int sign2 = (value2 == 0) ? 0 : value2 / Math.abs(value2);
        return sign1 == sign2;
    }

    /**
     * Returns a reference to a specified element within  an Iterable
     * container, if such element exists. Does not remove the element
     * from its container.
     * @param iterable The collection or other Iterable that (potentially)
     *                 contains the specified element
     * @param obj An object used to select the desired element. Each
     *            element e will be tested using e.equals(obj).
     * @param <T> The type of element held by iterable
     * @return If one or more element e exist such that e.equals(obj),
     * returns the first such element. Otherwise, returns null.
     */
    public static <T> T getObjectFromIterable(Iterable<T> iterable, Object obj) {
        for (T currentElement : iterable) {
            if (currentElement.equals(obj)) return currentElement;
        }
        return null;
    }
}

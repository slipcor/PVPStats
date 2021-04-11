package net.slipcor.pvpstats.math;

import java.util.Arrays;

/**
 * Pointing Exception Class
 *
 * A class to add some functionality to inheriting classes.
 * Specifically, an arrow to point out where in the input String an error is.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class PointingException extends RuntimeException {
    public PointingException(String message) {
        super(message);
    }

    /**
     * Give us a String that will point to a position in another String
     *
     * @param n the position to point to
     * @return an arrow pointing to the nth position
     */
    protected static String arrow(int n) {
        char[] value = new char[n];
        Arrays.fill(value, ' ');
        value[n - 1] = '^';
        return new String(value);
    }

    /**
     * Give us a String that will point to a range in another String
     *
     * @param fromIndex the starting point
     * @param toIndex   the ending point (exclusive)
     * @return a series of arrows pointing from fromIndex to toIndex
     */
    protected static String arrow(int fromIndex, int toIndex) {
        char[] value = new char[toIndex];
        Arrays.fill(value, 0, fromIndex, ' ');
        Arrays.fill(value, fromIndex, toIndex, '^');
        return new String(value);
    }
}

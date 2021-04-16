package net.slipcor.pvpstats.math;

import java.util.Objects;

/**
 * Parenthesis Token Class
 *
 * A parenthesis opens up a new expression, or closes it.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class ParenthesisToken implements Token {
    private static final ParenthesisToken LEFT = new ParenthesisToken('(', true);
    private static final ParenthesisToken RIGHT = new ParenthesisToken(')', false);

    final char symbol;
    final boolean left;

    private ParenthesisToken(char symbol, boolean left) {
        this.symbol = symbol;
        this.left = left;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParenthesisToken that = (ParenthesisToken) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return "ParenthesisToken{" + symbol + '}';
    }

    static ParenthesisToken left() {
        return LEFT;
    }

    static ParenthesisToken right() {
        return RIGHT;
    }
}

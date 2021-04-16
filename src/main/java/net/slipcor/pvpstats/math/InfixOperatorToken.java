package net.slipcor.pvpstats.math;

import java.util.Objects;

/**
 * Infix Operator Token Class
 *
 * An infix operator token belongs between two other tokens.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
class InfixOperatorToken implements Token {
    final String symbol;  // the symbol defining this Token
    final int precedence; // the precedence we take over other Tokens
    final boolean left;   // whether we are left-associative

    /**
     * Create an infix operator token instance
     *
     * @param symbol     the symbol to identify this token
     * @param precedence its precedence
     * @param left       whether it is left-associative
     */
    InfixOperatorToken(String symbol, int precedence, boolean left) {
        this.symbol = symbol;
        this.precedence = precedence;
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
        InfixOperatorToken that = (InfixOperatorToken) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return "InfixOperatorToken{" + symbol + '}';
    }
}

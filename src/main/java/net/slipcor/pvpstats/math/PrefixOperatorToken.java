package net.slipcor.pvpstats.math;

import java.util.Objects;

/**
 * Prefix Operator Token Class
 *
 * A prefix operator token belongs in front of another Token.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class PrefixOperatorToken implements Token {
    final String symbol;  // the symbol defining this Token
    final int precedence; // the precedence we take over other Tokens

    /**
     * Create a prefix operator token instance
     *
     * @param symbol     the symbol to identify this token
     * @param precedence its precedence
     */
    public PrefixOperatorToken(String symbol, int precedence) {
        this.symbol = symbol;
        this.precedence = precedence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PrefixOperatorToken that = (PrefixOperatorToken) o;
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

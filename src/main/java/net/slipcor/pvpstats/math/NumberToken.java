package net.slipcor.pvpstats.math;

import java.util.Objects;

/**
 * Number Token Class
 *
 * A number token belongs in front of a prefix or around an infix operator.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class NumberToken implements Token {
    final double value;

    public NumberToken(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NumberToken that = (NumberToken) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NumberToken{" + value + '}';
    }
}

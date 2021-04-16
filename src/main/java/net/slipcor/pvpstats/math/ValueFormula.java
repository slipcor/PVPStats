package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.classes.PlayerStatistic;

import java.util.Objects;

/**
 * Value Formula Class
 *
 * Evaluation simply means returning the number.
 */
public class ValueFormula implements Formula {
    private final double value;

    public ValueFormula(double value) {
        this.value = value;
    }

    @Override
    public double evaluate(PlayerStatistic stats) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueFormula that = (ValueFormula) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

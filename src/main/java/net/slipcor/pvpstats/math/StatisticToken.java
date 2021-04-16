package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.api.InformationType;

import java.util.Objects;

/**
 * Statistic Placeholder Token Class
 *
 * A statistic placeholder token will be replaced with a PlayerStatistic's value.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class StatisticToken implements Token {
    public InformationType type; // the statistic information type we will replace.

    public StatisticToken(InformationType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatisticToken that = (StatisticToken) o;
        return that.type == type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "StatisticToken{" + type + '}';
    }
}

package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.classes.PlayerStatistic;

/**
 * The Formula Interface
 *
 * This interface deals with evaluating a formula which can consist of complex sub parts, all inheriting this interface.
 */
public interface Formula {
    double evaluate(PlayerStatistic stats);
}

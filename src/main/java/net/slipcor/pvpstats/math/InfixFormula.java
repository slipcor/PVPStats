package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.classes.PlayerStatistic;

/**
 * Infix Formula Class
 *
 * Evaluation simply means getting both formula we are attached to and applying the mathematical formula that defines us.
 */
public class InfixFormula implements Formula {
    final String symbol; // the symbol that defines the formula
    final Formula left;  // the left Formula it attaches to
    final Formula right; // the right Formula it attaches to

    public InfixFormula(String symbol, Formula left, Formula right) {
        this.symbol = symbol;
        this.left = left;
        this.right = right;
    }

    @Override
    public double evaluate(PlayerStatistic stats) {
        double leftValue = left.evaluate(stats);
        double rightValue = right.evaluate(stats);

        if (symbol.equals("*")) {
            return leftValue * rightValue;
        } else if (symbol.equals("/")) {
            if (rightValue == 0) {
                // If we would have to divide by 0, we just display left value as a result. No-one wants to see infinity or N/A.
                return leftValue;
            }
            return leftValue / rightValue;
        } else if (symbol.equals("+")) {
            return leftValue + rightValue;
        } else if (symbol.equals("-")) {
            return leftValue - rightValue;
        } else if (symbol.equals("^")) {
            return Math.pow(leftValue, rightValue);
        }

        throw new IllegalArgumentException("InfixFormula not implemented yet: " + symbol);
    }
}

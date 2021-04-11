package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.api.InformationType;

import java.util.HashMap;
import java.util.Map;

/**
 * The Environment class
 *
 * This class houses all operator definitions.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class Environment {

    // Which symbols do we support for operators?
    private static final String OPERATOR_SYMBOLS = "+-*/^";

    private final Map<String, InfixOperatorToken> infixOperators;   // operators that are in between operands
    private final Map<String, PrefixOperatorToken> prefixOperators; // operators that are before their operands
    private final Map<String, StatisticToken> statistics;           // statistic placeholders

    Environment() {
        infixOperators = new HashMap<>();
        prefixOperators = new HashMap<>();
        statistics = new HashMap<>();

        // Tier 2 - We are not really important but there might be something less important in the future...
        registerInfixOperator("+", 2, true); // Addition
        registerInfixOperator("-", 2, true); // Subtraction

        // Tier 3 - We are more important than the simple operations.
        registerInfixOperator("*", 3, true); // Multiplication
        registerInfixOperator("/", 3, true); // Division

        // Tier 4 - We are the most important!
        registerInfixOperator("^", 4, false); // Power

        registerPrefixOperator("+", 4); // positive sign
        registerPrefixOperator("-", 4); // negative sign

        // These are statistical types that we support for numerical replacement.
        registerStatistic("&k", InformationType.KILLS);
        registerStatistic("&d", InformationType.DEATHS);
        registerStatistic("&e", InformationType.ELO);
        registerStatistic("&s", InformationType.CURRENTSTREAK);
        registerStatistic("&m", InformationType.STREAK);
    }

    /**
     * Register an infix operator definition
     *
     * @param symbol     the symbol defining this operator
     * @param precedence the precedence level
     * @param left       whether the operator is left-associative
     */
    private void registerInfixOperator(String symbol, int precedence, boolean left) {
        validateOperator(symbol);
        infixOperators.put(symbol, new InfixOperatorToken(symbol, precedence, left));
    }

    /**
     * Register a prefix operator definition
     *
     * @param symbol     the symbol defining this operator
     * @param precedence the precedence level
     */
    private void registerPrefixOperator(String symbol, int precedence) {
        validateOperator(symbol);
        prefixOperators.put(symbol, new PrefixOperatorToken(symbol, precedence));
    }

    /**
     * Register a statistic placeholder
     *
     * @param symbol the symbol defining this placeholder
     * @param type   the information type that will be replaced
     */
    private void registerStatistic(String symbol, InformationType type) {
        statistics.put(symbol, new StatisticToken(type));
    }

    /**
     * Check whether a character is a valid operator symbol
     *
     * @param c the character to check
     * @return whether the character is a valid operator symbol
     */
    boolean isValidOperatorSymbol(char c) {
        return OPERATOR_SYMBOLS.indexOf(c) >= 0;
    }

    /**
     * Attempt to get an infix operator token
     *
     * @param symbol the symbol to look for
     * @return the operator (can be null)
     */
    InfixOperatorToken getInfixOperator(String symbol) {
        return infixOperators.get(symbol);
    }

    /**
     * Attempt to get a prefix operator token
     *
     * @param symbol the symbol to look for
     * @return the operator (can be null)
     */
    PrefixOperatorToken getPrefixOperator(String symbol) {
        return prefixOperators.get(symbol);
    }

    /**
     * Attempt to get a statistic placeholder token
     *
     * @param symbol the symbol to look for
     * @return the placeholder token (can be null)
     */
    StatisticToken getStatic(String symbol) {
        return statistics.get(symbol);
    }

    /**
     * Validate a symbol to make sure it only contains valid characters
     *
     * @param symbol the input String
     */
    private void validateOperator(String symbol) {
        for (char c : symbol.toCharArray()) {
            if (!isValidOperatorSymbol(c)) {
                throw new IllegalArgumentException("invalid operator symbol: " + c);
            }
        }
    }
}

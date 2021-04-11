package net.slipcor.pvpstats.math;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * The Parser Class
 *
 * This class takes a List of Tokens and tries turns them into one single Formula to later evaluate.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class Parser {
    private boolean debug = false;

    /**
     * Convert a list of Tokens into one single Formula
     *
     * @param tokens the List of Tokens to parse
     * @return the Formula to later evaluate
     */
    public Formula parse(List<Token> tokens) {
        // Do a little bit of magic of reorganizing the Tokens so we can go through it in order, later.
        List<Token> postFix = new InfixToPostfix().convert(tokens);

        if (debug) {
            System.out.println("postFix: " + postFix);
        }

        // the deck of cards we throw things on to later take from, filo style
        Deque<Formula> stack = new ArrayDeque<>();

        for (Token token : postFix) {
            if (token instanceof NumberToken || token instanceof StatisticToken) {
                // Numbers or replacement numbers just go on the stack to be grabbed later.
                stack.push(parse(token));
                continue;
            }

            // If we want to construct a prefix formula
            if (token instanceof PrefixOperatorToken) {
                // Grab the top item off the stack and make the formula with it.
                Formula argument = stack.pop();
                // Throw it back on the stack.
                stack.push(new PrefixFormula(((PrefixOperatorToken) token).symbol, argument));
            }

            // If we want to construct an infix formula:
            if (token instanceof InfixOperatorToken) {
                // The top of the stack has the right argument.
                Formula right = stack.pop();
                // Below it lies the left argument.
                Formula left = stack.pop();
                // Throw the resulting Formula back onto the stack.
                stack.push(new InfixFormula((((InfixOperatorToken) token).symbol), left, right));
            }
        }

        if (stack.size() == 1) {
            // The result should be a single combined Formula, return it.
            return stack.pop();
        }

        throw new UnexpectedStackSizeException(stack.size());
    }

    /**
     * Parse a single Token into a formula
     *
     * @param token the Token to parse
     * @return the resulting Formula
     */
    Formula parse(Token token) {
        if (token instanceof NumberToken) {
            NumberToken number = (NumberToken) token;
            return new ValueFormula(number.value);
        } else if (token instanceof StatisticToken) {
            StatisticToken statistic = (StatisticToken) token;
            return StatisticFormula.from(statistic.type);
        }
        throw new IllegalArgumentException("Cannot be turned into Formula: " + token);
    }

    @Deprecated
    public void setDebug(boolean value) {
        this.debug = value;
        InfixToPostfix.debug = value;
    }
}

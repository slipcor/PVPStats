package net.slipcor.pvpstats.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The Lexer Class
 *
 * This class uses an Environment class to turn a String input into a List of Tokens.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class Lexer {
    // the Environment instance which has the operator and symbol definitions
    private final Environment env = new Environment();

    /**
     * Turn a String input into a List of Tokens
     *
     * @param input the String to interpret
     * @return the Token List
     */
    List<Token> tokenize(String input) {
        validate(input);
        return tokenize(input, 0, new ArrayList<>());
    }

    /**
     * Turn a String input into a List of Tokens, with offset and previously found Tokens
     *
     * @param input  the original input String
     * @param offset the position we are currently investigating
     * @param result previously found Tokens
     * @return the full List of all Tokens found
     */
    private List<Token> tokenize(String input, int offset, List<Token> result) {
        if (offset >= input.length()) {
            // We are done, return what we found.
            return result;
        }

        char c = input.charAt(offset);

        if (Character.isWhitespace(c)) {
            return tokenize(input, offset + 1, result);
        }
        if (c == '(') {
            return eatLeftParenthesis(input, offset, result);
        }
        if (c == ')') {
            return eatRightParenthesis(input, offset, result);
        }
        if (c == '&') {
            return eatStatistic(input, offset, result);
        }
        if (Character.isDigit(c)) {
            return eatNumber(input, offset, result);
        }
        if (env.isValidOperatorSymbol(c)) {
            return eatOperator(input, offset, result);
        }

        throw new UnexpectedSymbolException(input, offset);
    }

    /**
     * Validate a String input, check for mismatched parentheses or misplaced operators
     *
     * @param input the String input
     */
    private void validate(String input) {
        Deque<Integer> stack = new ArrayDeque<>(input.length());

        char last = '(';

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                stack.push(i);
            } else if (c == ')') {
                if (stack.isEmpty()) {
                    // There is nothing to close yet, so this does not belong here!
                    throw new UnmatchedRightParenthesisException(input, i);
                }
                if (env.isValidOperatorSymbol(last)) {
                    // The last character was an operator, we can not close the parenthesis here!
                    throw new UnexpectedOperatorException(input, i - 1, false);
                }
                stack.pop();
            } else if (env.isValidOperatorSymbol(c) && last == '(') {
                // This is the start of an/the expression, here should not be an operator unless it is prefix!
                if (c != '+' && c != '-') {
                    throw new UnexpectedOperatorException(input, i, true);
                }
            }
            last = c;
        }
        if (env.isValidOperatorSymbol(last)) {
            // The last character was an operator, we can not end here!
            throw new UnexpectedOperatorException(input, input.length() - 1, false);
        }
        if (!stack.isEmpty()) {
            int i = stack.pop();
            // We forgot to close a parenthesis! Show where it opened.
            throw new UnmatchedLeftParenthesisException(input, i);
        }
    }

    /**
     * Add a left parenthesis to the list and continue looking for other Tokens
     *
     * @param input  the original input String
     * @param pos    the position we found the parenthesis
     * @param result previously found Tokens
     * @return the full List of all Tokens
     */
    private List<Token> eatLeftParenthesis(String input, int pos, List<Token> result) {
        result.add(ParenthesisToken.left());
        return tokenize(input, pos + 1, result);
    }

    /**
     * Add a right parenthesis to the list and continue looking for other Tokens
     *
     * @param input  the original input String
     * @param pos    the position we found the parenthesis
     * @param result previously found Tokens
     * @return the full List of all Tokens
     */
    private List<Token> eatRightParenthesis(String input, int pos, List<Token> result) {
        result.add(ParenthesisToken.right());
        return tokenize(input, pos + 1, result);
    }

    /**
     * Try reading a number, add it to the list and continue looking for other Tokens
     *
     * @param input  the original input String
     * @param pos    the position we expect a number at
     * @param result previously found Tokens
     * @return the full List of all Tokens
     */
    private List<Token> eatNumber(String input, int pos, List<Token> result) {
        boolean eatingExponent = false; // Are we already in the exponent parsing part?
        boolean eatingDecimals = false; // Have we found a decimal point?
        boolean exponentSign = false;   // Does the exponent have a sign?
        int i;
        for (i = pos + 1; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isDigit(c) || c == '.' || c == 'e' || c == '-') {
                if (c == 'e') {
                    if (eatingExponent) {
                        // We already found an exponent denominator, there is an issue here!
                        throw new UnexpectedSymbolException(input, pos + i);
                    }
                    eatingExponent = true;
                } else if (c == '-') {
                    if (!eatingExponent || exponentSign) {
                        // If we did not find an exponent denominator that would allow for a sign, or
                        // if we already found a sign, the number should be done and this is a new operator.
                        break;
                    }
                    exponentSign = true;
                } else if (c == '.') {
                    if (eatingExponent || eatingDecimals) {
                        // We already found the decimals or we even are at the exponent part already.
                        // There should be no decimals at the position, this has to be an error!
                        throw new UnexpectedSymbolException(input, pos + i);
                    }
                    eatingDecimals = true;
                }
                continue;
            }

            break;
        }

        String part = input.substring(pos, i);
        try {
            double value = Double.parseDouble(part);
            Token token = new NumberToken(value);
            result.add(token);
        } catch (NumberFormatException e) {
            throw new InvalidNumberException(input, pos, part);
        }

        return tokenize(input, i, result);
    }

    /**
     * Try reading an operator, add it to the list and continue looking for other Tokens
     *
     * @param input  the original input String
     * @param pos    the position we expect an operator
     * @param result previously found Tokens
     * @return the full List of all Tokens
     */
    private List<Token> eatOperator(String input, int pos, List<Token> result) {
        int i;
        for (i = pos + 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!env.isValidOperatorSymbol(c)) {
                break;
            }
        }

        boolean prefix = isPrefixOperatorExpected(result);

        for (int j = i; j > pos; j--) {
            String symbol = input.substring(pos, j);

            Token token = (prefix) ? env.getPrefixOperator(symbol) : env.getInfixOperator(symbol);

            if (token == null) {
                continue;
            }

            // We found a matching token to a possibly multi character symbol, add it to the list and continue looking.
            result.add(token);
            return tokenize(input, j, result);
        }

        throw (prefix)
                ? new UnknownPrefixOperatorException(input, pos, i)
                : new UnknownInfixOperatorException(input, pos, i);
    }

    /**
     * Check whether we expect a PrefixOperatorToken rather than an InfixOperatorToken
     *
     * @param result the Tokens found so far
     * @return whether the next Token should be a PrefixOperationToken
     */
    private boolean isPrefixOperatorExpected(List<Token> result) {
        if (result.isEmpty()) {
            // At the beginning we only expect a prefix operator.
            return true;
        }

        Token previous = result.get(result.size() - 1);

        if (previous instanceof InfixOperatorToken) {
            // After an infix operator, we expect a prefix operator.
            return true;
        }
        if (previous instanceof ParenthesisToken) {
            ParenthesisToken parenthesis = (ParenthesisToken) previous;
            // After an opening parenthesis we expect only a prefix operator.
            return parenthesis.left;
        }
        // Otherwise, we fall back to expecting an infix operator.
        return false;
    }

    /**
     * Try reading a PlayerStatistic placeholder, add it to the list and continue looking for other Tokens
     *
     * @param input  the original input String
     * @param pos    the position we expect a placeholder at
     * @param result previously found Tokens
     * @return the full List of all Tokens
     */
    private List<Token> eatStatistic(String input, int pos, List<Token> result) {
        Token token;
        try {
            char symbol = input.charAt(pos + 1);
            if (!Character.isAlphabetic(symbol)) {
                // We expect a letter here, so a symbol is not expected here!
                throw new UnexpectedSymbolException(input, pos + 1);
            }
            String prefix = input.substring(pos, pos + 2);
            token = env.getStatic(prefix);
        } catch (StringIndexOutOfBoundsException exception) {
            // If we went out of bounds, the input ended with an ampersand. Let's assume it is an invalid symbol in that place.
            throw new UnexpectedSymbolException(input, pos);
        }
        if (token == null) {
            // We found a letter that does not correlate with anything we know, assume we tried a placeholder, but mistyped.
            throw new UnknownStatisticException(input, pos);
        }
        result.add(token);
        return tokenize(input, pos + 2, result);
    }
}

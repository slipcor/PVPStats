package net.slipcor.pvpstats.math;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The Infix To Postfix Class
 *
 * This class receives a List of Tokens in human readable order and returns a List of Tokens
 * - stripping parentheses and applying them as top priority indicator
 * - applying precedence that the tokens claim
 * - ordered in a postfix logic to process it more easily later
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class InfixToPostfix {

    private final List<Token> output; // the output Token List
    private final Deque<Token> stack; // the temporary stack to put things on to remember

    static boolean debug = false;

    InfixToPostfix() {
        output = new ArrayList<>();
        stack = new ArrayDeque<>();
    }

    /**
     * Do the actual conversion
     *
     * @param tokens a List of Tokens ordered as they are written in the input String
     * @return a List of Tokens ordered for ease of parsing
     */
    List<Token> convert(List<Token> tokens) {
        // First off, make sure we are all clean.
        output.clear();
        stack.clear();

        if (debug) {
            System.out.println("converting: " + tokens);
        }

        for (Token token : tokens) {
            if (debug) {
                System.out.println("token: " + token);
            }
            if (token instanceof NumberToken) {
                number((NumberToken) token);
                continue;
            }
            if (token instanceof StatisticToken) {
                statistic((StatisticToken) token);
                continue;
            }
            if (token instanceof PrefixOperatorToken) {
                prefix((PrefixOperatorToken) token);
                continue;
            }
            if (token instanceof InfixOperatorToken) {
                infix((InfixOperatorToken) token);
                continue;
            }
            if (token instanceof ParenthesisToken) {
                parenthesis((ParenthesisToken) token);
                continue;
            }
            throw new UnexpectedStackContentException(token);
        }

        // After we are done, all that remains should be operators that we threw on there.
        while (!stack.isEmpty()) {
            Token top = stack.peek();
            if (debug) {
                System.out.println("top: " + top);
            }
            if (popIfOperator(top)) {
                continue;
            }
            throw new UnexpectedStackContentException(top);
        }

        return output;
    }

    /**
     * Handle NumberTokens
     *
     * @param token the NumberToken
     */
    private void number(NumberToken token) {
        // Numbers go straight to the output.
        output.add(token);
    }

    /**
     * Handle StatisticTokens
     *
     * @param token the StatisticToken
     */
    private void statistic(StatisticToken token) {
        // Statistics go straight to the output.
        output.add(token);
    }

    /**
     * Handle InfixOperatorToken
     *
     * @param token the InfixOperatorToken
     */
    private void infix(InfixOperatorToken token) {
        // Infix operators go to the output, but it is necessary to check
        // the stack for tokens that might need to be resolved first.
        while (!stack.isEmpty()) {
            Token peek = stack.peek();

            if (peek instanceof PrefixOperatorToken) {
                PrefixOperatorToken top = (PrefixOperatorToken) peek;
                if (top.precedence > token.precedence) {
                    // We want that top Token because it is more important than us!
                    output.add(stack.pop());
                    continue;
                }
                break;
            }

            if (peek instanceof InfixOperatorToken) {
                InfixOperatorToken top = (InfixOperatorToken) peek;
                if (top.precedence > token.precedence) {
                    // We want that top Token because it is more important than us!
                    output.add(stack.pop());
                    continue;
                }
                if (top.precedence == token.precedence && token.left) {
                    // We want that top Token because it is more important than us!
                    output.add(stack.pop());
                    continue;
                }
                break;
            }

            // Parentheses do not pop anything.
            if (peek instanceof ParenthesisToken) {
                break;
            }
        }
        // Finally, pop us onto the stack so we can be popped later.
        stack.push(token);
    }

    /**
     * Handle PrefixOperatorToken
     *
     * @param token the PrefixOperatorToken
     */
    private void prefix(PrefixOperatorToken token) {
        // Prefix operators go on the stack to be popped later.
        stack.push(token);
    }

    private void parenthesis(ParenthesisToken token) {
        // Parenthesis do not go to the output, but they change the things that happen inside them.
        if (token.left) {
            // Left parenthesis just goes on the stack.
            stack.push(token);
        } else {
            // Right parenthesis terminates stuff.
            while (!stack.isEmpty()) {
                Token peek = stack.peek();

                // Operators inside the current "parenthesis block" now go straight to the output.
                if (popIfOperator(peek)) {
                    continue;
                }
                if (peek instanceof ParenthesisToken) {
                    ParenthesisToken parenthesis = (ParenthesisToken) peek;
                    if (!parenthesis.left) {
                        // This should not happen really, but just in case for some reason it does...
                        throw new IllegalArgumentException("Mismatched Parenthesis: " + token);
                    }
                    stack.pop();
                    // Alright, we found our match. That's it for now.
                    break;
                }

                throw new UnexpectedStackContentException(token);
            }
        }
    }

    /**
     * If the given Token is an operator Token, pop it off the stack and add it to the output
     *
     * @param token the Token to check
     * @return whether we did pop and add it
     */
    private boolean popIfOperator(Token token) {
        if (token instanceof InfixOperatorToken || token instanceof PrefixOperatorToken) {
            stack.pop();
            output.add(token);
            return true;
        }
        return false;
    }
}

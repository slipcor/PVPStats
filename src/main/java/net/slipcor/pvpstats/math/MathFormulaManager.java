package net.slipcor.pvpstats.math;

import java.util.List;

/**
 * Math Formula Parser
 *
 * This class uses a Lexer and a Parser to turn a String input into a Formula that can later be evaluated.
 * It features customized error handling, trying to show where and what the issue is.
 * This is the only class facing outwards, next to the interfaces.
 *
 * Heavily based on garbagemule's Formula parser, during its development, around April 2021.
 */
public class MathFormulaManager {

    private final Lexer lexer;   // the Lexer instance that turns text into Tokens
    private final Parser parser; // the Parser instance that turns Tokens into a Formula

    // placeholder for the static instance that never needs to change
    private static MathFormulaManager instance;

    private MathFormulaManager(Lexer lexer, Parser parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    public static MathFormulaManager getInstance() {
        if (instance == null) {
            Lexer lexer = new Lexer();
            Parser parser = new Parser();
            instance = new MathFormulaManager(lexer, parser);
        }
        return instance;
    }

    /**
     * Turn a String input into a formula to later evaluate
     *
     * @param input the String to interpret
     * @return a Formula instance
     */
    public Formula parse(String input) {
        List<Token> tokens = lexer.tokenize(input);
        try {
            return parser.parse(tokens);
        } catch (UnexpectedStackSizeException exception) {
            throw new StackException(input, exception.getMessage());
        }
    }

    @Deprecated
    public void setDebug(boolean value) {
        this.parser.setDebug(value);
    }
}

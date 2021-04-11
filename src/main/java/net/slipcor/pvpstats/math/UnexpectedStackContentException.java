package net.slipcor.pvpstats.math;

public class UnexpectedStackContentException extends IllegalArgumentException {
    UnexpectedStackContentException(Token input) {
        super("Unexpected Token: " + input);
    }
}

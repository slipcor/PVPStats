package net.slipcor.pvpstats.math;

class UnmatchedLeftParenthesisException extends PointingException {
    UnmatchedLeftParenthesisException(String input, int pos) {
        super(
                String.format(
                        "Unmatched left parenthesis '%s' in column %d:\n%s\n%s",
                        input.substring(pos, pos + 1),
                        pos + 1,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

package net.slipcor.pvpstats.math;

class UnmatchedRightParenthesisException extends PointingException {
    UnmatchedRightParenthesisException(String input, int pos) {
        super(
                String.format(
                        "Unmatched right parenthesis '%s' in column %d:\n%s\n%s",
                        input.substring(pos, pos + 1),
                        pos + 1,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

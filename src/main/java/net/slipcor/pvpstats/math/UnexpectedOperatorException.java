package net.slipcor.pvpstats.math;

class UnexpectedOperatorException extends PointingException {
    UnexpectedOperatorException(String input, int pos, boolean prefix) {
        super(
                String.format(
                        "Unexpected operator '%s' in column %d (expression %s):\n%s\n%s",
                        input.substring(pos, pos + 1),
                        pos + 1,
                        prefix ? "start" : "end",
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

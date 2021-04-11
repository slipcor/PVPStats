package net.slipcor.pvpstats.math;

class UnknownInfixOperatorException extends PointingException {
    UnknownInfixOperatorException(String input, int pos, int i) {
        super(
                String.format(
                        "Unexpected infix operator '%s' in column %d:\n%s\n%s",
                        input.substring(pos, i),
                        pos,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

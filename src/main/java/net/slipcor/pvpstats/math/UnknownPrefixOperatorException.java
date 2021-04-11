package net.slipcor.pvpstats.math;

class UnknownPrefixOperatorException extends PointingException {
    UnknownPrefixOperatorException(String input, int pos, int i) {
        super(
                String.format(
                        "Unexpected prefix operator '%s' in column %d:\n%s\n%s",
                        input.substring(pos, i),
                        pos,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

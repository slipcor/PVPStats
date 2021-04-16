package net.slipcor.pvpstats.math;

class UnexpectedSymbolException extends PointingException {
    UnexpectedSymbolException(String input, int pos) {
        super(
                String.format(
                        "Unexpected symbol '%s' in column %d:\n%s\n%s",
                        input.substring(pos, pos + 1),
                        pos + 1,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

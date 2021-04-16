package net.slipcor.pvpstats.math;

class InvalidNumberException extends PointingException {
    InvalidNumberException(String input, int pos, String part) {
        super(
                String.format(
                        "Invalid number '%s' starting in column %d:\n%s\n%s",
                        part,
                        pos + 1,
                        input,
                        arrow(pos, pos + part.length())
                )
        );
    }
}

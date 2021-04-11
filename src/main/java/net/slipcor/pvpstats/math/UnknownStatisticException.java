package net.slipcor.pvpstats.math;

class UnknownStatisticException extends PointingException {
    UnknownStatisticException(String input, int pos) {
        super(
                String.format(
                        "Unknown statistic '%s' in column %d:\n%s\n%s",
                        input.substring(pos, pos + 2),
                        pos + 1,
                        input,
                        arrow(pos + 1)
                )
        );
    }
}

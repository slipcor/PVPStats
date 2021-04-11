package net.slipcor.pvpstats.math;

public class UnexpectedStackSizeException extends IllegalArgumentException {
    UnexpectedStackSizeException(int size) {
        super(
                String.format(
                        "Unexpected Stack Size: %d",
                        size
                )
        );
    }
}

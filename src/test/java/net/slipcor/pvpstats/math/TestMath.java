package net.slipcor.pvpstats.math;

import net.slipcor.pvpstats.classes.PlayerStatistic;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestMath {
    private static MathFormulaManager parser = MathFormulaManager.getInstance();

    @Test
    public void testSimpleNumbers() {
        assertEquals(0.0, parser.parse("0").evaluate(null), 0.1);
        assertEquals(1.0, parser.parse("1").evaluate(null), 0.1);
        assertEquals(-1.0, parser.parse("-1").evaluate(null), 0.1);
        assertEquals(16.0, parser.parse("16").evaluate(null), 0.1);
        assertEquals(32.64, parser.parse("32.64").evaluate(null), 0.01);
        assertEquals(1200, parser.parse("1.2e3").evaluate(null), 0.1);
        assertEquals(0.024, parser.parse("2.4e-2").evaluate(null), 0.001);
    }

    @Test
    public void verifyExceptions() {
        assertThrows(UnexpectedSymbolException.class, () -> parser.parse(".1"));
        assertThrows(UnexpectedSymbolException.class, () -> parser.parse("1.0e1.2"));
        assertThrows(UnexpectedOperatorException.class, () -> parser.parse("1*1*"));
        assertThrows(UnexpectedOperatorException.class, () -> parser.parse("100*(*12)"));
        assertThrows(UnmatchedRightParenthesisException.class, () -> parser.parse("2^5)+4"));
        assertThrows(UnmatchedLeftParenthesisException.class, () -> parser.parse("((2^5)+4"));
        assertThrows(InvalidNumberException.class, () -> parser.parse("1.2e"));
        assertThrows(UnexpectedSymbolException.class, () -> parser.parse("2e2e2"));
        assertThrows(UnknownStatisticException.class, () -> parser.parse("(4*2)*&y"));
        assertThrows(UnexpectedSymbolException.class, () -> parser.parse("(&k*&d)*&"));
        assertThrows(UnexpectedSymbolException.class, () -> parser.parse("(&k*&)/&d"));
    }

    @Test
    public void verifySimpleFormulas() {
        assertEquals(-1200, parser.parse("100*(-12)").evaluate(null), 1);
        assertEquals(-1200, parser.parse("-100*12").evaluate(null), 1);
        assertEquals(-44, parser.parse("2-5*10+4").evaluate(null), 1);
        assertEquals(-42, parser.parse("(2-5)*(10+4)").evaluate(null), 1);
        assertEquals(36, parser.parse("(2^5)+4").evaluate(null), 1);
        assertEquals(-65, parser.parse("(-5^2)-(10*4)").evaluate(null), 1);
        assertEquals(-65, parser.parse("(-5^2)-10*4").evaluate(null), 1);
        assertEquals(-65, parser.parse("-5^2-10*4").evaluate(null), 1);
    }

    @Test
    public void verifyStatisticFormulas() {
        // killer has 16 kills, 8 max streak, 4 current streak, 2000 ELO score
        PlayerStatistic killerStatistic = new PlayerStatistic(
                "Killer", 16, 0, 8, 4, 2000, 0,
                new UUID(0xADDED, 0xC0FFEE)
        );

        // victim has 8 deaths, 1000 ELO score
        PlayerStatistic victimStatistic = new PlayerStatistic(
                "Victim",
                0, 8, 0, 0, 1000, 0,
                new UUID(0xDEAD, 0xD0D0)
        );

        String defaultKD = "&k/&d";
        Formula defaultFormula = parser.parse(defaultKD);

        // division by 0 results in 0
        assertEquals(16, defaultFormula.evaluate(killerStatistic));
        // 0 divided by anything results in 0
        assertEquals(0, defaultFormula.evaluate(victimStatistic));

        String safeKD = "&k/(&d+1)";
        Formula safeFormula = parser.parse(safeKD);

        assertEquals(16, safeFormula.evaluate(killerStatistic));
        assertEquals(0, safeFormula.evaluate(victimStatistic));

        String allValues = "&e + ((&k * &s) / (&m * (&d + 1)))";
        Formula fullFormula = parser.parse(allValues);

        assertEquals(2008.0, fullFormula.evaluate(killerStatistic));
        assertEquals(1000, fullFormula.evaluate(victimStatistic));
    }
}

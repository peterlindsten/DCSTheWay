import main.Utils.CoordinateUtils;
import main.models.DMSCoordinate;
import main.models.Hemisphere;
import main.models.Point;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinateTest {
    private static final List<Point> p = Arrays.asList(new Point("33.999999", "33.1234", "54.123", Hemisphere.NORTH, Hemisphere.EAST));

    @Test
    void shouldConvertDecimalToDMS() {
        var actual = CoordinateUtils.decimalToDMS(new BigDecimal("42.360123"));

        assertEquals(new DMSCoordinate(42, 21, 36), actual);
    }

    @Test
    void shouldRoundupSeconds() {
        var actual = CoordinateUtils.decimalToDMS(new BigDecimal("33.8999"));
        assertEquals(new DMSCoordinate(33, 54, 0), actual);
    }

    @Test
    void shouldRoundupSecondsAndMinutesDMS() {
        var actual = CoordinateUtils.decimalToDMS(new BigDecimal("33.999999"));
        assertEquals(new DMSCoordinate(34, 0, 0), actual);
    }

    @Test
    void dmmFormat() {
        var actual = CoordinateUtils.dcsToDmmFtPoints(p, "00.0000", "000", false);

        var first = actual.get(0);
        assertEquals("3359.9999", first.latitude());
    }

    @Test
    void dmmFormatShouldHandleRoundup() {
        var actual = CoordinateUtils.dcsToDmmFtPoints(p, "00.00", "000", false);
        var first = actual.get(0);
        assertEquals("3400.00", first.latitude());
    }
}

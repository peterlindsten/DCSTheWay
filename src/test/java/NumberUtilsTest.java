import main.Utils.NumberUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberUtilsTest {
    @Test
    void SingleReturnsSingle() {
        var actual = NumberUtils.getPositiveDigits(0);
        assertEquals(1, actual.size());
        assertEquals(0, actual.get(0));
    }

    @Test
    void Multiple() {
        var actual = NumberUtils.getPositiveDigits(130);
        assertEquals(3, actual.size());
        Assertions.assertIterableEquals(Arrays.asList(1, 3, 0), actual);
    }

    @Test
    void NegativeTurnsPositive() {
        var actual = NumberUtils.getPositiveDigits(-2);
        assertEquals(1, actual.size());
        assertEquals(2, actual.get(0));
    }
}

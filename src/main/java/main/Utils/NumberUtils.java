package main.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberUtils {
    public static List<Integer> getPositiveDigits(int number) {
        List<Integer> ret = new ArrayList<>();
        int absNumber = Math.abs(number);
        while (absNumber > 0) {
            ret.add(absNumber % 10);
            absNumber /= 10;
        }
        if (number == 0) {
            ret.add(0);
        }
        Collections.reverse(ret);
        return ret;
    }
}

package main.models;

import java.math.BigDecimal;

public record DMMCoordinate(int degrees, BigDecimal minutes) {
    public DMMCoordinate(int degrees, double i) {
        this(degrees, new BigDecimal(i));
    }
}

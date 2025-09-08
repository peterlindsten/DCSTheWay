package main.models;

public record DMSCoordinate(int degrees, int minutes, int seconds) {
    @Override
    public String toString() {
        return degrees + "\u00B0" + minutes + "\u2032" + seconds + '"';
    }
}

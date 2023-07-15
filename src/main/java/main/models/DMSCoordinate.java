package main.models;

public record DMSCoordinate(int degrees, int minutes, int seconds) {
    @Override
    public String toString() {
        return degrees + "d" + minutes + "'" + seconds + '"';
    }
}

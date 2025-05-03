package main.Waypoints.PlanesCommands;

import main.models.Point;
import org.json.JSONArray;

import java.util.List;

public abstract class Aircraft {
    protected double speedDenominator;

    public Aircraft() {
        this.speedDenominator = 1;
    }

    public Aircraft(int speed) {
        this.speedDenominator = speed / 100.0;
    }

    public String getCorrectedDelay(int delay) {
        // Add delay to 0-delay inputs if slowdown is desired
        if (delay == 0 && this.speedDenominator < 1) {
            delay = 1;
        }
        return Integer.toString((int) Math.ceil(delay / this.speedDenominator));
    }

    public abstract JSONArray getCommands(List<Point> dcsPoints);
}

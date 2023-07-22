package main.Waypoints.PlanesCommands;

import main.models.Point;
import org.json.JSONArray;

import java.util.List;

public interface Aircraft {
    JSONArray getCommands(List<Point> dcsPoints);
}

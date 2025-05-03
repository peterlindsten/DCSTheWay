package main.Waypoints;

import main.DCSconnection.PortListenerThread;
import main.DCSconnection.PortSender;
import main.UI.GUI;
import main.Waypoints.PlanesCommands.*;
import main.models.Hemisphere;
import main.models.Point;

import java.util.ArrayList;

public class WaypointManager {
    static private final ArrayList<Point> waypoints = new ArrayList<>();

    public static void transfer(int speed) {
        String model = PortListenerThread.getPlaneModel();

        if (model != null && !waypoints.isEmpty()) {
            Aircraft a = null;
            switch (model) {
                case "F-16C_50" -> a = new F16(speed);
                case "FA-18C_hornet" -> a = new F18(speed);
                case "A-10C_2", "A-10C" -> a = new A10CII(speed);
                case "M-2000C" -> a = new M2000(speed);
                case "AV8BNA" -> a = new AV8BNA(speed);
                case "Ka-50" -> {
                    if (waypoints.size() > 6) {
                        GUI.error("The Ka-50 can store a maximum of 6 waypoints. ");
                    } else {
                        a = new Ka50(speed);
                    }
                }
                case "AH-64D_BLK_II" -> a = new AH64(speed);
                case "AJS37" -> a = new AJS37(speed);
                case "F-15ESE" -> a = new F15E(speed);
                case "CH-47Fbl1" -> a = new CH47F(speed);
                case "OH58D" -> a = new OH58D(speed);
                default -> GUI.error("Unsupported module: " + model);
            }
            if (null != a) {
                PortSender.send(a.getCommands(waypoints).toString());
            }
        }
    }

    public static boolean saveWaypointSuccessful() {
        String latitude = PortListenerThread.getLatitude();
        String longitude = PortListenerThread.getLongitude();
        String elevation = PortListenerThread.getElevation();
        if (latitude != null && longitude != null && elevation != null) {
            Hemisphere latHem;
            Hemisphere longHem;
            if (latitude.contains("-")) {
                latHem = Hemisphere.SOUTH;
            } else {
                latHem = Hemisphere.NORTH;
            }
            if (longitude.contains("-")) {
                longHem = Hemisphere.WEST;
            } else {
                longHem = Hemisphere.EAST;
            }
            var point = new Point(latitude.replace("-", ""), longitude.replace("-", ""), elevation, latHem, longHem);
            waypoints.add(point);
            return true;
        }
        return false;
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static int getSelectedWaypointsCount() {
        return waypoints.size();
    }
}

package main.Waypoints;

import main.DCSconnection.PortListenerThread;
import main.DCSconnection.PortSender;
import main.UI.GUI;
import main.Waypoints.PlanesCommands.*;
import main.models.Hemisphere;
import main.models.Point;

import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    static private final ArrayList<Point> waypoints = new ArrayList<>();

    public static void transfer() {
        String model = PortListenerThread.getPlaneModel();

        if (model != null && !waypoints.isEmpty()) {
            Aircraft a = null;
            switch (model) {
                case "F-16C_50" -> a = new F16();
                case "FA-18C_hornet" -> {
                    GUI.warning("""
                            Please make sure that:\s
                            1. PRECISE option is boxed in HSI > DATA
                            2. You are not in the TAC menu
                            3. You are in the 00°00.0000' coordinate format""");
                    ArrayList<Point> f18Coords = F18.getCoords(waypoints);
                    String dataToSend = F18.getCommands(f18Coords).toString();
                    PortSender.send(dataToSend);
                }
                case "A-10C_2", "A-10C" -> a = new A10CII();
                case "M-2000C" -> {
                    List<Point> m2000Coords = M2000.getCoords(waypoints);
                    String dataToSend = M2000.getCommands(m2000Coords).toString();
                    PortSender.send(dataToSend);
                }
                case "AV8BNA" -> {
                    List<Point> av8bnaCoords = AV8BNA.getCoords(waypoints);
                    String dataToSend = AV8BNA.getCommands(av8bnaCoords).toString();
                    PortSender.send(dataToSend);
                }
                case "Ka-50" -> {
                    if (waypoints.size() > 6) {
                        GUI.error("The Ka-50 can store a maximum of 6 waypoints. ");
                    } else {
                        List<Point> Ka50Coords = Ka50.getCoords(waypoints);
                        String dataToSend = Ka50.getCommands(Ka50Coords).toString();
                        PortSender.send(dataToSend);
                    }
                }
                case "AH-64D_BLK_II" -> a = new AH64();
                case "AJS37" -> a = new AJS37();
                case "F-15ESE" -> a = new F15E();
                default -> GUI.error("You are not flying a supported module.");
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

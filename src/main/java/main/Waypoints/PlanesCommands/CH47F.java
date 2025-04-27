package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CH47F implements Aircraft {
    private static final String[] tenKeys = new String[]{
            "3010", // 0
            "3011", // 1
            "3012", // 2
            "3013", // 3
            "3014", // 4
            "3015", // 5
            "3016", // 6
            "3017", // 7
            "3018", // 8
            "3019", // 9
    };
    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        List<Point> coords = CH47F.getCoords(dcsPoints);
        JSONArray commandArray = new JSONArray();
        /*
        CDU copilot = 2
        CDU pilot = 3

        3071 - FPLN
        3008 - CLR
        3050 - LSK1
        ...
        3055 - LSK6
        3056 - RSK1
        ...
        3061 - RSK6
        3010 - 0
        3011 - 1
        3019 - 9
        3020 - .
        3027 - E
        3036 - N
        3041 - S
        3045 - W
         */
        commandArray.put(deviceCodeDelay("3071")); // FPLN
        commandArray.put(deviceCodeDelay("3008")); // CLR
        // We loop backwards, since we push coords onto the front of the flightplan
        // this makes the order you selected the coords in match the order in the flightplan
        for (int i = coords.size()-1; i >= 0; i--) {
            // Lat
            if (coords.get(i).latitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(deviceCodeDelay("3036"));
            } else {
                commandArray.put(deviceCodeDelay("3041"));
            }
            enterDigits(coords.get(i).latitude(), commandArray);

            // Long
            if (coords.get(i).longitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(deviceCodeDelay("3027"));
            } else {
                commandArray.put(deviceCodeDelay("3045"));
            }
            enterDigits(coords.get(i).longitude(), commandArray);

            // FPLN pos (top)
            commandArray.put(deviceCodeDelay("3051"));
        }
        return commandArray;
    }

    private static void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            if (digit == '.') {
                commandArray.put(deviceCodeDelay("3020"));
            } else {
                commandArray.put(deviceCodeDelay(tenKeys[Character.getNumericValue(digit)]));
            }
        }
    }

    private static JSONObject deviceCodeDelay(String code) {
        return new JSONObject().put("device", "3").put("code", code).put("delay", "5").put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.000", "00", false);
    }
}

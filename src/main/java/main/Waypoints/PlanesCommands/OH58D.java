package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class OH58D extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3062", // 0
        "3053", // 1
        "3054", // 2
        "3055", // 3
        "3056", // 4
        "3057", // 5
        "3058", // 6
        "3059", // 7
        "3060", // 8
        "3061", // 9
    };

    public OH58D(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        List<Point> coords = OH58D.getCoords(dcsPoints);
        GUI.warning("""
            Please make sure that\s
            You are in LAT/LON mode, Not UTM""");
        JSONArray commandArray = new JSONArray();
        /*
        MFD copilot = 23
        MFD pilot = 11

        3008 - VSD
        3009 - HSD
        3001 - L1
        ...
        3005 - L5
        3013 - R1
        ...
        3017 - R5

        Keyboard = 14
        3062 - 0
        3053 - 1
        3061 - 9
        3090 - .
        3067 - E
        3076 - N
        3081 - S
        3085 - W
        3089 - Enter
        3091 - Clear
         */
        commandArray.put(deviceCode("3009", "11")); // HSD
        commandArray.put(deviceCode("3014", "11")); // R2
        commandArray.put(deviceCode("3004", "11")); // L4
        for (Point coord : coords) {
            commandArray.put(deviceCodeDelay("3002", "11", 20)); // L2
            commandArray.put(deviceCodeDelay("3091", "14", 20)); // Clear
            // Lat
            if (coord.latitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(deviceCode("3076", "14"));
            } else {
                commandArray.put(deviceCode("3081", "14"));
            }
            enterDigits(coord.latitude(), commandArray);
            commandArray.put(deviceCodeDelay("3089", "14", 10)); // Enter

            commandArray.put(deviceCodeDelay("3003", "11", 20)); // L3
            commandArray.put(deviceCodeDelay("3091", "14", 20)); // Clear
            // Long
            if (coord.longitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(deviceCode("3067", "14"));
            } else {
                commandArray.put(deviceCode("3085", "14"));
            }
            enterDigits(coord.longitude(), commandArray);
            commandArray.put(deviceCodeDelay("3089", "14", 10)); // Enter

            commandArray.put(deviceCode("3004", "11")); // L4
            commandArray.put(deviceCode("3091", "14")); // Clear
            // Elevation
            enterDigits(coord.elevation(), commandArray);
            commandArray.put(deviceCodeDelay("3089", "14", 10)); // Enter

            commandArray.put(deviceCode("3017", "11")); // R5 - Store
        }
        commandArray.put(deviceCode("3008", "11")); // Exit nav setup
        return commandArray;
    }

    private void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(deviceCode(tenKeys[Character.getNumericValue(digit)], "14"));
        }
    }

    private JSONObject deviceCode(String code, String device) {
        return deviceCodeDelay(code, device, 5);
    }

    private JSONObject deviceCodeDelay(String code, String device, int delay) {
        return new JSONObject()
            .put("device", device)
            .put("code", code)
            .put("delay", getCorrectedDelay(delay))
            .put("activate", "1")
            .put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.00", "00");
    }
}

package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.Utils.NumberUtils;
import main.models.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class F15E extends Aircraft {

    private static final String[] tenKeys = new String[]{
        "3036", // 0
        "3020", // 1/A
        "3021", // 2/N
        "3022", // 3/B
        "3025", // 4/W
        "3026", // 5/M
        "3027", // 6/E
        "3030", // 7/:
        "3031", // 8/S
        "3032", // 9/C
    };
    private static final String SHIFT = "3033";

    public F15E(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        F15EOptions options = GUI.f15eDialog();
        List<Point> coords = F15E.getCoords(dcsPoints);
        /*

        UFC Pilot 56
        UFC WSO 57

        Clear 3035
        Menu 3038

        PB1 3001
        PB2 3002 - Lat
        PB3 3003 - Long
        PB7 3007 - Elev
        PB10 3010

         */

        System.out.println("f15 opts: " + options.toString());

        String device = options.pilot() ? "56" : "57";
        String seriesButton = switch (options.series()) {
            case "A" -> tenKeys[1];
            case "B" -> tenKeys[3];
            case "C" -> tenKeys[9];
            default -> throw new IllegalStateException("Unexpected series value: " + options.series());
        };

        JSONArray commandArray = new JSONArray();

        // Clear x5 - Ensure scratchpad is clear & we're not displaying any menu/data
        commandArray.put(deviceCodeDelay(device, "3035"));
        commandArray.put(deviceCodeDelay(device, "3035"));
        commandArray.put(deviceCodeDelay(device, "3035"));
        commandArray.put(deviceCodeDelay(device, "3035"));
        commandArray.put(deviceCodeDelay(device, "3035"));
        // Menu
        commandArray.put(deviceCodeDelay(device, "3038"));
        // Steerpoint
        commandArray.put(deviceCodeDelay(device, "3010"));
        for (int i = 0, current = options.first(); i < coords.size(); i++, current++) {
            // Point to modify/create
            for (int digit : NumberUtils.getPositiveDigits(current)) {
                commandArray.put(deviceCodeDelay(device, tenKeys[digit]));
            }
            commandArray.put(deviceCodeDelay(device, SHIFT));
            commandArray.put(deviceCodeDelay(device, seriesButton));
            commandArray.put(deviceCodeDelay(device, "3001"));

            // Long
            commandArray.put(deviceCodeDelay(device, SHIFT));
            if (coords.get(i).longitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(deviceCodeDelay(device, tenKeys[6]));
            } else {
                commandArray.put(deviceCodeDelay(device, tenKeys[4]));
            }
            enterDigits(coords.get(i).longitude(), commandArray, device);
            commandArray.put(deviceCodeDelay(device, "3003"));

            // Lat
            commandArray.put(deviceCodeDelay(device, SHIFT));
            if (coords.get(i).latitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(deviceCodeDelay(device, tenKeys[2]));
            } else {
                commandArray.put(deviceCodeDelay(device, tenKeys[8]));
            }
            enterDigits(coords.get(i).latitude(), commandArray, device);
            commandArray.put(deviceCodeDelay(device, "3002"));

            // Elev
            enterDigits(coords.get(i).elevation(), commandArray, device);
            commandArray.put(deviceCodeDelay(device, "3007"));

        }
        return commandArray;
    }

    private void enterDigits(String coords, JSONArray commandArray, String device) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(deviceCodeDelay(device, tenKeys[Character.getNumericValue(digit)]));
        }
    }

    private JSONObject deviceCodeDelay(String device, String code) {
        return new JSONObject().put("device", device).put("code", code).put("delay", getCorrectedDelay(5)).put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.000", true);
    }
}

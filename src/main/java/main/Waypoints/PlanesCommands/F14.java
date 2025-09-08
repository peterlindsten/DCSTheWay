package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class F14 extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3535", // 0/BRG
        "3536", // 1/LAT
        "3537", // 2/NBR
        "3538", // 3/SPD
        "3539", // 4/ALT
        "3540", // 5/RNG
        "3541", // 6/LONG
        "3542", // 7
        "3543", // 8/HDG
        "3544", // 9
    };

    private static final String[] capKeys = new String[]{
        "3518",
        "3519",
        "3520",
        "3521", // Fix PT
        "3522", // IP
        "3526", // Surf tgt
    };

    public F14(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        List<Point> coords = getCoords(dcsPoints);
        int offset = getArgs();
        /*

        Supports 6 waypoints, 1-3, fix pt, IP, surf tgt
        F-14 only supports tenth of a minute precision

        CAP 24

        Category 3530 (Rotary) 0.6 Tac data
        Clear 3531
        S/W 3532
        N/E 3533
        Enter 3534

         */
        JSONArray commandArray = new JSONArray();
        //commandArray.put(new JSONObject().put("device", "24").put("code", "3530").put("delay", getCorrectedDelay(150)).put("activate", "0.4").put("addDepress", "false"));
        // Set cap category to Tac data
        commandArray.put(new JSONObject().put("device", "24").put("code", "3530").put("delay", getCorrectedDelay(150)).put("activate", "0.6").put("addDepress", "false"));

        int maxIter = Math.min(6 - offset, coords.size());
        for (int i = 0; i < maxIter; i++) {
            // Select waypoint, clear prefix
            commandArray.put(capCode(capKeys[i + offset]));
            commandArray.put(capCode("3531"));

            Point current = coords.get(i);

            // Lat
            commandArray.put(capCode(tenKeys[1]));
            if (current.latitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(capCode("3533"));
            } else {
                commandArray.put(capCode("3532"));
            }
            enterDigits(current.latitude(), commandArray);
            commandArray.put(capCodeDelay("3534", 15)); // Enter

            // Long
            commandArray.put(capCode(tenKeys[6]));
            if (current.longitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(capCode("3533"));
            } else {
                commandArray.put(capCode("3532"));
            }
            enterDigits(current.longitude(), commandArray);
            commandArray.put(capCodeDelay("3534", 15)); // Enter

            // Elevation
            commandArray.put(capCode(tenKeys[4]));
            enterDigits(current.elevation(), commandArray);
            commandArray.put(capCodeDelay("3534", 15)); // Enter

        }

        return commandArray;
    }

    private JSONObject capCode(String code) {
        return capCodeDelay(code, 5);
    }
    private JSONObject capCodeDelay(String code, int delay) {
        return new JSONObject().put("device", "24").put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", "1").put("addDepress", "true");
    }

    private void enterDigits(String digits, JSONArray commandArray) {
        for (char digit : digits.toCharArray()) {
            commandArray.put(capCode(tenKeys[Character.getNumericValue(digit)]));
        }
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.0", true);
    }

    public static int getArgs() {
        String[] seq = new String[]{"1", "2", "3", "Fix", "IP", "Surf"};
        String choice = GUI.multiChoice("First in sequence?", seq);
        for (int i = 0; i < seq.length; i++) {
            if (choice.equals(seq[i])) {
                return i;
            }
        }
        return 0;
    }
}

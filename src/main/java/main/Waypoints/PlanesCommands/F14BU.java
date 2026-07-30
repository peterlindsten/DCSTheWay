package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class F14BU extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3001", // 0
        "3002", // 1
        "3003", // 2
        "3004", // 3
        "3005", // 4
        "3006", // 5
        "3007", // 6
        "3008", // 7
        "3009", // 8
        "3010", // 9
    };

    public F14BU(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        List<Point> coords = getCoords(dcsPoints);
        GUI.warning("""
            Please make sure that:\s
            1. You are in the (default) DMM format (00°00.000')
            2. You are in the (default) EXPAND flightplan list mode
            It is recommended to use MAN or OFLY waypoint advance mode""");
        /*

        Supports 50 wpt per flightplan
        F-14BU supports dmm with 3 decimals or dms with 2 decimals (Except in precision edit)

        CDNU 81

        0 - 3001
        1 - 3002
        2 - 3003
        A - 3011
        E - 3015
        N - 3024
        S - 3029
        W - 3033
        Z - 3036
        F1 - 3037
        F2 - 3038
        F3 - 3039
        F4 - 3040
        F5 - 3041
        F6 - 3042
        F7 - 3043
        MENU - 3044
        Arrow up - 3045
        Arrow down - 3046
        Arrow left - 3047
        Arrow right - 3048
        . - 3049
        RNAV - 3052
        CLEAR - 3054
        IDX - 3055
        FPLN - 3056
        LSK1 - 3060
        LSK2 - 3061
        LSK3 - 3062
        LSK4 - 3063
        RSK1 - 3064
        RSK2 - 3065
        RSK3 - 3066
        RSK4 - 3067

         */
        JSONArray commandArray = new JSONArray();
        // IDX, CLR (hold), FPLN, DOWN arrow
        commandArray.put(new JSONObject().put("device", "81").put("code", "3055").put("delay", getCorrectedDelay(10)).put("activate", "1").put("addDepress", "true"));
        commandArray.put(new JSONObject().put("device", "81").put("code", "3054").put("delay", getCorrectedDelay(150)).put("activate", "1").put("addDepress", "true"));
        commandArray.put(new JSONObject().put("device", "81").put("code", "3056").put("delay", getCorrectedDelay(10)).put("activate", "1").put("addDepress", "true"));
        commandArray.put(new JSONObject().put("device", "81").put("code", "3046").put("delay", getCorrectedDelay(10)).put("activate", "1").put("addDepress", "true"));

        // Loop in reverse
        // Enter Lat Lon
        // LSK1
        // Enter elevation
        // LSK2
        System.out.println("size: " + coords.size());
        for (int i = coords.size()-1; i >= 0; i--) {
            System.out.println("coords: " + coords.get(i));
            Point current = coords.get(i);

            // Lat
            if (current.latitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(cdnuCode("3024"));
            } else {
                commandArray.put(cdnuCode("3029"));
            }
            enterDigits(current.latitude(), commandArray);

            // Long
            if (current.longitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(cdnuCode("3015"));
            } else {
                commandArray.put(cdnuCode("3033"));
            }
            enterDigits(current.longitude(), commandArray);
            commandArray.put(cdnuCodeDelay("3060", 15)); // LSK1

            // Elevation
            enterDigits(current.elevation(), commandArray);
            commandArray.put(cdnuCodeDelay("3061", 15)); // LSK2
        }

        System.out.println("array: " + commandArray);
        return commandArray;
    }

    private JSONObject cdnuCode(String code) {
        return cdnuCodeDelay(code, 5);
    }
    private JSONObject cdnuCodeDelay(String code, int delay) {
        return new JSONObject().put("device", "81").put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", "1").put("addDepress", "true");
    }

    private void enterDigits(String digits, JSONArray commandArray) {
        for (char digit : digits.toCharArray()) {
            if (digit == '.') {
                commandArray.put(cdnuCode("3049"));
            } else {
                commandArray.put(cdnuCode(tenKeys[Character.getNumericValue(digit)]));
            }
        }
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.000", false);
    }
}

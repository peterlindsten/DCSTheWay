package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.Utils.NumberUtils;
import main.Utils.UnitConvertorUtils;
import main.models.DMSCoordinate;
import main.models.F15EOptions;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class F15E {

    private static final String[] tenKeys = new String[] {
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
    public static JSONArray getCommands(List<Point> coords, F15EOptions options) {
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
            if (coords.get(i).getLongitudeHemisphere() == Hemisphere.EAST) {
                commandArray.put(deviceCodeDelay(device, tenKeys[6]));
            } else {
                commandArray.put(deviceCodeDelay(device, tenKeys[4]));
            }
            for (char digit : coords.get(i).getLongitude().toCharArray()) {
                commandArray.put(deviceCodeDelay(device, tenKeys[Character.getNumericValue(digit)]));
            }
            commandArray.put(deviceCodeDelay(device, "3003"));

            // Lat
            commandArray.put(deviceCodeDelay(device, SHIFT));
            if (coords.get(i).getLatitudeHemisphere() == Hemisphere.NORTH) {
                commandArray.put(deviceCodeDelay(device, tenKeys[2]));
            } else {
                commandArray.put(deviceCodeDelay(device, tenKeys[8]));
            }
            for (char digit : coords.get(i).getLatitude().toCharArray()) {
                commandArray.put(deviceCodeDelay(device, tenKeys[Character.getNumericValue(digit)]));
            }
            commandArray.put(deviceCodeDelay(device, "3002"));

            // Elev
            for (char digit : coords.get(i).getElevation().toCharArray()) {
                commandArray.put(deviceCodeDelay(device, tenKeys[Character.getNumericValue(digit)]));
            }
            commandArray.put(deviceCodeDelay(device, "3007"));

        }
        return commandArray;
    }

    private static JSONObject deviceCodeDelay(String device, String code) {
        return new JSONObject().put("device", device).put("code", code).put("delay", "5").put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> f15ePoints = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            BigDecimal dcsLat = new BigDecimal(dcsPoint.getLatitude());
            BigDecimal dcsLong = new BigDecimal(dcsPoint.getLongitude());
            Double dcsElev = Double.parseDouble(dcsPoint.getElevation());

            DMSCoordinate dmsLat = CoordinateUtils.decimalToDMS(dcsLat);
            DMSCoordinate dmsLong = CoordinateUtils.decimalToDMS(dcsLong);

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat("00");
            DecimalFormat latSecDf = new DecimalFormat("00");
            DecimalFormat longDegDf = new DecimalFormat("000");
            DecimalFormat longMinDf = new DecimalFormat("00");
            DecimalFormat longSecDf = new DecimalFormat("00");
            String f15eLat = latDegDf.format(dmsLat.getDegrees()) + latMinDf.format(dmsLat.getMinutes()) + latSecDf.format(dmsLat.getSeconds());
            String f15eLong = longDegDf.format(dmsLong.getDegrees()) + longMinDf.format(dmsLong.getMinutes()) + longSecDf.format(dmsLong.getSeconds());
            String f15eElev = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var f15ePoint = new Point(f15eLat, f15eLong, f15eElev, dcsPoint.getLatitudeHemisphere(), dcsPoint.getLongitudeHemisphere());
            f15ePoints.add(f15ePoint);
        }
        return f15ePoints;
    }
}

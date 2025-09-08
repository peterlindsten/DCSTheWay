package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.Utils.UnitConvertorUtils;
import main.models.DMSCoordinate;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JF17 extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3224", // 0
        "3202", // 1/PFL
        "3203", // 2/VRC
        "3204", // 3
        "3209", // 4/DST
        "3210", // 5/TOT
        "3211", // 6/TOD
        "3216", // 7/FUL
        "3217", // 8/IFF
        "3218", // 9
    };

    private static final Map<String, String> ufc = Map.of(
        "L1", "3205",
        "R1", "3206",
        "L2", "3212",
        "R2", "3213",
        "L3", "3219",
        "R3", "3220",
        "RTN", "3223",
        "L4", "3226",
        "R4", "3227"
    );

    private static final String LmpdL2 = "3245";
    private final JSONObject next = new JSONObject().put("device", "47").put("code", LmpdL2).put("delay", getCorrectedDelay(5)).put("activate", "1").put("addDepress", "true");

    public JF17(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        var coords = getCoords(dcsPoints);
        // UFCP device: 46
        // MFCD device: 47
        JSONArray commandArray = new JSONArray();
        commandArray.put(ufcCode(ufc.get("RTN")));
        commandArray.put(ufcCode(ufc.get("RTN")));
        commandArray.put(ufcCode(ufc.get("RTN")));
        commandArray.put(ufcCode(tenKeys[4])); // DST

        for (Point coordinate : coords) {
            commandArray.put(next);
            // Lat
            commandArray.put(ufcCode(ufc.get("L2")));
            enterDigits(coordinate.latitude(), commandArray);
            commandArray.put(ufcCode(ufc.get("L2")));
            // Hemisphere?

            // Lon
            commandArray.put(ufcCode(ufc.get("L3")));
            enterDigits(coordinate.longitude(), commandArray);
            commandArray.put(ufcCode(ufc.get("L3")));
            // Hemisphere?

            // Elev
            commandArray.put(ufcCode(ufc.get("R4")));
            enterDigits(coordinate.elevation(), commandArray);
            commandArray.put(ufcCode(ufc.get("R4")));
        }
        return commandArray;
    }

    private JSONObject ufcCode(String code) {
        return new JSONObject().put("device", "46").put("code", code).put("delay", getCorrectedDelay(1)).put("activate", "1").put("addDepress", "true");
    }

    private void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(ufcCode(tenKeys[Character.getNumericValue(digit)]));
        }
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> points = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            BigDecimal dcsLat = new BigDecimal(dcsPoint.latitude());
            BigDecimal dcsLong = new BigDecimal(dcsPoint.longitude());
            Double dcsElev = Double.parseDouble(dcsPoint.elevation());

            DMSdCoordinate dmsdLat = decimalToDMSd(dcsLat);
            DMSdCoordinate dmsdLong = decimalToDMSd(dcsLong);

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat("00");
            DecimalFormat latSecDf = new DecimalFormat("00.0");
            DecimalFormat longDegDf = new DecimalFormat("000");
            DecimalFormat longMinDf = new DecimalFormat("00");
            DecimalFormat longSecDf = new DecimalFormat("00.0");
            DecimalFormat elevFormatDf = new DecimalFormat("00000");
            String lat = latDegDf.format(dmsdLat.degrees()) + latMinDf.format(dmsdLat.minutes()) + latSecDf.format(dmsdLat.seconds()).replace(".", "");
            String lon = longDegDf.format(dmsdLong.degrees()) + longMinDf.format(dmsdLong.minutes()) + longSecDf.format(dmsdLong.seconds()).replace(".", "");
            String elev = elevFormatDf.format(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var point = new Point(lat, lon, elev, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            points.add(point);
        }
        return points;
    }

    private static DMSdCoordinate decimalToDMSd(BigDecimal decimalCoordinate) {
        int degrees = Integer.parseInt(decimalCoordinate.toBigInteger().toString());
        BigDecimal minutesDecimal = decimalCoordinate.remainder(BigDecimal.ONE).multiply(new BigDecimal(60));
        int minutes = Integer.parseInt(minutesDecimal.toBigInteger().toString());
        BigDecimal secondsDecimal = minutesDecimal.remainder(BigDecimal.ONE).multiply(new BigDecimal(60));
        float seconds = Float.parseFloat(secondsDecimal.setScale(1, RoundingMode.HALF_UP).toBigInteger().toString());
        if (seconds >= 60.0f) {
            minutes++;
            seconds = 0;
        }
        if (minutes == 60) {
            degrees++;
            minutes = 0;
        }
        return new DMSdCoordinate(degrees, minutes, seconds);
    }
}

record DMSdCoordinate(int degrees, int minutes, float seconds) {
}

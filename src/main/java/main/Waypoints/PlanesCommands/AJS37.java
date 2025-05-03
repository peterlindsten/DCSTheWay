package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.Utils.UnitConvertorUtils;
import main.models.DMSCoordinate;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AJS37 extends Aircraft {
    public AJS37(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        var coords = getCoords(dcsPoints);
        int offset = getArgs();
        /*
        The AJS37 has inherent limitations in its coordinate entry
        Only 6 digits are allowed for either Lat/Long, and there is no provision for hemispheres
        The game module "guesses" which hemisphere is meant as well as if there should be a 1 in front of the long value
        Maps that cross hemispheres (Normandy) or the 100 degree line (No maps currently), has unknown support

        NAVIGATIONPANEL 23 - Dataselector, in/out
        Actions
        Dataselector: 3009, 0 - 0.7, .5 = REF/LOLA, .6 = AKT/POS
        In/out: 3008, 1 = In, 0 = Out


        NAVIGATION 12 - Data input
        Actions
        Data input, 0-9, 3020 - 3029
        Nav selector, B1-B9, 3011 - 3019
        Nav selector, BX, 3010
        Nav selector, LS, 3009
        Nav selector, L MÅL, 3008


         */
        JSONArray commandArray = new JSONArray();
        // REF/LOLA
        commandArray.put(deviceCodeDelayActivateDepress("23", "3009", "0.5", "false"));
        // IN
        commandArray.put(deviceCodeDelayActivateDepress("23", "3008", "1", "false"));
        // Start entry
        for (int i = 0; i < coords.size() && i < (9 - offset); i++) {
            for (char digit : coords.get(i).longitude().toCharArray()) {
                // Digits
                commandArray.put(deviceCodeDelayActivateDepress("12", "302" + digit, "1", "true"));
            }
            for (char digit : coords.get(i).latitude().toCharArray()) {
                // Digits
                commandArray.put(deviceCodeDelayActivateDepress("12", "302" + digit, "1", "true"));
            }
            // B1-B9
            commandArray.put(deviceCodeDelayActivateDepress("12", "301" + (i + 1 + offset), "1", "true"));
        }
        // AKT/POS
        commandArray.put(deviceCodeDelayActivateDepress("23", "3009", "0.6", "false"));
        // OUT
        commandArray.put(deviceCodeDelayActivateDepress("23", "3008", "0", "false"));
        return commandArray;
    }

    private JSONObject deviceCodeDelayActivateDepress(String device, String code, String activate, String depress) {
        return new JSONObject().put("device", device).put("code", code).put("delay", getCorrectedDelay(1)).put("activate", activate).put("addDepress", depress);
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> ajs37points = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            BigDecimal dcsLat = new BigDecimal(dcsPoint.latitude());
            BigDecimal dcsLong = new BigDecimal(dcsPoint.longitude());
            Double dcsElev = Double.parseDouble(dcsPoint.elevation());

            DMSCoordinate dmsLat = CoordinateUtils.decimalToDMS(dcsLat);
            DMSCoordinate dmsLong = CoordinateUtils.decimalToDMS(dcsLong);

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat("00");
            DecimalFormat latSecDf = new DecimalFormat("00");
            DecimalFormat longDegDf = new DecimalFormat("000");
            DecimalFormat longMinDf = new DecimalFormat("00");
            DecimalFormat longSecDf = new DecimalFormat("00");
            String ajs37Latitude = latDegDf.format(dmsLat.degrees()) + latMinDf.format(dmsLat.minutes()) + latSecDf.format(dmsLat.seconds());
            String ajs37Longitude = longDegDf.format(dmsLong.degrees()).substring(1) + longMinDf.format(dmsLong.minutes()) + longSecDf.format(dmsLong.seconds());
            String ajs37Elevation = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var ajs37Point = new Point(ajs37Latitude, ajs37Longitude, ajs37Elevation, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            ajs37points.add(ajs37Point);
        }
        return ajs37points;
    }

    public static int getArgs() {
        return Integer.parseInt(GUI.multiChoice("First in sequence?",
            new String[]{"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9"}).substring(1)) - 1;
    }
}

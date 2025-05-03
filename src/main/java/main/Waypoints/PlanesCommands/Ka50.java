package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.Utils.UnitConvertorUtils;
import main.models.DMMCoordinate;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Ka50 extends Aircraft {
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

    public Ka50(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> waypoints) {
        List<Point> coords = getCoords(waypoints);
        /*
           button list, all are device 20
           Waypoint button 	3011
		   Enter 			3018
		   PVI mode ENT 	3026 rotary value 0.2
           PVI Mode OPER 	3026 rotary value 0.3

           0/+     3001 +for north and east
           1/-     3002 -for south and west
           2       3003
           3       3004
           4       3005
           5       3006
           6       3007
           7       3008
           8       3009
           9       3010

           */

        JSONArray commandArray = new JSONArray();

        //PVI to Entry mode
        commandArray.put(codeActivateDepress("3026", "0.2", "false"));
        //Press waypoint button
        commandArray.put(codeActivateDepress("3011", "1", "true"));
        for (int i = 1; i <= coords.size(); i++) {
            //Press the corresponding waypoint number
            commandArray.put(codeActivateDepress(tenKeys[i], "1", "true"));
            //Check if latitude is N or S
            if (coords.get(i - 1).latitudeHemisphere() == Hemisphere.NORTH) {
                //press 0/+ for North
                commandArray.put(codeActivateDepress("3001", "1", "true"));
            } else {
                //press 1/- for South
                commandArray.put(codeActivateDepress("3002", "1", "true"));
            }
            //Start typing latitude
            enterDigits(coords.get(i - 1).latitude(), commandArray);
            //Check if longitude is E or W
            if (coords.get(i - 1).longitudeHemisphere() == Hemisphere.EAST) {
                //press 0/+ for East
                commandArray.put(codeActivateDepress("3001", "1", "true"));
            } else {
                //press 1/- for West
                commandArray.put(codeActivateDepress("3002", "1", "true"));
            }
            //Start typing longitude
            enterDigits(coords.get(i - 1).longitude(), commandArray);
            //Press Enter
            commandArray.put(codeActivateDepress("3018", "1", "true"));
        }
        //PVI to OPER
        commandArray.put(codeActivateDepress("3026", "0.3", "false"));

        return commandArray;
    }

    private void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(codeActivateDepress(tenKeys[Character.getNumericValue(digit)], "1", "true"));
        }
    }

    public JSONObject codeActivateDepress(String code, String activate, String addDepress) {
        return new JSONObject()
            .put("device", "20")
            .put("code", code)
            .put("delay", getCorrectedDelay(0))
            .put("activate", activate)
            .put("addDepress", addDepress);
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> Ka50Points = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            BigDecimal dcsLat = new BigDecimal(dcsPoint.latitude());
            BigDecimal dcsLong = new BigDecimal(dcsPoint.longitude());
            Double dcsElev = Double.parseDouble(dcsPoint.elevation());

            DMMCoordinate dmmLat = CoordinateUtils.decimalToDMM(dcsLat);
            DMMCoordinate dmmLong = CoordinateUtils.decimalToDMM(dcsLong);

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat("00.0");
            DecimalFormat longDegDf = new DecimalFormat("000");
            DecimalFormat longMinDf = new DecimalFormat("00.0");
            String Ka50Latitude = latDegDf.format(dmmLat.degrees()) + latMinDf.format(dmmLat.minutes()).replace(".", "");
            String Ka50Longitude = longDegDf.format(dmmLong.degrees()) + longMinDf.format(dmmLong.minutes()).replace(".", "");
            String Ka50Elevation = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var Ka50Point = new Point(Ka50Latitude, Ka50Longitude, Ka50Elevation, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            Ka50Points.add(Ka50Point);
        }
        return Ka50Points;
    }
}

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

public class M2000 extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3593", // 0
        "3584", // 1
        "3585", // 2
        "3586", // 3
        "3587", // 4
        "3588", // 5
        "3589", // 6
        "3590", // 7
        "3591", // 8
        "3592", // 9
    };

    public M2000(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> waypoints) {
        List<Point> coords = M2000.getCoords(waypoints);
        /*
           button list, all are device 9

           INS Coord display 3574 value 0.4
           Next WP  3110
           Prep  3570

           1/Lat   3584
           2/N     3585
           3/Lon   3586
           4/W     3587
           5       3588
           6/E     3589
           7       3590
           8/S     3591
           9       3592
           0       3593
           ENTR    3596
         */

        JSONArray commandArray = new JSONArray();

        for (Point coordinate : coords) {
            //INS coordinate display select
            commandArray.put(codeActivateDepress("3574", "0.4", "false"));
            //increment steerpoint
            commandArray.put(buttonPush("3110"));
            //select Prep twice
            commandArray.put(buttonPush("3570"));
            commandArray.put(buttonPush("3570"));
            //goto lat field
            commandArray.put(buttonPush("3584"));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N
                commandArray.put(buttonPush("3585"));
            } else {
                //press S
                commandArray.put(buttonPush("3591"));
            }
            //start typing latitude
            enterDigits(coordinate.latitude(), commandArray);
            //press enter
            commandArray.put(buttonPush("3596"));
            //goto long field
            commandArray.put(buttonPush("3586"));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E
                commandArray.put(buttonPush("3589"));
            } else {
                //press W
                commandArray.put(buttonPush("3587"));
            }
            //start typing longitude
            enterDigits(coordinate.longitude(), commandArray);
//            //press enter
            commandArray.put(buttonPush("3596"));
//            //goto elevation field
            commandArray.put(codeActivateDepress("3574", "0.3", "false"));
            //select feet and positive elevatioon
            commandArray.put(buttonPush("3584"));
            commandArray.put(buttonPush("3584"));

//            //start entering elevation
            enterDigits(coordinate.elevation(), commandArray);
//            press enter
            commandArray.put(buttonPush("3596"));
        }

        return commandArray;
    }

    private void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(buttonPush(tenKeys[Character.getNumericValue(digit)]));
        }
    }

    public JSONObject buttonPush(String code) {
        return codeActivateDepress(code, "1", "true");
    }

    public JSONObject codeActivateDepress(String code, String activate, String addDepress) {
        return new JSONObject()
            .put("device", "9")
            .put("code", code)
            .put("delay", getCorrectedDelay(10))
            .put("activate", activate)
            .put("addDepress", addDepress);
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> m2000Points = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            BigDecimal dcsLat = new BigDecimal(dcsPoint.latitude());
            BigDecimal dcsLong = new BigDecimal(dcsPoint.longitude());
            Double dcsElev = Double.parseDouble(dcsPoint.elevation());

            DMMCoordinate dmmLat = CoordinateUtils.decimalToDMM(dcsLat);
            DMMCoordinate dmmLong = CoordinateUtils.decimalToDMM(dcsLong);

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat("00.000");
            DecimalFormat longDegDf = new DecimalFormat("000");
            DecimalFormat longMinDf = new DecimalFormat("00.000");
            String m2000Latitude = latDegDf.format(dmmLat.degrees()) + latMinDf.format(dmmLat.minutes()).replace(".", "");
            String m2000Longitude = longDegDf.format(dmmLong.degrees()) + longMinDf.format(dmmLong.minutes()).replace(".", "");
            String m2000Elevation = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var m2000Point = new Point(m2000Latitude, m2000Longitude, m2000Elevation, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            m2000Points.add(m2000Point);
        }
        return m2000Points;
    }
}

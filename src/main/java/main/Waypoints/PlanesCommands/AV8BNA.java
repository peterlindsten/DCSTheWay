package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.Utils.UnitConvertorUtils;
import main.models.DMSCoordinate;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AV8BNA extends Aircraft {
    private static final String[] tenKeys = new String[]{
        "3315", // 0
        "3302", // 1
        "3303", // 2
        "3304", // 3
        "3306", // 4
        "3307", // 5
        "3308", // 6
        "3310", // 7
        "3311", // 8
        "3312", // 9
    };

    public AV8BNA(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> waypoints) {
        List<Point> coords = AV8BNA.getCoords(waypoints);
        /*
           Button List ODU, device 24
		   ODU option 1 3250
		   ODU option 2 3251
		   ODU option 3 3252

		   Left MFD, device 26
		   OBS PB 2 3201
		   OBS PB 18 3217

		   UFC device 23
           1       3302
           2/N     3303
           3       3304
           4/W     3306
           5       3307
           6/E     3308
           7       3310
           8/S     3311
           9       3312
           0       3315
           ENT     3314

           Master Modes Panel, device 12
           NAV Master mode 3282
         */

        JSONArray commandArray = new JSONArray();

        //Enter NAV master mode
        commandArray.put(deviceCodeDelay("12", "3282", 10));
        //Enter left MFD menu
        commandArray.put(deviceCodeDelay("26", "3217", 10));
        //Select EHSD
        commandArray.put(deviceCodeDelay("26", "3201", 10));
        //Select DATA sub menu
        commandArray.put(deviceCodeDelay("26", "3201", 10));
        for (Point coordinate : coords) {
            //Enter 99 to increment waypoint
            commandArray.put(ufcPush("3312"));
            commandArray.put(ufcPush("3312"));
            //Press ENT
            commandArray.put(deviceCodeDelay("23", "3314", 30));
            //Select ODU option 2 to enter latitude
            commandArray.put(deviceCodeDelay("24", "3251", 10));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N
                commandArray.put(ufcPush("3303"));
            } else {
                //press S
                commandArray.put(ufcPush("3311"));
            }
            //start typing latitude
            enterDigits(coordinate.latitude(), commandArray);
            //Press ENT
            commandArray.put(deviceCodeDelay("23", "3314", 30));
            //Select ODU option 2 to enter longitude -- removed
//            commandArray.put(new JSONObject().put("device", "24").put("code", "3251").put("delay", "10").put("activate", "1").put("addDepress", "true"));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E
                commandArray.put(ufcPush("3308"));
            } else {
                //press W
                commandArray.put(ufcPush("3306"));
            }
            //start typing longitude
            enterDigits(coordinate.longitude(), commandArray);
            //Press ENT
            commandArray.put(deviceCodeDelay("23", "3314", 30));
            //Select ODU option 2 to revert to latitude - removed
//            commandArray.put(new JSONObject().put("device", "24").put("code", "3251").put("delay", "10").put("activate", "1").put("addDepress", "true"));
            //Select ODU option 1 to revert to waypoint selection mode
            commandArray.put(deviceCodeDelay("24", "3250", 10));
        }
        //Deselect EHSD DATA sub menu
        commandArray.put(deviceCodeDelay("26", "3201", 10));

        return commandArray;
    }

    private void enterDigits(String coords, JSONArray commandArray) {
        for (char digit : coords.toCharArray()) {
            commandArray.put(ufcPush(tenKeys[Character.getNumericValue(digit)]));
        }
    }

    private JSONObject ufcPush(String code) {
        return deviceCodeDelay("23", code, 10);
    }

    public JSONObject deviceCodeDelay(String device, String code, int delay) {
        return new JSONObject()
            .put("device", device)
            .put("code", code)
            .put("delay", getCorrectedDelay(delay))
            .put("activate", "1")
            .put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        List<Point> av8bnaPoints = new ArrayList<>();
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
            String av8bnaLatitude = latDegDf.format(dmsLat.degrees()) + latMinDf.format(dmsLat.minutes()) + latSecDf.format(dmsLat.seconds());
            String av8bnaLongitude = longDegDf.format(dmsLong.degrees()) + longMinDf.format(dmsLong.minutes()) + longSecDf.format(dmsLong.seconds());
            String av8bnaElevation = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var av8bnaPoint = new Point(av8bnaLatitude, av8bnaLongitude, av8bnaElevation, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            av8bnaPoints.add(av8bnaPoint);
        }
        return av8bnaPoints;
    }
}

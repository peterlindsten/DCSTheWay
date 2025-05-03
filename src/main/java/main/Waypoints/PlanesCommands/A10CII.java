package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class A10CII extends Aircraft {
    public A10CII(int speed) {
        super(speed);
    }

    @Override
    public JSONArray getCommands(List<Point> dcsPoints) {
        var coords = getCoords(dcsPoints);
            /*
           button list, all are device 9
           LSK 3L  3001
           LSK 5L  3002
           LSK 7L  3003
           LSK 9L  3004
           LSK 3R  3005
           LSK 5R  3006
           LSK 7R  3007
           LSK 9R  3008

           WP MENU 3011

           1       3015
           2       3016
           3       3017
           4       3018
           5       3019
           6       3020
           7       3021
           8       3022
           9       3023
           0       3024
            N       3040
            S       3045
            E       3031
            W       3049

           */

        var commandArray = new JSONArray();


        //go to WP page
        commandArray.put(codeDelay("3011", 10));
        //goto WAYPOINT page
        commandArray.put(code("3005"));
        for (Point coordinate : coords) {
            //create new WP
            commandArray.put(code("3007"));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N
                commandArray.put(code("3040"));
            } else {
                //press S
                commandArray.put(code("3045"));
            }
            //start typing latitude
            for (char digit : coordinate.latitude().toCharArray()) {
                commandArray.put(digitCommand(digit));
            }
            //enter into field
            commandArray.put(code("3003"));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E
                commandArray.put(code("3031"));
            } else {
                //press W
                commandArray.put(code("3049"));
            }
            //start typing longitude
            for (char digit : coordinate.longitude().toCharArray()) {
                commandArray.put(digitCommand(digit));
            }
            //enter into field
            commandArray.put(code("3004"));
        }

        return commandArray;
    }

    private JSONObject digitCommand(char digit) {
        return digit == '0' ? code("3024") : code(Integer.toString(Character.getNumericValue(digit) + 3014));
    }

    private JSONObject code(String code) {
        return codeDelay(code, 0);
    }

    private JSONObject codeDelay(String code, int delay) {
        return new JSONObject().put("device", "9").put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.000", true);
    }
}

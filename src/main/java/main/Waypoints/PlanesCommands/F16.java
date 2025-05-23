package main.Waypoints.PlanesCommands;

import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class F16 extends Aircraft {
    public F16(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> dcsPoints) {
        List<Point> coords = getCoords(dcsPoints);
        /*
           button list, all are device 17
           DCS rtn   3032 -1
           DCS dn    3035 -1
           DCS up    3034 1
           rocker up 3031
           rocker dn 3030
           1         3003
           2/N       3004
           3         3005
           4/W       3006
           5         3007
           6/E       3008
           7         3009
           8/S       3010
           9         3011
           0         3002
           ENTR      3016
         */

        JSONArray commandArray = new JSONArray();

        //rtn to main page of DED
        commandArray.put(codeDelayActivate("3032", 30, "-1"));
        //goto STPT page
        commandArray.put(codeDelayActivate("3006", 1, "1"));
        for (Point coordinate : coords) {
            //increment steerpoint
            commandArray.put(codeDelayActivate("3030", 1, "1"));
            //goto lat field
            commandArray.put(codeDelayActivate("3035", 30, "-1"));
            commandArray.put(codeDelayActivate("3035", 30, "-1"));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N
                commandArray.put(codeDelayActivate("3004", 1, "1"));
            } else {
                //press S
                commandArray.put(codeDelayActivate("3010", 1, "1"));
            }
            //start typing latitude
            for (char digit : coordinate.latitude().toCharArray()) {
                enterDigit(commandArray, digit);
            }
            //press enter
            commandArray.put(codeDelayActivate("3016", 1, "1"));
            //goto long field
            commandArray.put(codeDelayActivate("3035", 30, "-1"));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E
                commandArray.put(codeDelayActivate("3008", 1, "1"));
            } else {
                //press W
                commandArray.put(codeDelayActivate("3006", 1, "1"));
            }
            //start typing longitude
            for (char digit : coordinate.longitude().toCharArray()) {
                enterDigit(commandArray, digit);
            }
            //press enter
            commandArray.put(codeDelayActivate("3016", 1, "1"));
            //goto elevation field
            commandArray.put(codeDelayActivate("3035", 30, "-1"));
            //start entering elevation
            for (char digit : coordinate.elevation().toCharArray()) {
                enterDigit(commandArray, digit);
            }
            //press enter
            commandArray.put(codeDelayActivate("3016", 1, "1"));
            //goto steerpoint field
            commandArray.put(codeDelayActivate("3034", 30, "1"));
            commandArray.put(codeDelayActivate("3034", 30, "1"));
            commandArray.put(codeDelayActivate("3034", 30, "1"));
            commandArray.put(codeDelayActivate("3034", 30, "1"));
        }
        //return to main page
        commandArray.put(codeDelayActivate("3032", 30, "-1"));

        return commandArray;
    }

    private void enterDigit(JSONArray commandArray, char digit) {
        commandArray.put(codeDelayActivate(Integer.toString(Character.getNumericValue(digit) + 3002), 0, "1"));
    }

    private JSONObject codeDelayActivate(String code, int delay, String activate) {
        return new JSONObject().put("device", "17").put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", activate).put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.000", true);
    }
}

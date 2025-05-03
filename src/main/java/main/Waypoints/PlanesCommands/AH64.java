package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class AH64 extends Aircraft {
    public AH64(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> dcsPoints) {
        boolean choice = GUI.choice("Are you in the pilot seat?", "Yes", "No, I am CPG")
            .equals("Yes");
        List<Point> coords = getCoords(dcsPoints);
        var commandArray = new JSONArray();

        String mfdDevice = choice ? "43" : "45";
        String keyDevice = choice ? "29" : "30";

        //Enter TSD Page
        commandArray.put(deviceCodeDelay(mfdDevice, "3029", 0));
        //goto Point page
        commandArray.put(deviceCodeDelay(mfdDevice, "3013", 0));

        for (Point coordinate : coords) {
            //press ADD
            commandArray.put(deviceCodeDelay(mfdDevice, "3023", 0));
            //press IDENT
            commandArray.put(deviceCodeDelay(mfdDevice, "3024", 10));
            //press ENTER twice
            commandArray.put(deviceCodeDelay(keyDevice, "3006", 10));
            commandArray.put(deviceCodeDelay(keyDevice, "3006", 10));
            //press CLR
            commandArray.put(deviceCodeDelay(keyDevice, "3001", 10));

            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N
                commandArray.put(deviceCodeDelay(keyDevice, "3020", 3));
            } else {
                //press S
                commandArray.put(deviceCodeDelay(keyDevice, "3025", 3));
            }
            //start typing latitude
            for (char digit : coordinate.latitude().toCharArray()) {
                commandArray.put(digitCommand(keyDevice, digit));
            }

            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E
                commandArray.put(deviceCodeDelay(keyDevice, "3011", 3));
            } else {
                //press W
                commandArray.put(deviceCodeDelay(keyDevice, "3029", 3));
            }
            //start typing longitude
            for (char digit : coordinate.longitude().toCharArray()) {
                commandArray.put(digitCommand(keyDevice, digit));
            }
            //press ENTER twice
            commandArray.put(deviceCodeDelay(keyDevice, "3006", 10));
            commandArray.put(deviceCodeDelay(keyDevice, "3006", 10));
        }
        return commandArray;
    }

    private JSONObject digitCommand(String device, char digit) {
        return digit == '0' ? deviceCodeDelay(device, "3043", 3)
            : deviceCodeDelay(device, Integer.toString(Character.getNumericValue(digit) + 3032), 3);
    }

    private JSONObject deviceCodeDelay(String device, String code, int delay) {
        return new JSONObject().put("device", device).put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.00", true);
    }
}

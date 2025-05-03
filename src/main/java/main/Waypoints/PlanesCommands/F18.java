package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class F18 extends Aircraft {
    static int instant = 10;
    int small = 10;
    int twenty = 20;
    static int thirty = 80;
    int forty = 40;

    public F18(int speed) {
        super(speed);
    }

    public JSONArray getCommands(List<Point> dcsPoints) {
        var coords = getCoords(dcsPoints);
        GUI.warning("""
            Please make sure that:\s
            1. PRECISE option is boxed in HSI > DATA
            2. You are not in the TAC menu
            3. You are in the 00°00.0000' coordinate format""");
        /*
            AMPCD stuff, device 37
            PB 18 - 3028
            PB 2  - 3012
            PB 10 - 3020
            PB 12 - 3022
            PB 5  - 3015
            PB 19 - 3029

            UFC stuff, device 25
            Select 1 - 3010
            Select 1 - 3012
            1 - 3019
            2 (north) - 3020
            3 - 3021
            4 (west) - 3022
            5 - 3023
            6 (east) - 3024
            7 - 3025
            8 (south) - 3026
            9 - 3027
            0 - 3018
            ENT - 3029
            CLR - 3028
         */

        String ampcdDevice = "37";
        String ufcDevice = "25";

        JSONArray commandArray = new JSONArray();

        //enter the SUPT menu
        commandArray.put(deviceCodeDelay(ampcdDevice, "3028", instant));
        commandArray.put(deviceCodeDelay(ampcdDevice, "3028", instant));
        //select HSD
        commandArray.put(deviceCodeDelay(ampcdDevice, "3012", instant));
        //select DATA
        commandArray.put(deviceCodeDelay(ampcdDevice, "3020", instant));

        for (Point coordinate : coords) {
            //increment steerpoint
            commandArray.put(deviceCodeDelay(ampcdDevice, "3022", twenty));
            //press UFC
            commandArray.put(deviceCodeDelay(ampcdDevice, "3015", forty));
            // press position 1
            commandArray.put(deviceCodeDelay(ufcDevice, "3010", small));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N north
                commandArray.put(deviceCodeDelay(ufcDevice, "3020", twenty));
            } else {
                //press S south
                commandArray.put(deviceCodeDelay(ufcDevice, "3026", twenty));
            }
            // Enter lat
            enterLatOrLongDigits(commandArray, ufcDevice, coordinate.latitude());
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", thirty));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E east
                commandArray.put(deviceCodeDelay(ufcDevice, "3024", twenty));
            } else {
                //press W west
                commandArray.put(deviceCodeDelay(ufcDevice, "3022", twenty));
            }
            // Enter long
            enterLatOrLongDigits(commandArray, ufcDevice, coordinate.longitude());
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", thirty));
            // press position 3 to select elevation
            commandArray.put(deviceCodeDelay(ufcDevice, "3012", small));
            // press position 1 to select ft
            commandArray.put(deviceCodeDelay(ufcDevice, "3010", small));
            //start entering elevation
            for (char digit : coordinate.elevation().toCharArray()) {
                commandArray.put(digitCommand(ufcDevice, digit));
            }
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", thirty));
        }

        return commandArray;
    }

    private void enterLatOrLongDigits(JSONArray commandArray, String ufcDevice, String latOrLong) {
        String degreesMinutes = latOrLong.substring(0, latOrLong.length() - 4);
        String minuteDecimals = latOrLong.substring(latOrLong.length() - 4);
        for (char digit : degreesMinutes.toCharArray()) {
            commandArray.put(digitCommand(ufcDevice, digit));
        }
        //press enter
        commandArray.put(deviceCodeDelay(ufcDevice, "3029", thirty));
        //start entering last 4 digits
        for (char digit : minuteDecimals.toCharArray()) {
            commandArray.put(digitCommand(ufcDevice, digit));
        }
    }

    private JSONObject digitCommand(String ufcDevice, char digit) {
        return deviceCodeDelay(ufcDevice, Integer.toString(Character.getNumericValue(digit) + 3018), instant);
    }

    private JSONObject deviceCodeDelay(String device, String code, int delay) {
        return new JSONObject().put("device", device).put("code", code).put("delay", getCorrectedDelay(delay)).put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.0000", "#00");
    }
}

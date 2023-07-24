package main.Waypoints.PlanesCommands;

import main.UI.GUI;
import main.Utils.CoordinateUtils;
import main.models.Hemisphere;
import main.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class F18 implements Aircraft {
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
        commandArray.put(deviceCodeDelay(ampcdDevice, "3028", "0"));
        commandArray.put(deviceCodeDelay(ampcdDevice, "3028", "0"));
        //select HSD
        commandArray.put(deviceCodeDelay(ampcdDevice, "3012", "0"));
        //select DATA
        commandArray.put(deviceCodeDelay(ampcdDevice, "3020", "0"));

        for (Point coordinate : coords) {
            //increment steerpoint
            commandArray.put(deviceCodeDelay(ampcdDevice, "3022", "20"));
            //press UFC
            commandArray.put(deviceCodeDelay(ampcdDevice, "3015", "40"));
            // press position 1
            commandArray.put(deviceCodeDelay(ufcDevice, "3010", "10"));
            //check if latitude is N or S
            if (coordinate.latitudeHemisphere() == Hemisphere.NORTH) {
                //press N north
                commandArray.put(deviceCodeDelay(ufcDevice, "3020", "0"));
            } else {
                //press S south
                commandArray.put(deviceCodeDelay(ufcDevice, "3026", "0"));
            }
            // Enter lat
            enterLatOrLongDigits(commandArray, ufcDevice, coordinate.latitude());
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", "30"));
            //check if longitude is E or W
            if (coordinate.longitudeHemisphere() == Hemisphere.EAST) {
                //press E east
                commandArray.put(deviceCodeDelay(ufcDevice, "3024", "0"));
            } else {
                //press W west
                commandArray.put(deviceCodeDelay(ufcDevice, "3022", "0"));
            }
            // Enter long
            enterLatOrLongDigits(commandArray, ufcDevice, coordinate.longitude());
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", "30"));
            // press position 3 to select elevation
            commandArray.put(deviceCodeDelay(ufcDevice, "3012", "10"));
            // press position 1 to select ft
            commandArray.put(deviceCodeDelay(ufcDevice, "3010", "10"));
            //start entering elevation
            for (char digit : coordinate.elevation().toCharArray()) {
                commandArray.put(digitCommand(ufcDevice, digit));
            }
            //press enter
            commandArray.put(deviceCodeDelay(ufcDevice, "3029", "30"));
        }

        return commandArray;
    }

    private static void enterLatOrLongDigits(JSONArray commandArray, String ufcDevice, String latOrLong) {
        String degreesMinutes = latOrLong.substring(0, latOrLong.length() - 4);
        String minuteDecimals = latOrLong.substring(latOrLong.length() - 4);
        for (char digit : degreesMinutes.toCharArray()) {
            commandArray.put(digitCommand(ufcDevice, digit));
        }
        //press enter
        commandArray.put(deviceCodeDelay(ufcDevice, "3029", "30"));
        //start entering last 4 digits
        for (char digit : minuteDecimals.toCharArray()) {
            commandArray.put(digitCommand(ufcDevice, digit));
        }
    }

    private static JSONObject digitCommand(String ufcDevice, char digit) {
        return deviceCodeDelay(ufcDevice, Integer.toString(Character.getNumericValue(digit) + 3018), "0");
    }

    private static JSONObject deviceCodeDelay(String device, String code, String delay) {
        return new JSONObject().put("device", device).put("code", code).put("delay", delay).put("activate", "1").put("addDepress", "true");
    }

    public static List<Point> getCoords(List<Point> dcsPoints) {
        return CoordinateUtils.dcsToDmmFtPoints(dcsPoints, "00.0000", "#00");
    }
}

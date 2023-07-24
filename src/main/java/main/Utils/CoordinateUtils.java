package main.Utils;

import main.models.DMMCoordinate;
import main.models.DMSCoordinate;
import main.models.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CoordinateUtils {
    public static DMMCoordinate decimalToDMM(BigDecimal decimalCoordinate) {
        int degrees = Integer.parseInt(decimalCoordinate.toBigInteger().toString());
        BigDecimal minutes = decimalCoordinate.remainder(BigDecimal.ONE).multiply(new BigDecimal(60));
        return new DMMCoordinate(degrees, minutes);
    }

    public static DMSCoordinate decimalToDMS(BigDecimal decimalCoordinate) {
        int degrees = Integer.parseInt(decimalCoordinate.toBigInteger().toString());
        BigDecimal minutesDecimal = decimalCoordinate.remainder(BigDecimal.ONE).multiply(new BigDecimal(60));
        int minutes = Integer.parseInt(minutesDecimal.toBigInteger().toString());
        BigDecimal secondsDecimal = minutesDecimal.remainder(BigDecimal.ONE).multiply(new BigDecimal(60));
        int seconds = Integer.parseInt(secondsDecimal.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString());
        if (seconds == 60) {
            minutes++;
            seconds = 0;
        }
        if (minutes == 60) {
            degrees++;
            minutes = 0;
        }
        return new DMSCoordinate(degrees, minutes, seconds);
    }

    public static List<Point> dcsToDmmFtPoints(List<Point> dcsPoints, String mmFormat, String longFormat) {
        return dcsToDmmFtPoints(dcsPoints, mmFormat, longFormat, true);
    }

    public static List<Point> dcsToDmmFtPoints(List<Point> dcsPoints, String mmFormat, boolean stripDecimalPoint) {
        return dcsToDmmFtPoints(dcsPoints, mmFormat, "000", stripDecimalPoint);
    }

    public static List<Point> dcsToDmmFtPoints(List<Point> dcsPoints, String mmFormat, String longFormat, boolean stripDecimalPoint) {
        List<Point> dmmFtPoints = new ArrayList<>();
        for (Point dcsPoint : dcsPoints) {
            Double dcsElev = Double.parseDouble(dcsPoint.elevation());

            DMMCoordinate dmmLat = decimalToDMM(new BigDecimal(dcsPoint.latitude()));
            DMMCoordinate dmmLong = decimalToDMM(new BigDecimal(dcsPoint.longitude()));

            DecimalFormat latDegDf = new DecimalFormat("00");
            DecimalFormat latMinDf = new DecimalFormat(mmFormat);
            DecimalFormat longDegDf = new DecimalFormat(longFormat);
            DecimalFormat longMinDf = new DecimalFormat(mmFormat);
            dmmLat = handleIllegalRoundup(dmmLat, latMinDf);
            dmmLong = handleIllegalRoundup(dmmLong, longMinDf);
            String lat = latDegDf.format(dmmLat.degrees()) + latMinDf.format(dmmLat.minutes());
            String lon = longDegDf.format(dmmLong.degrees()) + longMinDf.format(dmmLong.minutes());
            if (stripDecimalPoint) {
                lat = lat.replace(".", "");
                lon = lon.replace(".", "");
            }
            String elev = String.valueOf(Math.round(UnitConvertorUtils.metersToFeet(dcsElev)));

            var dmmFtPoint = new Point(lat, lon, elev, dcsPoint.latitudeHemisphere(), dcsPoint.longitudeHemisphere());
            dmmFtPoints.add(dmmFtPoint);
        }
        return dmmFtPoints;
    }

    private static DMMCoordinate handleIllegalRoundup(DMMCoordinate dmm, DecimalFormat minutesFormat) {
        if (minutesFormat.format(dmm.minutes()).split("\\.")[0].equals("60")) {
            return new DMMCoordinate(dmm.degrees() + 1, 0);
        }
        return dmm;
    }
}

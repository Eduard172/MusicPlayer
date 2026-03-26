package me.eduard.musicplayer.Utils;

@SuppressWarnings("unused")
public class MathUtils {

    public static boolean isNumberInRange(double val, double lowerLimit, double upperLimit){
        return val >= lowerLimit && val <= upperLimit;
    }

    public static int randomInteger(int min, int max){
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static double negative(double value){
        return value < 0 ? value : value * -1;
    }
    public static double roundOrDefault(double value){
        return Math.round(value) == (int) value ? value : Math.round(value);
    }
    public static double constraint(double minLimit, double value, double maxLimit) {
        return value < minLimit ? minLimit : Math.min(maxLimit, value);
    }
    public static double shiftToNextFirstDecimal(double val){
        if(val == (int) val){
            return val;
        }
        String valOf = String.valueOf(val);
        int indexOfDot = valOf.indexOf('.') + 1;
        int firstDecimal = Integer.parseInt(valOf.substring(indexOfDot, indexOfDot + 1));
        return Double.parseDouble(firstDecimal == 9 ? String.valueOf((int) val + 1) : valOf.substring(0, indexOfDot)+(firstDecimal+1));
    }

}

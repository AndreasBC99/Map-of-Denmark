package dk.itu.utils;

import dk.itu.model.OsmPathNode;
import dk.itu.model.OsmWay;

import java.util.Arrays;

public class ArrayUtils {
    public static double[] insertToSmallDoubleArray(double[] oldArray, double newElement) {
        if (oldArray[0] != 0.0) {
            double[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[newArray.length - 1] = newElement;
            return newArray;
        } else {
            oldArray[0] = newElement;
            return oldArray;
        }
    }
    public static float[] insertToSmallFloatArray(float[] oldArray, float newElement, int currentSize) {
        if(currentSize > 1){
            float[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[newArray.length - 1] = newElement;
            return newArray;
        } else {
            oldArray[0] = newElement;
            return oldArray;
        }

    }

    public static boolean[] insertToSmallBooleanArray(boolean[] oldArray, boolean newElement, int currentSize){
        if(currentSize > 1){
            boolean[] newArray = Arrays.copyOf(oldArray, oldArray.length+1);
            newArray[newArray.length - 1] = newElement;
            return newArray;
        } else {
            oldArray[0] = newElement;
            return oldArray;
        }
    }
    public static OsmPathNode[] insertToSmallOsmPathNodeArray(OsmPathNode[] oldArray, OsmPathNode newElement) {
        if (oldArray[0] != null) {
            OsmPathNode[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[newArray.length - 1] = newElement;
            return newArray;
        } else {
            oldArray[0] = newElement;
            return oldArray;
        }
    }
    public static OsmWay[] insertToSmallOsmWayArray(OsmWay[] oldArray, OsmWay newElement) {
        if (oldArray[0] != null) {
            OsmWay[] newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
            newArray[newArray.length - 1] = newElement;
            return newArray;
        } else {
            oldArray[0] = newElement;
            return oldArray;
        }
    }
}

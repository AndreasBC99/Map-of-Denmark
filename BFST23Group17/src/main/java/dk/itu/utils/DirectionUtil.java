package dk.itu.utils;

import dk.itu.model.OsmPathNode;

public class DirectionUtil {

    public static double bearing(double[] vector) {
        double bearing = Math.atan2(vector[0], vector[1]);
        return Math.toDegrees((bearing + 2 * Math.PI) % (2 * Math.PI));
    }

    public static String getDirection(double bearing1, double bearing2, OsmPathNode previousPN, OsmPathNode nextPN){
        double angleDifference; //(bearing2 - bearing1 + 360) % 360;
        if (bearing2 < bearing1) {
            angleDifference = 360 - Math.abs(bearing2 - bearing1);
        } else {
            angleDifference = bearing2 - bearing1;
        }
        String turnDirection;
        if (angleDifference >= 35 && angleDifference < 180) {
            turnDirection = "Right";
        } else if (angleDifference >= 180 && angleDifference <= 325) {
            turnDirection = "Left";
        } else if(angleDifference >= 20 && angleDifference <= 34){
            turnDirection = "Follow right turn";
        } else if(angleDifference >= 326 && angleDifference <= 340){
            turnDirection = "Follow left turn";
        }
        else if(previousPN.getParentWay() != null && previousPN.getParentWay().getName().equals(nextPN.getParentWay().getName())){
            turnDirection = "Stay";
        } else {
            turnDirection = "Straight";
        }
        return turnDirection;
    }
}

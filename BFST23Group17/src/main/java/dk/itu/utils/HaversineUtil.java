package dk.itu.utils;

import dk.itu.model.OsmElement;
import dk.itu.model.OsmPathNode;



public class HaversineUtil {
    //This function is based on the Haversine formula -> calculate the distance in KM between two points
    public static double pointsToKilometer(OsmElement point1, OsmElement point2){
        return pointsToKilometer(point1.getMinX(), point1.getMinY(), point2.getMinX(), point2.getMinY());
    }

    public static float pointsToKilometerFloat(OsmElement point1, OsmElement point2){
        return (float) pointsToKilometer(point1, point2);
    }

    public static double pointsToKilometer(OsmElement point1, float x, float y) {
        return pointsToKilometer(point1.getMinX(), point1.getMinY(), x, y);
    }

    public static float pointsToKilometerFloat(OsmElement point1, float x, float y) {
        return (float) pointsToKilometer(point1, x, y);
    }

    public static double pointsToKilometer(float x1, float y1, float x2, float y2) {
        double distanceLongitude = Math.toRadians(x1 - x2);
        double distanceLatitude = Math.toRadians(y1 - y2);
        double a = Math.sin(distanceLatitude / 2) * Math.sin(distanceLatitude / 2) +
                Math.cos(Math.toRadians(y1)) * Math.cos(Math.toRadians(y2)) *
                        Math.sin(distanceLongitude / 2) * Math.sin(distanceLongitude / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return (6371 * c);//Multiplied by kilometer radius of earth
    }

    public static float pointsToKilometerFloat(float x1, float y1, float x2, float y2) {
        return (float) pointsToKilometer(x1, y1, x2, y2);

    }
}



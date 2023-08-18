package dk.itu.utils;
public class WebMercatorUtils {
    /*
    * This class is based on the spherical pseudo-mercator projection.
    *
    * */

    private static final double RADIUS = 6378137.0; //Radius of earth

    public static double[] toWebMercator(double latitude, double longitude) {
        double x = RADIUS * Math.toRadians(longitude);
        double y = RADIUS * Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latitude) / 2));

        return new double[]{x, y};
    }

    public static double[] toLatLon(double x, double y) {

        double longitude = Math.toDegrees(x / RADIUS);
        double latitude = Math.toDegrees(2 * Math.atan(Math.exp(y / RADIUS)) - Math.PI / 2);

        return new double[]{latitude, longitude};

    }

}


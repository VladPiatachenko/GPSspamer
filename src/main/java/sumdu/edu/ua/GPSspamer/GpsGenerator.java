package sumdu.edu.ua.GPSspamer;

public class GpsGenerator {
    private double lat = 50.4500;
    private double lon = 30.5230;
    private double alt = 120.0;

    public double[] next() {
        lat += 0.0001;
        lon += 0.0001;
        alt += 0.5;
        return new double[]{lat, lon, alt};
    }
}

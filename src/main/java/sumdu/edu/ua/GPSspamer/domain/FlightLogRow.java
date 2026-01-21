package sumdu.edu.ua.GPSspamer.domain;

public record FlightLogRow(
        long t,
        String flightId,
        String source,
        double truthLat, double truthLon, double truthAlt,
        double attackLat, double attackLon, double attackAlt,
        String attackType,
        boolean attackEnabled,
        double biasMeters,
        double driftMps,
        double noiseSigmaMeters
) {}

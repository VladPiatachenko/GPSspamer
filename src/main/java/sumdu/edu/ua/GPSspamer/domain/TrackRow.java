package sumdu.edu.ua.GPSspamer.domain;

import sumdu.edu.ua.GPSspamer.config.AttackConfig;

import java.util.Map;

public record TrackRow(
        long tEpochSec,
        String flightId,
        TrackPoint truth,
        TrackPoint attack,
        AttackConfig attackConfig,
        Map<String, Object> meta
) {}

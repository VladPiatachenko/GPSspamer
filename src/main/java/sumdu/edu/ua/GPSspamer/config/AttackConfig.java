package sumdu.edu.ua.GPSspamer.config;

import sumdu.edu.ua.GPSspamer.domain.AttackType;

public record AttackConfig(
        boolean enabled,
        AttackType type,
        double biasMeters,
        double driftMps,
        double noiseSigmaMeters
) {}

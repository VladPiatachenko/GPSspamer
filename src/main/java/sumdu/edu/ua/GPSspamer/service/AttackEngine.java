package sumdu.edu.ua.GPSspamer.service;

import org.springframework.stereotype.Component;
import sumdu.edu.ua.GPSspamer.config.AttackConfig;
import sumdu.edu.ua.GPSspamer.domain.AttackType;
import sumdu.edu.ua.GPSspamer.domain.TrackPoint;

import java.util.Random;

@Component
public class AttackEngine {

    private final Random rnd = new Random();

    public TrackPoint apply(TrackPoint truth, AttackConfig cfg, long t0) {
        if (cfg == null || !cfg.enabled() || cfg.type() == AttackType.NONE) return truth;

        double lat = truth.lat();
        double lon = truth.lon();

        // груба конвертація метрів у градуси (для демо/датасету ок)
        // 1 deg lat ~ 111_320 m, 1 deg lon ~ 111_320*cos(lat)
        double metersToDegLat = 1.0 / 111_320.0;
        double metersToDegLon = 1.0 / (111_320.0 * Math.cos(Math.toRadians(lat)));

        double dMeters = 0.0;

        switch (cfg.type()) {
            case BIAS -> dMeters = cfg.biasMeters();
            case DRIFT -> {
                double dt = Math.max(0, truth.t() - t0); // секунди (якщо time epoch)
                dMeters = cfg.biasMeters() + cfg.driftMps() * dt;
            }
            case NOISE -> dMeters = rnd.nextGaussian() * cfg.noiseSigmaMeters();
            default -> {}
        }

        // “уводимо” під кутом 45° просто щоб було видно розходження
        double dLat = (dMeters * 0.7071) * metersToDegLat;
        double dLon = (dMeters * 0.7071) * metersToDegLon;

        return new TrackPoint(truth.t(), lat + dLat, lon + dLon, truth.alt());
    }
}

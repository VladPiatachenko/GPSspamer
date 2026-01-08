package sumdu.edu.ua.GPSspamer.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import sumdu.edu.ua.GPSspamer.config.AttackConfig;
import sumdu.edu.ua.GPSspamer.domain.*;
import sumdu.edu.ua.GPSspamer.integration.fr24.Fr24Client;
import sumdu.edu.ua.GPSspamer.service.AttackEngine;
import sumdu.edu.ua.GPSspamer.service.CsvFlightLogger;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GpsApiController {

    private final Fr24Client fr24;
    private final SessionState state;
    private final AttackEngine attackEngine;
    private final TrackStore trackStore;
    private final CsvFlightLogger logger;

    public GpsApiController(
            Fr24Client fr24,
            SessionState state,
            AttackEngine attackEngine,
            TrackStore trackStore,
            CsvFlightLogger logger
    ) {
        this.fr24 = fr24;
        this.state = state;
        this.attackEngine = attackEngine;
        this.trackStore = trackStore;
        this.logger = logger;
    }

    // 1) select flight by flightId (e.g. 3dd34e3c)
    @PostMapping("/selectFlight")
    public Map<String, Object> selectFlight(@RequestBody Map<String, Object> body) {
        String flightId = ((String) body.getOrDefault("flightId", "")).trim();
        if (flightId.isBlank()) return Map.of("ok", false, "error", "flightId is required");

        state.setSelectedFlight(new SessionState.SelectedFlight(flightId));

        logger.stop();
        logger.startNew(flightId);

        return Map.of("ok", true, "selected", Map.of("flightId", flightId));
    }

    // 2) set attack config
    @PostMapping("/attack")
    public Map<String, Object> attack(@RequestBody Map<String, Object> body) {
        boolean enabled = (Boolean) body.getOrDefault("enabled", false);
        AttackType type = AttackType.valueOf(((String) body.getOrDefault("type", "NONE")).toUpperCase());

        double bias = ((Number) body.getOrDefault("biasMeters", 0)).doubleValue();
        double drift = ((Number) body.getOrDefault("driftMps", 0)).doubleValue();
        double sigma = ((Number) body.getOrDefault("noiseSigmaMeters", 0)).doubleValue();

        AttackConfig cfg = new AttackConfig(enabled, type, bias, drift, sigma);
        state.setAttack(cfg);
        return Map.of("ok", true, "attack", cfg);
    }

    // 3) current state (row1 truth + row2 attack)
    @GetMapping("/current")
    public Map<String, Object> current() {
        var last = state.getLast();
        if (last == null) return Map.of("error", "No data yet. Start LIVE stream first.");

        return Map.of(
                "t", last.t(),
                "truth", Map.of("lat", last.truth().lat(), "lon", last.truth().lon(), "alt", last.truth().alt()),
                "attack", Map.of("lat", last.attack().lat(), "lon", last.attack().lon(), "alt", last.attack().alt()),
                "attackConfig", state.getAttack(),
                "trackSize", trackStore.size(),
                "currentLogFile", logger.getCurrentFile() == null ? null : logger.getCurrentFile().getFileName().toString()
        );
    }

    // 4) track history
    @GetMapping("/track")
    public Map<String, Object> track(@RequestParam(defaultValue = "500") int limit) {
        int lim = Math.max(1, Math.min(limit, 50_000));
        var sel = state.getSelectedFlight();
        return Map.of(
                "flightId", sel == null ? null : sel.flightId(),
                "count", trackStore.size(),
                "points", trackStore.last(lim)
        );
    }

    // 5) SSE stream
    @GetMapping(path = "/liveStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter liveStream() {
        SseEmitter emitter = new SseEmitter(0L);

        new Thread(() -> {
            String flightId = null;
            try {
                var sel = state.getSelectedFlight();
                if (sel == null) {
                    emitter.send(Map.of("type", "error", "message", "No flight selected. POST /api/selectFlight first."));
                    emitter.complete();
                    return;
                }

                flightId = sel.flightId();
                System.out.println("LIVE STREAM STARTED for flightId=" + flightId);

                long t0 = -1;

                while (true) {
                    System.out.println("Polling FR24 for " + flightId);

                    LiveState s = fr24.latestPointByFlightId(flightId);
                    if (s == null) {
                        emitter.send(Map.of("type", "heartbeat", "flightId", flightId, "message", "No data right now"));
                        Thread.sleep(5000);
                        continue;
                    }

                    if (t0 < 0) t0 = s.time();

                    TrackPoint truth = new TrackPoint(s.time(), s.lat(), s.lon(), s.alt());
                    TrackPoint attacked = attackEngine.apply(truth, state.getAttack(), t0);

                    state.setLast(new SessionState.PairPoint(s.time(), truth, attacked));

                    TrackRow row = new TrackRow(
                            s.time(),
                            flightId,
                            truth,
                            attacked,
                            state.getAttack(),
                            Map.of("src", "FR24", "id", s.id())
                    );

                    trackStore.add(row);
                    logger.append(row);

                    emitter.send(Map.of(
                            "type", "data",
                            "t", s.time(),
                            "meta", Map.of("flightId", flightId,  "id", s.id()),
                            "truth", Map.of("lat", truth.lat(), "lon", truth.lon(), "alt", truth.alt()),
                            "attack", Map.of("lat", attacked.lat(), "lon", attacked.lon(), "alt", attacked.alt())
                    ));

                    Thread.sleep(5000);
                }

            } catch (Exception e) {
                System.err.println("SSE stream error for " + flightId + ": " + e.getMessage());
                emitter.completeWithError(e);
            }
        }, "live-stream").start();

        return emitter;
    }
}

package sumdu.edu.ua.GPSspamer.service;

import org.springframework.stereotype.Component;
import sumdu.edu.ua.GPSspamer.config.AttackConfig;
import sumdu.edu.ua.GPSspamer.domain.TrackRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Component
public class CsvFlightLogger {

    private final Path logsDir = Paths.get("logs");
    private final Path currentFile = logsDir.resolve("current.csv");

    private volatile BufferedWriter writer;

    public synchronized void startNew(String flightId) {
        try {
            Files.createDirectories(logsDir);

            // overwrite every time (no new files)
            writer = Files.newBufferedWriter(
                    currentFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            writer.write(String.join(",",
                    "t_epoch",
                    "flight_id",
                    "lat_true","lon_true","alt_true",
                    "lat_attack","lon_attack","alt_attack",
                    "attack_enabled","attack_type","bias_m","drift_mps","sigma_m"
            ));
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Failed to start CSV logger", e);
        }
    }

    public synchronized void append(TrackRow row) {
        if (writer == null) return;

        try {
            AttackConfig cfg = row.attackConfig();

            String line = String.join(",",
                    String.valueOf(row.tEpochSec()),
                    csv(row.flightId()),

                    String.valueOf(row.truth().lat()),
                    String.valueOf(row.truth().lon()),
                    String.valueOf(row.truth().alt()),

                    String.valueOf(row.attack().lat()),
                    String.valueOf(row.attack().lon()),
                    String.valueOf(row.attack().alt()),

                    String.valueOf(cfg.enabled()),
                    csv(cfg.type().name()),
                    String.valueOf(cfg.biasMeters()),
                    String.valueOf(cfg.driftMps()),
                    String.valueOf(cfg.noiseSigmaMeters())
            );

            writer.write(line);
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            throw new RuntimeException("Failed to append CSV row", e);
        }
    }

    public synchronized void stop() {
        try {
            if (writer != null) writer.close();
        } catch (IOException ignored) {}
        writer = null;
    }

    public Path getCurrentFile() {
        return currentFile;
    }

    public Path getLogsDir() {
        return logsDir;
    }

    private static String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\"") || t.contains("\n") || t.contains("\r")) {
            return "\"" + t + "\"";
        }
        return t;
    }
}

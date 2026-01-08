package sumdu.edu.ua.GPSspamer.integration.fr24;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sumdu.edu.ua.GPSspamer.domain.LiveState;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Component
public class Fr24Client {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Value("${fr24.baseUrl}")
    private String baseUrl;

    @Value("${fr24.token:}")
    private String token;

    @Value("${fr24.acceptVersion:v1}")
    private String acceptVersion;

    public LiveState latestPointByFlightId(String flightId) throws IOException, InterruptedException {
        if (token == null || token.isBlank()) {
            System.out.println("[FR24] token is empty. Set fr24.token in application.properties");
            return null;
        }
        if (flightId == null || flightId.isBlank()) return null;

        String url = baseUrl + "/flight-tracks?flight_id=" + flightId.trim();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Accept-Version", acceptVersion)
                .header("Authorization", "Bearer " + token.trim())
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        String body = resp.body() == null ? "" : resp.body();

        if (code < 200 || code >= 300) {
            String snippet = body.replace("\n", " ").replace("\r", " ");
            if (snippet.length() > 300) snippet = snippet.substring(0, 300);
            System.out.println("[FR24] HTTP " + code + " body=" + snippet);
            return null;
        }

        JsonNode root = mapper.readTree(body);

        // ПАРСИНГ "з запасом": шукаємо масив точок, де є lat/lon
        JsonNode pointsArray = findFirstTrackArray(root);
        if (pointsArray == null || !pointsArray.isArray() || pointsArray.size() == 0) {
            System.out.println("[FR24] No track array found in response (flight_id=" + flightId + ")");
            return null;
        }

        JsonNode last = pointsArray.get(pointsArray.size() - 1);

        Double lat = pickDouble(last, "lat", "latitude");
        Double lon = pickDouble(last, "lon", "lng", "longitude");
        Double alt = pickDouble(last, "alt", "altitude", "alt_ft", "altitude_ft", "geo_altitude");

        Long t = pickLong(last, "t", "time", "timestamp", "ts");

        if (lat == null || lon == null) {
            System.out.println("[FR24] Last point has no lat/lon (flight_id=" + flightId + ")");
            return null;
        }

        long time = (t != null) ? t : System.currentTimeMillis() / 1000;
        double altitude = (alt != null) ? alt : 0.0;

        return new LiveState(time, flightId, lat, lon, altitude);
    }

    // -------- helpers --------

    private static Double pickDouble(JsonNode node, String... keys) {
        if (node == null) return null;
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && !v.isNull()) {
                try { return v.asDouble(); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static Long pickLong(JsonNode node, String... keys) {
        if (node == null) return null;
        for (String k : keys) {
            JsonNode v = node.get(k);
            if (v != null && !v.isNull()) {
                try { return v.asLong(); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * Шукаємо перший JsonNode-масив, в елементах якого є lat/lon або latitude/longitude.
     * Це робить клієнт живучим, навіть якщо структура відповіді відрізняється.
     */
    private JsonNode findFirstTrackArray(JsonNode root) {
        if (root == null) return null;
        // найчастіше це щось типу root.track / root.tracks / root.data.track ...
        JsonNode direct =
                firstNonNull(root.get("track"), root.get("tracks"), root.get("positions"), root.get("points"));
        if (direct != null && direct.isArray()) return direct;

        // рекурсивний пошук
        return deepFindArrayWithLatLon(root);
    }

    private JsonNode deepFindArrayWithLatLon(JsonNode node) {
        if (node == null) return null;

        if (node.isArray()) {
            if (node.size() > 0 && elementLooksLikePoint(node.get(0))) return node;
            // якщо масив масивів — теж пройдемось
            for (JsonNode child : node) {
                JsonNode r = deepFindArrayWithLatLon(child);
                if (r != null) return r;
            }
            return null;
        }

        if (node.isObject()) {
            var it = node.fields();
            while (it.hasNext()) {
                var e = it.next();
                JsonNode r = deepFindArrayWithLatLon(e.getValue());
                if (r != null) return r;
            }
        }
        return null;
    }

    private boolean elementLooksLikePoint(JsonNode el) {
        if (el == null || !el.isObject()) return false;
        boolean hasLat = el.has("lat") || el.has("latitude");
        boolean hasLon = el.has("lon") || el.has("lng") || el.has("longitude");
        return hasLat && hasLon;
    }

    private static JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode n : nodes) if (n != null && !n.isNull()) return n;
        return null;
    }
}

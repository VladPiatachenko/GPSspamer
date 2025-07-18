package sumdu.edu.ua.GPSspamer;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/gps")
public class GpsController {
    private final GpsGenerator generator;

    public GpsController(GpsGenerator generator) {
        this.generator = generator;
    }

    @GetMapping
    public Map<String, Object> getCurrentGps() {
        Map<String, Object> res = new HashMap<>();
        res.put("timestamp", Instant.now().toString());

        double[] coords = generator.next();
        res.put("lat", coords[0]);
        res.put("lon", coords[1]);
        res.put("alt", coords[2]);
        res.put("status", "NORMAL");
        return res;
    }
}
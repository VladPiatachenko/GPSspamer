package sumdu.edu.ua.GPSspamer.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.GPSspamer.service.CsvFlightLogger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/log")
public class LogController {

    private final CsvFlightLogger logger;

    public LogController(CsvFlightLogger logger) {
        this.logger = logger;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Path p = logger.getCurrentFile();
        return Map.of(
                "ok", true,
                "file", p.getFileName().toString(),
                "exists", Files.exists(p)
        );
    }

    @GetMapping("/downloadCurrent")
    public ResponseEntity<FileSystemResource> downloadCurrent() {
        Path p = logger.getCurrentFile();
        if (!Files.exists(p)) return ResponseEntity.notFound().build();

        FileSystemResource res = new FileSystemResource(p.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"current.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(res);
    }
}

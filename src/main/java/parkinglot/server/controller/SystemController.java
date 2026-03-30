package parkinglot.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class SystemController {

    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    @GetMapping("/api/info")
    public Map<String, Object> getInfo() {
        return Map.of(
                "status", "UP",
                "serverTime", LocalDateTime.now(),
                "version", "1.0.0-SNAPSHOT"
        );
    }
}

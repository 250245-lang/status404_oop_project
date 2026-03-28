package parkinglot.server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public abstract class BaseController {

    protected <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }

    protected ResponseEntity<Map<String, String>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    protected ResponseEntity<Map<String, String>> unauthorized(String message) {
        return error(message, HttpStatus.UNAUTHORIZED);
    }
}

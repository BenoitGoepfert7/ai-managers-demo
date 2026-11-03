package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeoBlockingController {

    private final GeoBlockingService service;

    public GeoBlockingController(GeoBlockingService service) {
        this.service = service;
    }

    @GetMapping("/geo/check")
    public ResponseEntity<Map<String, Boolean>> check(@RequestParam String contentId, @RequestParam String ip) {
        boolean allowed = service.isAllowed(contentId, ip);
        return ResponseEntity.status(allowed ? 200 : 451).body(Map.of("allowed", allowed));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> geoIpFailure(IllegalArgumentException e) {
        return ResponseEntity.status(502).body(e.getMessage());
    }
}

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

    public GeoBlockingController(GeoBlockingService p_service) {
        this.service = p_service;
    }

    @GetMapping("/geo/check")
    public ResponseEntity<Map<String, Boolean>> check(@RequestParam("contentId") String p_contentId,
                                                      @RequestParam("ip") String p_ip) {
        boolean allowed = service.isAllowed(p_contentId, p_ip);
        return ResponseEntity.status(allowed ? 200 : 451).body(Map.of("allowed", allowed));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> geoIpFailure(IllegalArgumentException p_e) {
        return ResponseEntity.status(502).body(p_e.getMessage());
    }
}

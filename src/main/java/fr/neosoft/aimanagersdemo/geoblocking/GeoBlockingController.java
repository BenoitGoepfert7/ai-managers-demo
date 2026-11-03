package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GeoBlockingController {

    private final GeoBlockingSvc service;

    public GeoBlockingController(GeoBlockingSvc p_service) {
        this.service = p_service;
    }

    @GetMapping("/geo/check")
    public ResponseEntity<Map<String, Object>> check(@RequestParam("contentId") String p_contentId,
                                                     @RequestParam("ip") String p_ip,
                                                     @RequestParam(name = "subscriberId", required = false) String p_subscriberId) {
        var decision = service.check(p_contentId, p_ip, p_subscriberId);
        int status = switch (decision) {
            case ALLOWED -> 200;
            case UNKNOWN_SUBSCRIBER -> 404;
            default -> 451;
        };
        return ResponseEntity.status(status)
                .body(Map.of("allowed", decision == GeoBlockingSvc.Decision.ALLOWED, "decision", decision));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> geoIpFailure(IllegalArgumentException e) {
        return ResponseEntity.status(502).body(e.getMessage());
    }
}

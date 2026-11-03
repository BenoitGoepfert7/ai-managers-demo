package fr.neosoft.aimanagersdemo.geoblocking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class GeoBlockingController {

    private final GeoBlockingSvc service;

    public GeoBlockingController(GeoBlockingSvc service) {
        this.service = service;
    }

    @GetMapping("/geo/check")
    public ResponseEntity<GeoDecision> check(@RequestParam("contentId") String contentId,
                                             @RequestParam("ip") String ip,
                                             @RequestParam(name = "subscriberId", required = false) String subscriberId) {
        var decision = service.check(contentId, ip, Optional.ofNullable(subscriberId).filter(id -> !id.isBlank()));
        return ResponseEntity.status(decision.allowed() ? 200 : 451).body(decision);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<String> geoIpFailure(IllegalArgumentException e) {
        return ResponseEntity.status(502).body(e.getMessage());
    }
}

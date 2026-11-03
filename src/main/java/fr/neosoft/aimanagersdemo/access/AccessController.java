package fr.neosoft.aimanagersdemo.access;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AccessController {

    private final AccessService service;

    public AccessController(AccessService service) {
        this.service = service;
    }

    @GetMapping("/access/{subscriberId}/channels/{channel}")
    public ResponseEntity<Map<String, Object>> check(@PathVariable String subscriberId, @PathVariable String channel) {
        var decision = service.check(subscriberId, channel);
        var status = switch (decision) {
            case GRANTED -> 200;
            case UNKNOWN_SUBSCRIBER -> 404;
            default -> 403;
        };
        return ResponseEntity.status(status).body(Map.of("decision", decision, "granted", decision == AccessService.Decision.GRANTED));
    }
}

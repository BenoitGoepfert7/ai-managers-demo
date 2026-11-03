package fr.neosoft.aimanagersdemo.access;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AccessController {

    private final AccessService service;

    public AccessController(AccessService p_service) {
        this.service = p_service;
    }

    @GetMapping("/access/{subscriberId}/channels/{channel}")
    public ResponseEntity<Map<String, Object>> check(@PathVariable("subscriberId") String p_subscriberId,
                                                     @PathVariable("channel") String p_channel) {
        return response(service.check(p_subscriberId, p_channel));
    }

    @PostMapping("/access/{subscriberId}/channels/{channel}")
    public ResponseEntity<Map<String, Object>> grant(@PathVariable("subscriberId") String p_subscriberId,
                                                     @PathVariable("channel") String p_channel) {
        return response(service.grant(p_subscriberId, p_channel));
    }

    private ResponseEntity<Map<String, Object>> response(AccessService.Decision decision) {
        var status = switch (decision) {
            case GRANTED -> 200;
            case UNKNOWN_SUBSCRIBER -> 404;
            default -> 403;
        };
        return ResponseEntity.status(status).body(Map.of("decision", decision, "granted", decision == AccessService.Decision.GRANTED));
    }
}

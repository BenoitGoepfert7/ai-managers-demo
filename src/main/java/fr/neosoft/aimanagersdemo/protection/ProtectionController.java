package fr.neosoft.aimanagersdemo.protection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProtectionController {

    private final PlaybackTokenService tokens;

    public ProtectionController(PlaybackTokenService tokens) {
        this.tokens = tokens;
    }

    @PostMapping("/protection/tokens")
    public Map<String, String> issue(@RequestParam String subscriberId, @RequestParam String contentId) {
        return Map.of("token", tokens.issue(subscriberId, contentId));
    }

    @GetMapping("/protection/stream")
    public ResponseEntity<String> stream(@RequestParam String subscriberId, @RequestParam String contentId,
                                         @RequestParam String token) {
        return tokens.isValid(token, subscriberId, contentId)
                ? ResponseEntity.ok("stream://" + contentId + "/master.m3u8")
                : ResponseEntity.status(403).body("Invalid or expired token");
    }
}

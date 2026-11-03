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

    public ProtectionController(PlaybackTokenService p_tokens) {
        this.tokens = p_tokens;
    }

    @PostMapping("/protection/tokens")
    public Map<String, String> issue(@RequestParam("subscriberId") String p_subscriberId,
                                     @RequestParam("contentId") String p_contentId) {
        return Map.of("token", tokens.issue(p_subscriberId, p_contentId));
    }

    @GetMapping("/protection/stream")
    public ResponseEntity<String> stream(@RequestParam("subscriberId") String p_subscriberId,
                                         @RequestParam("contentId") String p_contentId,
                                         @RequestParam("token") String p_token) {
        return tokens.isValid(p_token, p_subscriberId, p_contentId)
                ? ResponseEntity.ok("stream://" + p_contentId + "/master.m3u8")
                : ResponseEntity.status(403).body("Invalid or expired token");
    }
}

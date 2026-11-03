package fr.neosoft.aimanagersdemo.protection;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;

/** Issues and validates short-lived signed tokens protecting a stream URL (HMAC-SHA256). */
@Service
public class PlaybackTokenService {

    static final long TTL_SECONDS = 60;
    private static final byte[] SECRET = "demo-secret-key-not-for-production".getBytes(StandardCharsets.UTF_8);

    private final Clock clock;

    public PlaybackTokenService(Clock p_clock) {
        this.clock = p_clock;
    }

    public String issue(String subscriberId, String contentId) {
        long expires = clock.instant().getEpochSecond() + TTL_SECONDS;
        String payload = subscriberId + "|" + contentId + "|" + expires;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + sign(payload);
    }

    public boolean isValid(String p_token, String p_subscriberId, String p_contentId) {
        try {
            String[] parts = p_token.split("\\.");
            if (parts.length != 2) return false;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            String[] fields = payload.split("\\|");
            return fields.length == 3
                    && fields[0].equals(p_subscriberId)
                    && fields[1].equals(p_contentId)
                    && Long.parseLong(fields[2]) > clock.instant().getEpochSecond();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String sign(String p_payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(p_payload.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}

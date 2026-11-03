package fr.neosoft.aimanagersdemo.protection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaybackTokenServiceTest {

    private final Instant now = Instant.parse("2026-01-01T10:00:00Z");

    private PlaybackTokenService at(Instant instant) {
        return new PlaybackTokenService(Clock.fixed(instant, ZoneOffset.UTC));
    }

    @Nested
    class Validity {

        @Test
        void freshTokenIsValid() {
            String token = at(now).issue("s1", "movie1");
            assertThat(at(now.plusSeconds(30)).isValid(token, "s1", "movie1")).isTrue();
        }

        @Test
        void expiredTokenIsRejected() {
            String token = at(now).issue("s1", "movie1");
            assertThat(at(now.plusSeconds(61)).isValid(token, "s1", "movie1")).isFalse();
        }
    }

    @Test
    void tokenIsBoundToSubscriberAndContent() {
        String token = at(now).issue("s1", "movie1");
        assertFalse(at(now).isValid(token, "s2", "movie1"));
        assertFalse(at(now).isValid(token, "s1", "movie2"));
    }

    @Nested
    class Tampering {

        @Test
        void tamperedOrMalformedTokenIsRejected() {
            String token = at(now).issue("s1", "movie1");
            assertFalse(at(now).isValid(token.substring(0, token.length() - 2) + "xx", "s1", "movie1"));
            assertFalse(at(now).isValid("garbage", "s1", "movie1"));
            assertFalse(at(now).isValid("%%%.abc", "s1", "movie1"));
        }
    }
}

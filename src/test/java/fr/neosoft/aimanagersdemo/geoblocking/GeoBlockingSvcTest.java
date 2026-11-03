package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GeoBlockingSvcTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);

    private String country = "FR";
    private final GeoBlockingSvc service = new GeoBlockingSvc(ip -> country, new SubscriberRepo(), CLOCK);

    private GeoDecision check(String contentId, String subscriberId) {
        return service.check(contentId, "1.1.1.1", Optional.ofNullable(subscriberId));
    }

    @Test
    void unknownContentIsDenied() {
        assertThat(check("unknown", "s1")).isEqualTo(GeoDecision.deny(DenialReason.UNKNOWN_CONTENT));
    }

    @Test
    void categoryWithoutAgeLimitIsAllowedWithoutSubscriber() {
        assertThat(check("world-cup-final", null)).isEqualTo(GeoDecision.allow());
    }

    @Test
    void countryRulesDependOnCategory() {
        country = "US";
        assertThat(check("news-live", null).allowed()).isTrue();
        assertThat(check("world-cup-final", null)).isEqualTo(GeoDecision.deny(DenialReason.COUNTRY_BLOCKED));
        assertThat(check("late-night", "s1")).isEqualTo(GeoDecision.deny(DenialReason.COUNTRY_BLOCKED));
    }

    @Test
    void ageLimitedContentWithoutSubscriberIsDenied() {
        assertThat(check("action-movie", null)).isEqualTo(GeoDecision.deny(DenialReason.SUBSCRIBER_REQUIRED));
    }

    @Test
    void unknownSubscriberIsDenied() {
        assertThat(check("action-movie", "nobody")).isEqualTo(GeoDecision.deny(DenialReason.UNKNOWN_SUBSCRIBER));
    }

    @Test
    void adultSubscriberIsAllowed() {
        assertThat(check("late-night", "s1")).isEqualTo(GeoDecision.allow());
    }

    @Test
    void subscriberTurningEighteenTomorrowIsDenied() {
        assertThat(check("late-night", "s3")).isEqualTo(GeoDecision.deny(DenialReason.AGE_RESTRICTED));
    }

    @Test
    void minimumAgeDependsOnCountry() {
        assertThat(check("action-movie", "s2")).isEqualTo(GeoDecision.allow());
        country = "DE";
        assertThat(check("action-movie", "s2")).isEqualTo(GeoDecision.deny(DenialReason.AGE_RESTRICTED));
    }
}

package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import fr.neosoft.aimanagersdemo.geoblocking.GeoBlockingSvc.Decision;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoBlockingSvcTest {

    private static final String ADULT = "late-night-movie";

    private GeoBlockingSvc svcAt(String instant, String country) {
        GeoIpClient geoIp = p_ip -> {
            if (country == null) throw new IllegalArgumentException("lookup failed");
            return country;
        };
        return new GeoBlockingSvc(geoIp, new SubscriberRepo(), Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    @Test
    void allowsCategoryAllowedInCountry() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "US").check("news-live", "ip", null)).isEqualTo(Decision.ALLOWED);
    }

    @Test
    void blocksCategoryNotAllowedInCountry() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "DE").check(ADULT, "ip", "s1")).isEqualTo(Decision.CATEGORY_BLOCKED);
    }

    @Test
    void blocksContentRestrictedToOtherCountries() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "US").check("world-cup-final", "ip", "s1")).isEqualTo(Decision.COUNTRY_BLOCKED);
    }

    @Test
    void blocksUnknownContent() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "FR").check("unknown", "ip", null)).isEqualTo(Decision.COUNTRY_BLOCKED);
    }

    @Test
    void minimumAgeDiffersByCountry() {
        // s4 is 18 on 2026-01-01: allowed in FR (18+), restricted in US (21+)
        assertThat(svcAt("2026-01-01T10:00:00Z", "FR").check(ADULT, "ip", "s4")).isEqualTo(Decision.ALLOWED);
        assertThat(svcAt("2026-01-01T10:00:00Z", "US").check(ADULT, "ip", "s4")).isEqualTo(Decision.AGE_RESTRICTED);
    }

    @Test
    void ageBoundaryIsTheBirthday() {
        // s4 is born 2007-06-15
        assertThat(svcAt("2025-06-14T23:59:00Z", "FR").check(ADULT, "ip", "s4")).isEqualTo(Decision.AGE_RESTRICTED);
        assertThat(svcAt("2025-06-15T00:00:00Z", "FR").check(ADULT, "ip", "s4")).isEqualTo(Decision.ALLOWED);
    }

    @Test
    void ageRestrictedContentWithoutSubscriberIsBlocked() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "FR").check(ADULT, "ip", null)).isEqualTo(Decision.AGE_RESTRICTED);
    }

    @Test
    void unknownSubscriber() {
        assertThat(svcAt("2026-01-01T10:00:00Z", "FR").check("news-live", "ip", "nobody")).isEqualTo(Decision.UNKNOWN_SUBSCRIBER);
    }

    @Test
    void geoIpFailurePropagates() {
        assertThatThrownBy(() -> svcAt("2026-01-01T10:00:00Z", null).check("news-live", "ip", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

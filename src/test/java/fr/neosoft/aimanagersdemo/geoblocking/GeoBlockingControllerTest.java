package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import fr.neosoft.aimanagersdemo.geoblocking.client.GeoIpClientMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeoBlockingController.class)
@Import({GeoBlockingSvc.class, GeoIpClientMock.class, ContentCatalog.class, AgeRules.class,
        SubscriberRepo.class, GeoBlockingControllerTest.FixedClock.class})
class GeoBlockingControllerTest {

    @TestConfiguration
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-11-03T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    static final String MOVIE = "late-night-movie-1";
    static final String FR_IP = "81.2.69.1";
    static final String US_IP = "8.8.8.8";

    @Autowired
    MockMvc mvc;

    @Nested
    class Decision {

        @Test
        void allowedCountry() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "world-cup-final").param("ip", "81.2.69.1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.allowed").value(true));
        }

        @Test
        void blockedCountry() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "world-cup-final").param("ip", "8.8.8.8"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.allowed").value(false));
        }

        @Test
        void unknownContentIsBlocked() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "unknown").param("ip", "81.2.69.1"))
                    .andExpect(status().is(451));
        }
    }

    @Nested
    class ProviderFailure {

        @Test
        void externalApiFailureReturnsBadGateway() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "news-live").param("ip", "0.0.0.0"))
                    .andExpect(status().isBadGateway());
        }
    }

    @Nested
    class AgeRestriction {

        private org.springframework.test.web.servlet.ResultActions check(String ip, String subscriber) throws Exception {
            var req = get("/geo/check").param("contentId", MOVIE).param("ip", ip);
            if (subscriber != null) req = req.param("subscriberId", subscriber);
            return mvc.perform(req);
        }

        @Test
        void nineteenYearOldAllowedInFrance() throws Exception {
            check(FR_IP, "s2").andExpect(status().isOk()).andExpect(jsonPath("$.reason").value("ALLOWED"));
        }

        @Test
        void nineteenYearOldBlockedInUs() throws Exception {
            check(US_IP, "s2").andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.allowed").value(false))
                    .andExpect(jsonPath("$.reason").value("AGE_RESTRICTED"));
        }

        @Test
        void adultAllowedInUs() throws Exception {
            check(US_IP, "s1").andExpect(status().isOk());
        }

        @Test
        void minorBlockedInFrance() throws Exception {
            check(FR_IP, "s3").andExpect(status().isForbidden()).andExpect(jsonPath("$.reason").value("AGE_RESTRICTED"));
        }

        @Test
        void missingSubscriberIsAgeUnknown() throws Exception {
            check(US_IP, null).andExpect(status().isForbidden()).andExpect(jsonPath("$.reason").value("AGE_UNKNOWN"));
        }

        @Test
        void unknownSubscriberIsAgeUnknown() throws Exception {
            check(US_IP, "nobody").andExpect(status().isForbidden()).andExpect(jsonPath("$.reason").value("AGE_UNKNOWN"));
        }

        @Test
        void uncategorisedContentIgnoresAge() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "news-live").param("ip", US_IP).param("subscriberId", "s3"))
                    .andExpect(status().isOk());
        }

        @Test
        void countryDenialKeepsPrecedenceAndReason() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "world-cup-final").param("ip", US_IP).param("subscriberId", "s3"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.reason").value("COUNTRY_NOT_ALLOWED"));
        }
    }
}

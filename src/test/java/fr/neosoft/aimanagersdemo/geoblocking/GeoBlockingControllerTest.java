package fr.neosoft.aimanagersdemo.geoblocking;

import fr.neosoft.aimanagersdemo.access.persistence.SubscriberRepo;
import fr.neosoft.aimanagersdemo.config.ClockConfig;
import fr.neosoft.aimanagersdemo.geoblocking.client.GeoIpClientMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeoBlockingController.class)
@Import({GeoBlockingSvc.class, GeoIpClientMock.class, SubscriberRepo.class, ClockConfig.class})
class GeoBlockingControllerTest {

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
    class CountryRules {

        @Test
        void adultSubscriberCanWatchAdultContentInFrance() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night-movie").param("ip", "81.2.69.1").param("subscriberId", "s1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.decision").value("ALLOWED"));
        }

        @Test
        void minorIsAgeRestricted() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night-movie").param("ip", "81.2.69.1").param("subscriberId", "s2"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.decision").value("AGE_RESTRICTED"));
        }

        @Test
        void adultContentWithoutSubscriberIsRestricted() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night-movie").param("ip", "81.2.69.1"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.decision").value("AGE_RESTRICTED"));
        }

        @Test
        void categoryNotAllowedInCountry() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night-movie").param("ip", "5.5.5.5").param("subscriberId", "s1"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.decision").value("CATEGORY_BLOCKED"));
        }

        @Test
        void unknownSubscriberIsNotFound() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "news-live").param("ip", "81.2.69.1").param("subscriberId", "nobody"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.decision").value("UNKNOWN_SUBSCRIBER"));
        }
    }
}

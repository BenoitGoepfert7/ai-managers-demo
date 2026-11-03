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
                    .andExpect(jsonPath("$.allowed").value(false))
                    .andExpect(jsonPath("$.reason").value("COUNTRY_BLOCKED"));
        }

        @Test
        void ageLimitedContentWithoutSubscriberIsBlocked() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night").param("ip", "81.2.69.1"))
                    .andExpect(status().is(451))
                    .andExpect(jsonPath("$.allowed").value(false))
                    .andExpect(jsonPath("$.reason").value("SUBSCRIBER_REQUIRED"));
        }

        @Test
        void adultSubscriberCanWatchAgeLimitedContent() throws Exception {
            mvc.perform(get("/geo/check").param("contentId", "late-night").param("ip", "81.2.69.1")
                            .param("subscriberId", "s1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.allowed").value(true))
                    .andExpect(jsonPath("$.reason").doesNotExist());
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
}

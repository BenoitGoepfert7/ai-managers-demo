package fr.neosoft.aimanagersdemo.geoblocking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeoBlockingController.class)
@Import({GeoBlockingService.class, MockGeoIpClient.class})
class GeoBlockingControllerTest {

    @Autowired
    MockMvc mvc;

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

    @Test
    void externalApiFailureReturnsBadGateway() throws Exception {
        mvc.perform(get("/geo/check").param("contentId", "news-live").param("ip", "0.0.0.0"))
                .andExpect(status().isBadGateway());
    }
}

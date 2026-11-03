package fr.neosoft.aimanagersdemo.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessController.class)
@Import({AccessService.class, SubscriberRepository.class})
class AccessControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void grantsChannelInPackage() throws Exception {
        mvc.perform(get("/access/s1/channels/sport"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true));
    }

    @Test
    void deniesChannelOutsidePackage() throws Exception {
        mvc.perform(get("/access/s2/channels/SPORT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.decision").value("CHANNEL_NOT_IN_PACKAGE"));
    }

    @Test
    void deniesInactiveSubscription() throws Exception {
        mvc.perform(get("/access/s3/channels/NEWS"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.decision").value("SUBSCRIPTION_INACTIVE"));
    }

    @Test
    void unknownSubscriber() throws Exception {
        mvc.perform(get("/access/nobody/channels/NEWS"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBlankSubscriberId() throws Exception {
        mvc.perform(get(URI.create("/access/%20/channels/NEWS")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankChannel() throws Exception {
        mvc.perform(get(URI.create("/access/s1/channels/%20")))
                .andExpect(status().isBadRequest());
    }
}

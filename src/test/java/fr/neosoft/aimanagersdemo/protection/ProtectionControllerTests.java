package fr.neosoft.aimanagersdemo.protection;

import fr.neosoft.aimanagersdemo.config.ClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProtectionController.class)
@Import({PlaybackTokenService.class, ClockConfig.class})
class ProtectionControllerTests {

    @Autowired
    MockMvc mvc;

    @Test
    void issuedTokenGivesAccessToStream() throws Exception {
        String body = mvc.perform(post("/protection/tokens").param("subscriberId", "s1").param("contentId", "m1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/protection/stream").param("subscriberId", "s1").param("contentId", "m1").param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("stream://m1/master.m3u8"));
    }

    @Test
    void invalidTokenIsForbidden() throws Exception {
        mvc.perform(get("/protection/stream").param("subscriberId", "s1").param("contentId", "m1").param("token", "bad"))
                .andExpect(status().isForbidden());
    }
}

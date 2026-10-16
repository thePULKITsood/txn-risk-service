package com.pulkitsood.risk.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"rate-limit.capacity=2", "rate-limit.refill-per-second=0.001"})
@AutoConfigureMockMvc
class RateLimitApiTest {

    @Autowired
    private MockMvc mvc;

    private static MockHttpServletRequestBuilder check(String clientId) {
        var request = post("/v1/risk/check").contentType(MediaType.APPLICATION_JSON).content("""
                {"transactionId":"t-rl","clientId":"c","cardId":"card-rl",
                 "merchantId":"m-1","amount":10,"country":"IN"}
                """);
        return clientId == null ? request : request.header(RateLimitInterceptor.CLIENT_ID_HEADER, clientId);
    }

    @Test
    void rejectsOnceClientExceedsBurst() throws Exception {
        mvc.perform(check("client-a")).andExpect(status().isOk());
        mvc.perform(check("client-a")).andExpect(status().isOk());
        mvc.perform(check("client-a"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void limitsAreTrackedPerClient() throws Exception {
        mvc.perform(check("client-b")).andExpect(status().isOk());
        mvc.perform(check("client-b")).andExpect(status().isOk());
        mvc.perform(check("client-c")).andExpect(status().isOk());
    }

    @Test
    void missingClientIdIsRejected() throws Exception {
        mvc.perform(check(null)).andExpect(status().isBadRequest());
    }
}

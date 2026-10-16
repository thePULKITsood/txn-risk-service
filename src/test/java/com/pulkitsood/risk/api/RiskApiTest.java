package com.pulkitsood.risk.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void checkReturnsDecision() throws Exception {
        mvc.perform(post("/v1/risk/check").header("X-Client-Id", "api-test").contentType(MediaType.APPLICATION_JSON).content("""
                        {"transactionId":"t-1","clientId":"client-1","cardId":"card-api-1",
                         "merchantId":"m-sketchy-001","amount":120.00,"country":"IN"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("t-1"))
                .andExpect(jsonPath("$.decision").value("BLOCK"))
                .andExpect(jsonPath("$.hits[0].ruleId").value("blocklist"));
    }

    @Test
    void invalidTransactionIsRejected() throws Exception {
        mvc.perform(post("/v1/risk/check").header("X-Client-Id", "api-test").contentType(MediaType.APPLICATION_JSON).content("""
                        {"transactionId":"t-2","clientId":"client-1","cardId":"card-api-2",
                         "merchantId":"m-1","amount":-5,"country":"IND"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void malformedJsonIsRejected() throws Exception {
        mvc.perform(post("/v1/risk/check").header("X-Client-Id", "api-test").contentType(MediaType.APPLICATION_JSON).content("{nope"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed JSON body"));
    }

    @Test
    void adminListsActiveRules() throws Exception {
        mvc.perform(get("/v1/admin/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rules.length()").value(3));
    }

    @Test
    void adminRejectsReviewAboveBlock() throws Exception {
        mvc.perform(put("/v1/admin/rules").contentType(MediaType.APPLICATION_JSON).content("""
                        {"reviewAmount":500000,"blockAmount":1000,"velocityMaxTransactions":5,
                         "velocityWindow":"PT10M","blockedMerchants":[],"blockedCountries":[]}
                        """))
                .andExpect(status().isBadRequest());
    }
}

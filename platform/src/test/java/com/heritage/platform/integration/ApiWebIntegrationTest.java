package com.heritage.platform.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Import(TestConfig.class)
@Transactional
class ApiWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicCategories_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void publicProvinces_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void publicHeritageTypeGroups_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/heritage-type-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void publicResources_defaultPage_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/resources").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void publicResources_onlyEraFrom_returnsBusinessErrorInBody() throws Exception {
        mockMvc.perform(get("/api/public/resources").param("eraFrom", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void registerThenLogin_returnsToken() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "itest_" + suffix;
        String email = "itest_" + suffix + "@example.com";
        String password = "TestPass123!";

        String registerJson = """
                {"username":"%s","email":"%s","password":"%s"}
                """.formatted(username, email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String loginJson = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.username").value(username));
    }
}

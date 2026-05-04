package com.heritage.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heritage.platform.entity.Role;
import com.heritage.platform.model.Category;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.HeritageUserRepository;

/**
 * 贡献者资源 HTTP 集成测试：创建草稿 → 提交审核 → 我的提交列表。
 * 使用 {@code test} profile + 内存 H2（与 {@link ResourceServiceIntegrationTest} 一致），避免本地 MySQL
 * {@code resources.contributor_id} 误指向 {@code users} 等环境问题；与 {@link ApiWebIntegrationTest}（MySQL）、
 * {@link PublicDiscoveryWebIntegrationTest}（访客大厅）、{@link AuthAccountWebIntegrationTest}（认证账号，H2）形成互补。
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class ContributorResourceWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String loginAsContributor(String suffix) throws Exception {
        String username = "contrib_api_" + suffix;
        String email = "contrib_api_" + suffix + "@example.test";
        String rawPassword = "ContribPass123!";

        HeritageUser user = new HeritageUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName("API Contrib " + suffix);
        user.setRoles(Set.of(Role.CONTRIBUTOR.name()));
        userRepository.saveAndFlush(user);

        String loginJson = """
                {"username":"%s","password":"%s"}
                """.formatted(username, rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    @Test
    void createDraft_submitAndListSubmissions_flow() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = loginAsContributor(suffix);

        Category category = new Category();
        category.setName("ContribCat-" + suffix);
        category.setDescription("for contributor api test");
        category = categoryRepository.saveAndFlush(category);

        String createBody = """
                {
                  "title": "Draft from integration test",
                  "description": "Body text",
                  "locationName": "Test City",
                  "categoryId": %d,
                  "copyrightDeclaration": "CC BY"
                }
                """.formatted(category.getId());

        MvcResult createResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long resourceId = created.path("data").path("id").asLong();

        mockMvc.perform(post("/api/resources/{id}/submit", resourceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"));

        MvcResult subResult = mockMvc.perform(get("/api/resources/submissions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        JsonNode list = objectMapper.readTree(subResult.getResponse().getContentAsString()).path("data");
        assertThat(list.isArray()).isTrue();
        boolean found = false;
        for (JsonNode row : list) {
            if (row.path("id").asLong() == resourceId
                    && "PENDING_REVIEW".equals(row.path("status").asText())
                    && "Draft from integration test".equals(row.path("title").asText())) {
                found = true;
                break;
            }
        }
        assertThat(found).as("submissions 应包含刚提交的资源").isTrue();
    }

    @Test
    void createDraft_withoutToken_returnsUnauthorized() throws Exception {
        String body = """
                {
                  "title": "Should fail",
                  "description": "x",
                  "locationName": "x",
                  "categoryId": 1,
                  "copyrightDeclaration": "CC BY"
                }
                """;

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}

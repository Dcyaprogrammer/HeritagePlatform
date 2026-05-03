package com.heritage.platform.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.security.JwtUtil;

/**
 * 认证与账号 HTTP 集成测试：注册、登录、当前用户、忘记/重置密码、资料与改密、会话列表与注销。
 * 使用 {@code test} profile + 内存 H2，与 {@link PublicDiscoveryWebIntegrationTest} 一致。
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthAccountWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private void registerUser(String username, String email, String password) throws Exception {
        String body = """
                {"username":"%s","email":"%s","password":"%s"}
                """.formatted(username, email, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("token").asText();
    }

    private static MockHttpServletRequestBuilder bearer(MockHttpServletRequestBuilder req, String token) {
        return req.header("Authorization", "Bearer " + token);
    }

    @Test
    void registerThenLogin_returnsTokenUsernameAndRoles() throws Exception {
        String suffix = uniqueSuffix();
        String username = "auth_it_" + suffix;
        String email = "auth_it_" + suffix + "@example.com";
        String password = "TestPass123!";
        registerUser(username, email, password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void register_duplicateUsername_returns400() throws Exception {
        String suffix = uniqueSuffix();
        String username = "dup_u_" + suffix;
        registerUser(username, "a_" + suffix + "@example.com", "TestPass123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"b_%s@example.com","password":"TestPass123!"}
                                """.formatted(username, suffix)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        String suffix = uniqueSuffix();
        String email = "dup_e_" + suffix + "@example.com";
        registerUser("user1_" + suffix, email, "TestPass123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"user2_%s","email":"%s","password":"TestPass123!"}
                                """.formatted(suffix, email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("邮箱已存在"));
    }

    @Test
    void login_wrongPassword_returns400() throws Exception {
        String suffix = uniqueSuffix();
        String username = "badpwd_" + suffix;
        registerUser(username, "badpwd_" + suffix + "@example.com", "TestPass123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"WrongPass999!"}
                                """.formatted(username)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_withBearer_returnsProfile() throws Exception {
        String suffix = uniqueSuffix();
        String username = "me_" + suffix;
        String email = "me_" + suffix + "@example.com";
        String password = "TestPass123!";
        registerUser(username, email, password);
        String token = loginAndGetToken(username, password);
        mockMvc.perform(bearer(get("/api/auth/me"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.displayName").exists())
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void forgotPassword_unknownEmail_stillReturns200() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody_" + uniqueSuffix() + "@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void validateResetToken_invalid_returns400() throws Exception {
        mockMvc.perform(get("/api/auth/reset-password").param("token", "not-a-real-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid or expired reset token"));
    }

    @Test
    void forgotPassword_thenValidateAndReset_thenLoginWithNewPassword() throws Exception {
        String suffix = uniqueSuffix();
        String username = "reset_" + suffix;
        String email = "reset_" + suffix + "@example.com";
        String oldPassword = "TestPass123!";
        String newPassword = "ResetNew456!";
        registerUser(username, email, oldPassword);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        HeritageUser user = userRepository.findByEmail(email).orElseThrow();
        String resetToken = user.getResetToken();
        assertThat(resetToken).isNotBlank();

        mockMvc.perform(get("/api/auth/reset-password").param("token", resetToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.token").value(resetToken));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","newPassword":"%s"}
                                """.formatted(resetToken, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void resetPassword_invalidToken_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"invalid-token\",\"newPassword\":\"AnyNew999!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("重置链接无效或已过期"));
    }

    @Test
    void getUserByUsername_selfWithBearer_returnsUserDto() throws Exception {
        String suffix = uniqueSuffix();
        String username = "guser_" + suffix;
        registerUser(username, "guser_" + suffix + "@example.com", "TestPass123!");
        String token = loginAndGetToken(username, "TestPass123!");
        mockMvc.perform(bearer(get("/api/user/" + username), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(username));
    }

    @Test
    void putProfile_updatesDisplayName() throws Exception {
        String suffix = uniqueSuffix();
        String username = "prof_" + suffix;
        registerUser(username, "prof_" + suffix + "@example.com", "TestPass123!");
        String token = loginAndGetToken(username, "TestPass123!");
        mockMvc.perform(bearer(put("/api/user/" + username), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"展示名_" + suffix + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.displayName").value("展示名_" + suffix));
    }

    @Test
    void putProfile_otherUser_forbidden() throws Exception {
        String suffix = uniqueSuffix();
        String u1 = "p1_" + suffix;
        String u2 = "p2_" + suffix;
        registerUser(u1, "p1_" + suffix + "@example.com", "TestPass123!");
        registerUser(u2, "p2_" + suffix + "@example.com", "TestPass123!");
        String token1 = loginAndGetToken(u1, "TestPass123!");
        mockMvc.perform(bearer(put("/api/user/" + u2), token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"hack\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_thenLoginWithNewPassword() throws Exception {
        String suffix = uniqueSuffix();
        String username = "cpw_" + suffix;
        String oldPassword = "TestPass123!";
        String newPassword = "Changed456!";
        registerUser(username, "cpw_" + suffix + "@example.com", oldPassword);
        String token = loginAndGetToken(username, oldPassword);
        mockMvc.perform(bearer(put("/api/user/" + username + "/password"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"%s","newPassword":"%s"}
                                """.formatted(oldPassword, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, newPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void changePassword_wrongOldPassword_returns400() throws Exception {
        String suffix = uniqueSuffix();
        String username = "cow_" + suffix;
        registerUser(username, "cow_" + suffix + "@example.com", "TestPass123!");
        String token = loginAndGetToken(username, "TestPass123!");
        mockMvc.perform(bearer(put("/api/user/" + username + "/password"), token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"oldPassword":"WrongOld!","newPassword":"New999!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Old password is incorrect"));
    }

    @Test
    void listSessions_withBearer_returnsArray() throws Exception {
        String suffix = uniqueSuffix();
        String username = "sess_" + suffix;
        registerUser(username, "sess_" + suffix + "@example.com", "TestPass123!");
        String token = loginAndGetToken(username, "TestPass123!");
        mockMvc.perform(bearer(get("/api/sessions"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].jti").exists());
    }

    @Test
    void terminateSession_thenMeReturns401() throws Exception {
        String suffix = uniqueSuffix();
        String username = "term_" + suffix;
        registerUser(username, "term_" + suffix + "@example.com", "TestPass123!");
        String token = loginAndGetToken(username, "TestPass123!");
        String jti = jwtUtil.extractJti(token);

        mockMvc.perform(bearer(delete("/api/sessions/" + jti), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(bearer(get("/api/auth/me"), token))
                .andExpect(status().isUnauthorized());
    }
}

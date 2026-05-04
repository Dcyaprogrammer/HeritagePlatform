package com.heritage.platform.integration;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 公开访客大厅 HTTP 集成测试：{@code /api/public/**} 与公开评论列表。
 * 使用 {@code test} profile + 内存 H2，与 {@link ContributorResourceWebIntegrationTest} 一致，避免强依赖本地 MySQL。
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class PublicDiscoveryWebIntegrationTest {

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
    void publicTags_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void publicDynasties_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/dynasties"))
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
    void publicResources_eraFromAfterEraTo_returnsBusinessErrorInBody() throws Exception {
        mockMvc.perform(get("/api/public/resources")
                        .param("eraFrom", "2020-12-31")
                        .param("eraTo", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("起始时间不能晚于截止时间"));
    }

    @Test
    void publicResources_invalidDynastyCode_returnsBusinessErrorInBody() throws Exception {
        mockMvc.perform(get("/api/public/resources").param("dynastyCode", "NOT_A_REAL_DYNASTY_CODE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效的朝代代码"));
    }

    @Test
    void publicResources_invalidProvinceCode_returnsBusinessErrorInBody() throws Exception {
        mockMvc.perform(get("/api/public/resources").param("provinceCode", "NOT_A_PROVINCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效的地区代码"));
    }

    @Test
    void publicResources_invalidHeritageTypeCode_returnsBusinessErrorInBody() throws Exception {
        mockMvc.perform(get("/api/public/resources").param("heritageTypeCode", "INVALID_TYPE_XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效的文物类型代码"));
    }

    @Test
    void publicResources_withDynastyCodeTang_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/resources")
                        .param("dynastyCode", "TANG")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void publicResources_withProvinceBj_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/resources")
                        .param("provinceCode", "BJ")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void publicResources_withEraRange_returnsOkEnvelope() throws Exception {
        mockMvc.perform(get("/api/public/resources")
                        .param("eraFrom", "1000-01-01")
                        .param("eraTo", "2000-01-01")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.total").isNumber());
    }

    @Test
    void publicResourceDetail_unknownId_returnsOkWithNullData() throws Exception {
        mockMvc.perform(get("/api/public/resources/999999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    void publicResourceComments_unknownResource_returnsOkEmptyOrList() throws Exception {
        mockMvc.perform(get("/api/public/resources/999999999999/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}

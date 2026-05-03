package com.heritage.platform.discovery.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link TaxonomyCatalog} 单元测试：朝代/地区/文物类型归一化、年代推断、SQL 区间与地点解析（无 Spring）。
 */
class TaxonomyCatalogTest {

    private TaxonomyCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new TaxonomyCatalog();
    }

    @Test
    void normalizeDynastyCode_caseInsensitive() {
        assertThat(catalog.normalizeDynastyCode("TANG")).contains("TANG");
        assertThat(catalog.normalizeDynastyCode("tang")).contains("TANG");
    }

    @Test
    void normalizeDynastyCode_invalid_empty() {
        assertThat(catalog.normalizeDynastyCode("NOT_A_CODE")).isEmpty();
        assertThat(catalog.normalizeDynastyCode("")).isEmpty();
        assertThat(catalog.normalizeDynastyCode(null)).isEmpty();
    }

    @Test
    void normalizeProvinceCode_bj() {
        assertThat(catalog.normalizeProvinceCode("BJ")).contains("BJ");
        assertThat(catalog.normalizeProvinceCode("bj")).contains("BJ");
    }

    @Test
    void resolveHeritageTypeCodes_groupExpandsToLeaves() {
        Optional<List<String>> leaves = catalog.resolveHeritageTypeCodesForFilter("HTG_RITE");
        assertThat(leaves).isPresent();
        assertThat(leaves.get()).contains("RIT_BRONZE", "RIT_JADE");
    }

    @Test
    void resolveHeritageTypeCodes_leafIsSingleton() {
        Optional<List<String>> one = catalog.resolveHeritageTypeCodesForFilter("RIT_JADE");
        assertThat(one).isPresent();
        assertThat(one.get()).containsExactly("RIT_JADE");
    }

    @Test
    void inferDynastyCodeFromGregorianYear_tangAndPrc() {
        assertThat(catalog.inferDynastyCodeFromGregorianYear(650)).isEqualTo("TANG");
        assertThat(catalog.inferDynastyCodeFromGregorianYear(2024)).isEqualTo("PRC");
        assertThat(catalog.inferDynastyCodeFromGregorianYear(999_999)).isEqualTo("UNKNOWN");
    }

    @Test
    void sqlInclusiveYearRangeForDynastyFilter_tangAndUnknown() {
        assertThat(catalog.sqlInclusiveYearRangeForDynastyFilter("TANG").orElseThrow()).containsExactly(618, 907);
        assertThat(catalog.sqlInclusiveYearRangeForDynastyFilter("UNKNOWN")).isEmpty();
        assertThat(catalog.sqlInclusiveYearRangeForDynastyFilter("")).isEmpty();
    }

    @Test
    void inferProvinceCodeFromLocation_containsEnglishName() {
        assertThat(catalog.inferProvinceCodeFromLocation("Museum in Henan")).contains("HA");
        assertThat(catalog.inferProvinceCodeFromLocation("  ")).isEmpty();
    }

    @Test
    void provinceLocationLikePattern_bj_containsCoreName() {
        assertThat(catalog.provinceLocationLikePattern("BJ")).contains("Beijing");
    }

    @Test
    void getHeritageTypeGroups_nonEmpty() {
        assertThat(catalog.getHeritageTypeGroups()).isNotEmpty();
    }
}

package com.heritage.platform.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.heritage.platform.discovery.dto.PublicResourceDetail;
import com.heritage.platform.discovery.dto.PublicResourceSummary;
import com.heritage.platform.discovery.dto.SlicePage;
import com.heritage.platform.entity.Role;
import com.heritage.platform.model.Category;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;

/**
 * {@link PublicDiscoveryService} 集成测试：标签 id 解析、只读列表/检索/详情（依赖 H2 与 {@link com.heritage.platform.discovery.taxonomy.TaxonomyCatalog}）。
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
@Transactional
class PublicDiscoveryServiceTest {

    @Autowired
    private PublicDiscoveryService discoveryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private HeritageResourceRepository resourceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void parseTagIds_nullAndBlank_empty() {
        assertThat(PublicDiscoveryService.parseTagIds(null)).isEmpty();
        assertThat(PublicDiscoveryService.parseTagIds("")).isEmpty();
        assertThat(PublicDiscoveryService.parseTagIds("   ")).isEmpty();
    }

    @Test
    void parseTagIds_skipsInvalidSegments() {
        assertThat(PublicDiscoveryService.parseTagIds("1, 2, x, 3")).containsExactly(1L, 2L, 3L);
    }

    @Test
    void listCategories_includesPersistedCategory() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Category c = new Category();
        c.setName("DiscoveryCat_" + suffix);
        c.setDescription("for discovery test");
        categoryRepository.save(c);
        entityManager.flush();

        assertThat(discoveryService.listCategories())
                .extracting(com.heritage.platform.discovery.dto.NamedRow::getName)
                .contains("DiscoveryCat_" + suffix);
    }

    @Test
    void search_byKeyword_andDynasty_andProvince_andDetail() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String keywordToken = "TripodToken_" + suffix;

        HeritageUser user = new HeritageUser();
        user.setUsername("disc_user_" + suffix);
        user.setPasswordHash("x");
        user.setDisplayName("Disc");
        user.setEmail("disc_" + suffix + "@t.test");
        user.setRoles(Set.of(Role.VIEWER.name()));
        user = userRepository.save(user);

        Category category = new Category();
        category.setName("DiscCat_" + suffix);
        category = categoryRepository.save(category);

        HeritageResource r = new HeritageResource();
        r.setTitle("Bronze " + keywordToken);
        r.setDescription("desc");
        r.setLocationName("Luoyang area, Henan");
        r.setHeritageTypeCode("RIT_BRONZE");
        r.setCategory(category.getName());
        r.setCategoryId(category.getId());
        r.setCopyrightDeclaration("cc");
        r.setSubmittedAt(Instant.now());
        r.setStatus(ResourceStatus.APPROVED);
        r.setSubmitter(user);
        resourceRepository.save(r);
        entityManager.flush();

        Long id = r.getId();
        entityManager.createNativeQuery("UPDATE resources SET created_at = ? WHERE id = ?")
                .setParameter(1, Timestamp.valueOf(LocalDateTime.of(650, 6, 15, 10, 0)))
                .setParameter(2, id)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        SlicePage<PublicResourceSummary> byKw =
                discoveryService.search(keywordToken, null, List.of(), null, null, null, null, null, 0, 20);
        assertThat(byKw.getTotal()).isGreaterThanOrEqualTo(1);

        SlicePage<PublicResourceSummary> byDynasty = discoveryService.search(
                null, null, List.of(), List.of("TANG"), null, null, null, null, 0, 20);
        assertThat(byDynasty.getItems().stream().anyMatch(row -> row.getId().equals(id))).isTrue();

        SlicePage<PublicResourceSummary> byProvince = discoveryService.search(
                null, null, List.of(), null, null, null, List.of("HA"), null, 0, 20);
        assertThat(byProvince.getItems().stream().anyMatch(row -> row.getId().equals(id))).isTrue();

        Optional<PublicResourceDetail> detail = discoveryService.findApprovedDetail(id);
        assertThat(detail).isPresent();
        assertThat(detail.get().getTitle()).contains(keywordToken);
        assertThat(detail.get().getDynastyCode()).isEqualTo("TANG");
        assertThat(detail.get().getProvinceCode()).isEqualTo("HA");
    }

    @Test
    void findApprovedDetail_missing_returnsEmpty() {
        assertThat(discoveryService.findApprovedDetail(9_999_999_999L)).isEmpty();
    }

    @Test
    void search_clampsPageSize() {
        SlicePage<PublicResourceSummary> page =
                discoveryService.search(null, null, List.of(), null, null, null, null, null, 0, 500);
        assertThat(page.getSize()).isLessThanOrEqualTo(100);
    }
}

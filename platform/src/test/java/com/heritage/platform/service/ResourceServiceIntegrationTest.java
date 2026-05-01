package com.heritage.platform.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.heritage.platform.dto.ResourceDTO;
import com.heritage.platform.dto.ResourceDraftRequest;
import com.heritage.platform.model.Attachment;
import com.heritage.platform.model.Category;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.repository.AttachmentRepository;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;

@SpringBootTest
@Transactional
class ResourceServiceIntegrationTest {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private HeritageResourceRepository resourceRepository;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void updateDraft_preservesExistingAttachmentsForRejectedResource() {
        String suffix = UUID.randomUUID().toString().replace("-", "");

        HeritageUser submitter = new HeritageUser();
        submitter.setUsername("attachment-regression-" + suffix);
        submitter.setPasswordHash("secret");
        submitter.setDisplayName("Regression Tester");
        submitter.setRoles(Set.of("ROLE_CONTRIBUTOR"));
        submitter = userRepository.save(submitter);

        Category category = new Category();
        category.setName("Regression Category " + suffix);
        category.setDescription("Attachment regression test category");
        category = categoryRepository.save(category);

        HeritageResource resource = new HeritageResource();
        resource.setTitle("Original title");
        resource.setDescription("Original description");
        resource.setLocationName("Original location");
        resource.setCategory(category.getName());
        resource.setCategoryId(category.getId());
        resource.setCopyrightDeclaration("CC BY");
        resource.setStatus(ResourceStatus.REJECTED);
        resource.setSubmittedAt(Instant.now());
        resource.setSubmitter(submitter);
        resource.setRejectionReason("Needs changes");

        Attachment attachment = new Attachment();
        attachment.setStoredName("kept-image-" + suffix + ".jpg");
        attachment.setDisplayName("kept-image.jpg");
        attachment.setFilePath("/uploads/kept-image-" + suffix + ".jpg");
        attachment.setFileType("image");
        attachment.setFileSize(1024L);
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setResource(resource);
        resource.getAttachments().add(attachment);

        resource = resourceRepository.saveAndFlush(resource);
        Long resourceId = resource.getId();
        Long attachmentId = resource.getAttachments().iterator().next().getId();
        Long version = resource.getVersion();

        entityManager.clear();

        ResourceDraftRequest request = new ResourceDraftRequest();
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setLocationName("Updated location");
        request.setCategory(category.getName());
        request.setCategoryId(category.getId());
        request.setCopyrightDeclaration("CC BY-SA");
        request.setVersion(version);
        request.setAttachmentIds(List.of(attachmentId));

        ResourceDTO updated = resourceService.updateDraft(resourceId, request, submitter.getUsername());

        entityManager.flush();
        entityManager.clear();

        HeritageResource refreshed = resourceRepository.findById(resourceId).orElseThrow();
        Attachment refreshedAttachment = attachmentRepository.findById(attachmentId).orElseThrow();

        assertThat(refreshed.getAttachments()).hasSize(1);
        assertThat(refreshedAttachment.getResource()).isNotNull();
        assertThat(refreshedAttachment.getResource().getId()).isEqualTo(resourceId);
        assertThat(updated.getAttachments()).hasSize(1);
        assertThat(updated.getAttachments().get(0).get("id")).isEqualTo(attachmentId);
    }
}

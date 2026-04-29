package com.heritage.platform.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heritage.platform.dto.ResourceDTO;
import com.heritage.platform.dto.ResourceDraftRequest;
import com.heritage.platform.model.Attachment;
import com.heritage.platform.model.Category;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.model.Tag;
import com.heritage.platform.repository.AttachmentRepository;
import com.heritage.platform.repository.CategoryRepository;
import com.heritage.platform.repository.HeritageResourceRepository;
import com.heritage.platform.repository.HeritageUserRepository;
import com.heritage.platform.repository.TagRepository;

@Service
public class ResourceService {

    @Autowired
    private HeritageResourceRepository resourceRepository;

    @Autowired
    private HeritageUserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public ResourceDTO createDraft(ResourceDraftRequest request, String username) {
        HeritageUser submitter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        HeritageResource resource = new HeritageResource();

        mapRequestToEntity(request, resource);

        resource.setStatus(ResourceStatus.DRAFT);
        resource.setSubmitter(submitter);

        handleTagsAndAttachments(request, resource);

        HeritageResource savedResource = resourceRepository.save(resource);
        return convertToDTO(savedResource);
    }

    @Transactional(readOnly = true)
    public ResourceDTO getOwnedResource(Long id, String username) {
        HeritageResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getSubmitter().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only view your own resources");
        }

        return convertToDTO(resource);
    }

    @Transactional
    public void deleteOwnedResource(Long id, String username) {
        HeritageResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getSubmitter().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only delete your own resources");
        }

        if (resource.getStatus() == ResourceStatus.APPROVED
                || resource.getStatus() == ResourceStatus.PENDING_REVIEW) {
            throw new RuntimeException("Approved or pending-review resources cannot be deleted");
        }

        deleteAttachmentFiles(resource);
        resourceRepository.delete(resource);
    }


    @Transactional
    public ResourceDTO updateDraft(Long id, ResourceDraftRequest request, String username) {
        HeritageResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getSubmitter().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only edit your own resources");
        }

        if (resource.getStatus() != ResourceStatus.DRAFT && resource.getStatus() != ResourceStatus.REJECTED) {
            throw new RuntimeException("Cannot edit resource in " + resource.getStatus() + " status");
        }

        if (request.getVersion() != null && !request.getVersion().equals(resource.getVersion())) {
            throw new RuntimeException("Conflict: Resource has been updated by someone else. Please refresh and try again.");
        }

        mapRequestToEntity(request, resource);

        handleTagsAndAttachments(request, resource);

        HeritageResource updatedResource = resourceRepository.save(resource);
        return convertToDTO(updatedResource);
    }


    @Transactional
    public ResourceDTO submitForReview(Long id, String username) {
        HeritageResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getSubmitter().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized: You can only submit your own resources");
        }

        if (resource.getStatus() != ResourceStatus.DRAFT && resource.getStatus() != ResourceStatus.REJECTED) {
            throw new RuntimeException("Only DRAFT or REJECTED resources can be submitted");
        }

        resource.setStatus(ResourceStatus.PENDING_REVIEW);
        resource.setSubmittedAt(Instant.now());

        HeritageResource submittedResource = resourceRepository.save(resource);
        return convertToDTO(submittedResource);
    }


    private void mapRequestToEntity(ResourceDraftRequest request, HeritageResource resource) {
        resource.setTitle(request.getTitle());
        resource.setDescription(request.getDescription());
        resource.setLocationName(request.getLocationName());
        resource.setHeritageTypeCode(request.getHeritageTypeCode());
        Integer categoryId = request.getCategoryId();
        if (categoryId == null) {
            throw new RuntimeException("categoryId is required");
        }
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Invalid categoryId"));
        resource.setCategoryId(category.getId());
        // keep legacy category string field for existing DTO/review list compatibility
        resource.setCategory(category.getName());
        resource.setCopyrightDeclaration(request.getCopyrightDeclaration());
    }

    private void handleTagsAndAttachments(ResourceDraftRequest request, HeritageResource resource) {
        resource.getTags().clear();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Long> tagIds = request.getTagIds().stream().distinct().toList();
            List<Tag> tags = tagRepository.findAllById(tagIds);
            if (tags.size() != tagIds.size()) {
                throw new RuntimeException("Invalid tagIds");
            }
            for (Tag tag : tags) {
                resource.addTag(tag);
            }
        }

        List<Long> attachmentIds = request.getAttachmentIds() == null
                ? List.of()
                : request.getAttachmentIds().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Long, Attachment> requestedAttachments = attachmentIds.isEmpty()
                ? Map.of()
                : attachmentRepository.findAllById(attachmentIds).stream()
                        .collect(Collectors.toMap(Attachment::getId, Function.identity()));

        if (requestedAttachments.size() != attachmentIds.size()) {
            throw new RuntimeException("Invalid attachmentIds");
        }

        resource.getAttachments().removeIf(existing -> {
            Long existingId = existing.getId();
            boolean keep = existingId != null && requestedAttachments.containsKey(existingId);
            if (!keep) {
                existing.setResource(null);
            }
            return !keep;
        });

        Set<Long> currentAttachmentIds = resource.getAttachments().stream()
                .map(Attachment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));

        for (Long attachmentId : attachmentIds) {
            if (currentAttachmentIds.contains(attachmentId)) {
                continue;
            }

            Attachment attachment = requestedAttachments.get(attachmentId);
            HeritageResource owner = attachment.getResource();
            if (owner != null && owner.getId() != null && !owner.getId().equals(resource.getId())) {
                throw new RuntimeException("Attachment does not belong to this resource");
            }
            HeritageUser uploader = attachment.getUploader();
            if (uploader != null && !uploader.getUsername().equals(resource.getSubmitter().getUsername())) {
                throw new RuntimeException("Attachment was uploaded by another user");
            }

            attachment.setResource(resource);
            resource.getAttachments().add(attachment);
        }
    }

    private void deleteAttachmentFiles(HeritageResource resource) {
        for (Attachment attachment : resource.getAttachments()) {
            String storedName = attachment.getStoredName();
            if (storedName == null || storedName.isBlank()) {
                continue;
            }

            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", storedName);
            try {
                Files.deleteIfExists(filePath);
            } catch (java.io.IOException ignored) {
                // Do not block resource deletion because of leftover files on disk.
            }
        }
    }

    private ResourceDTO convertToDTO(HeritageResource resource) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setLocationName(resource.getLocationName());
        dto.setHeritageTypeCode(resource.getHeritageTypeCode());
        dto.setCategory(resource.getCategory());
        dto.setCategoryId(resource.getCategoryId());
        dto.setCopyrightDeclaration(resource.getCopyrightDeclaration());
        
        dto.setStatus(resource.getStatus().name());
        dto.setSubmittedAt(resource.getSubmittedAt());
        dto.setRejectionReason(resource.getRejectionReason());
        dto.setVersion(resource.getVersion());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setUpdatedAt(resource.getUpdatedAt());

        if (resource.getSubmitter() != null) {
            dto.setSubmitterId(resource.getSubmitter().getId());
            dto.setSubmitterName(resource.getSubmitter().getDisplayName());
        }

        List<Map<String, Object>> tagsList = new ArrayList<>();
        for (Tag tag : resource.getTags()) {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("id", tag.getId());
            tagMap.put("name", tag.getName());
            tagsList.add(tagMap);
        }
        dto.setTags(tagsList);

        List<Map<String, Object>> attachmentsList = new ArrayList<>();
        for (Attachment attachment : resource.getAttachments()) {
            Map<String, Object> attMap = new HashMap<>();
            attMap.put("id", attachment.getId());
            attMap.put("storedName", attachment.getStoredName());
            attMap.put("displayName", attachment.getDisplayName());
            attMap.put("filePath", attachment.getFilePath());
            attMap.put("fileType", attachment.getFileType());
            attMap.put("fileSize", attachment.getFileSize());
            attachmentsList.add(attMap);
        }
        dto.setAttachments(attachmentsList);

        return dto;
    }
}

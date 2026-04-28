package com.heritage.platform.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.heritage.platform.dto.ResourceDTO;
import com.heritage.platform.dto.ResourceDraftRequest;
import com.heritage.platform.model.Attachment;
import com.heritage.platform.model.HeritageResource;
import com.heritage.platform.model.HeritageUser;
import com.heritage.platform.model.ResourceStatus;
import com.heritage.platform.model.Tag;
import com.heritage.platform.repository.AttachmentRepository;
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
        resource.setCategory(request.getCategory());
        resource.setCategoryId(request.getCategoryId());
        resource.setCopyrightDeclaration(request.getCopyrightDeclaration());
    }

    private void handleTagsAndAttachments(ResourceDraftRequest request, HeritageResource resource) {
        resource.getTags().clear();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            for (Tag tag : tags) {
                resource.addTag(tag);
            }
        }

        resource.getAttachments().clear();
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<Attachment> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            for (Attachment attachment : attachments) {
                attachment.setResource(resource);
                resource.getAttachments().add(attachment);
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

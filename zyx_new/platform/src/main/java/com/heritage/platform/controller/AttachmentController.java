package com.heritage.platform.web;

import com.heritage.platform.model.Attachment;
import com.heritage.platform.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = "http://localhost:5173")
public class AttachmentController {
    private static final Logger log = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private final String CHUNK_DIR = System.getProperty("user.dir") + "/uploads/chunks/";

    // ----- Standard single upload -----
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("========== Upload method called ==========");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {}", file.getSize());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create upload directory
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Get original file name and extension
            String originalName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalName != null && originalName.contains(".")) {
                fileExtension = originalName.substring(originalName.lastIndexOf("."));
            }
            
            // Generate unique file name for storage
            String storedName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;
            
            // Save file to disk
            Path filePath = Paths.get(UPLOAD_DIR + storedName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved successfully, path: {}", filePath.toString());
            
            // Determine file type
            String fileType = "document";
            String lowerName = originalName != null ? originalName.toLowerCase() : "";
            if (file.getContentType() != null && file.getContentType().startsWith("image")) {
                fileType = "image";
            } else if (lowerName.endsWith(".pdf")) {
                fileType = "pdf";
            } else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) {
                fileType = "word";
            } else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".avi")) {
                fileType = "video";
            } else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a")) {
                fileType = "audio";
            }
            
            // Save to database
            Attachment attachment = new Attachment();
            attachment.setStoredName(storedName);
            attachment.setDisplayName(originalName);
            attachment.setFilePath("/uploads/" + storedName);
            attachment.setFileType(fileType);
            attachment.setFileSize(file.getSize());
            attachment.setCreatedAt(LocalDateTime.now());
            attachment.setResourceId(null); // 暂设为 null，对接组员4后改为真实 resource_id
            attachmentRepository.save(attachment);
            
            log.info("Database saved successfully, ID: {}", attachment.getId());
            
            // Return success response (displayName for frontend display)
            response.put("success", true);
            response.put("attachmentId", attachment.getId());
            response.put("displayName", originalName);
            response.put("storedName", storedName);
            response.put("filePath", "/uploads/" + storedName);
            response.put("previewUrl", "/api/attachments/" + attachment.getId() + "/preview");
            response.put("downloadUrl", "/api/attachments/" + attachment.getId() + "/download");
            response.put("fileSize", file.getSize());
            response.put("message", "Upload success");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ----- Chunked upload: receive one chunk -----
    @PostMapping("/upload/chunk")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path chunkDir = Paths.get(CHUNK_DIR, uploadId);
            Files.createDirectories(chunkDir);
            Path chunkFile = chunkDir.resolve(chunkIndex + ".part");
            file.transferTo(chunkFile.toFile());
            log.info("Chunk {}/{} saved for uploadId={}", chunkIndex + 1, totalChunks, uploadId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Chunk upload failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Chunk upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ----- Chunked upload: merge all chunks -----
    @PostMapping("/upload/merge")
    public ResponseEntity<Map<String, Object>> mergeChunks(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileName") String fileName,
            @RequestParam("totalChunks") int totalChunks) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path chunkDir = Paths.get(CHUNK_DIR, uploadId);
            if (!Files.exists(chunkDir)) {
                response.put("success", false);
                response.put("message", "Chunk directory not found");
                return ResponseEntity.status(400).body(response);
            }

            // Generate unique storage name
            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String storedName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;
            Path targetFile = Paths.get(UPLOAD_DIR, storedName);

            // Merge chunks in order
            try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
                for (int i = 0; i < totalChunks; i++) {
                    Path chunk = chunkDir.resolve(i + ".part");
                    if (!Files.exists(chunk)) {
                        log.error("Missing chunk {}/{} for uploadId={}", i, totalChunks, uploadId);
                        response.put("success", false);
                        response.put("message", "Missing chunk " + i);
                        return ResponseEntity.status(400).body(response);
                    }
                    Files.copy(chunk, fos);
                }
            }

            // Clean up chunk directory
            deleteDirectory(chunkDir);

            long fileSize = Files.size(targetFile);
            log.info("Merged file saved: {} ({} bytes)", storedName, fileSize);

            // Determine file type
            String fileType = "document";
            String lowerName = fileName != null ? fileName.toLowerCase() : "";
            if (lowerName.endsWith(".pdf")) fileType = "pdf";
            else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) fileType = "word";
            else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".avi")) fileType = "video";
            else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a")) fileType = "audio";
            else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")) fileType = "image";

            // Save to database
            Attachment attachment = new Attachment();
            attachment.setStoredName(storedName);
            attachment.setDisplayName(fileName);
            attachment.setFilePath("/uploads/" + storedName);
            attachment.setFileType(fileType);
            attachment.setFileSize(fileSize);
            attachment.setCreatedAt(LocalDateTime.now());
            attachment.setResourceId(null); // 暂设为 null，对接组员4后改为真实 resource_id
            attachmentRepository.save(attachment);

            response.put("success", true);
            response.put("attachmentId", attachment.getId());
            response.put("displayName", fileName);
            response.put("storedName", storedName);
            response.put("filePath", "/uploads/" + storedName);
            response.put("previewUrl", "/api/attachments/" + attachment.getId() + "/preview");
            response.put("downloadUrl", "/api/attachments/" + attachment.getId() + "/download");
            response.put("fileSize", fileSize);
            response.put("message", "Upload success");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Merge failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Merge failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ----- Delete endpoint (unchanged) -----
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find file record in database
            Attachment attachment = attachmentRepository.findById(id).orElse(null);
            if (attachment == null) {
                response.put("success", false);
                response.put("message", "File not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Delete physical file from server
            Path actualPath = resolveStoredFilePath(attachment);
            File file = actualPath.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("Deleted physical file: {}, result: {}", actualPath, deleted);
            } else {
                log.warn("File does not exist, skipping physical deletion: {}", actualPath);
            }
            
            // Delete database record
            attachmentRepository.deleteById(id);
            log.info("Deleted database record, ID: {}", id);
            
            response.put("success", true);
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Delete failed: {}", e.getMessage(), e);
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        try {
            Attachment attachment = attachmentRepository.findById(id).orElse(null);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = resolveStoredFilePath(attachment);
            if (!Files.exists(filePath)) {
                log.warn("Preview file not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            MediaType mediaType = detectMediaType(filePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(attachment.getDisplayName(), true))
                    .body(resource);
        } catch (Exception e) {
            log.error("Preview failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            Attachment attachment = attachmentRepository.findById(id).orElse(null);
            if (attachment == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = resolveStoredFilePath(attachment);
            if (!Files.exists(filePath)) {
                log.warn("Download file not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            MediaType mediaType = detectMediaType(filePath);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(attachment.getDisplayName(), false))
                    .body(resource);
        } catch (Exception e) {
            log.error("Download failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    private void deleteDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> files = Files.walk(dir)) {
                files.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try { Files.deleteIfExists(path); }
                            catch (IOException e) { log.warn("Failed to delete: {}", path); }
                        });
            }
        }
    }

    private Path resolveStoredFilePath(Attachment attachment) {
        return Paths.get(UPLOAD_DIR, attachment.getStoredName()).normalize();
    }

    private MediaType detectMediaType(Path filePath) {
        try {
            String type = Files.probeContentType(filePath);
            if (type != null) {
                return MediaType.parseMediaType(type);
            }
        } catch (IOException ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private String buildContentDisposition(String displayName, boolean inline) {
        String safeName = displayName == null || displayName.isBlank() ? "file" : displayName;
        ContentDisposition.Builder dispositionBuilder = inline
                ? ContentDisposition.inline()
                : ContentDisposition.attachment();

        return dispositionBuilder
                .filename(safeName, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
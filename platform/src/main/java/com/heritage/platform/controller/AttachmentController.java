package com.heritage.platform.controller;

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
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    // Allowed file extensions for heritage platform (images, documents, video, audio)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
        ".pdf", ".doc", ".docx", ".txt",
        ".mp4", ".mov", ".avi", ".mkv",
        ".mp3", ".wav", ".m4a", ".flac"
    );

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
            
            // Get original file name and validate type
            String originalName = file.getOriginalFilename();
            if (!isAllowedFileType(originalName)) {
                response.put("success", false);
                response.put("message", "File type not allowed");
                return ResponseEntity.status(400).body(response);
            }

            String fileExtension = "";
            if (originalName != null && originalName.contains(".")) {
                fileExtension = originalName.substring(originalName.lastIndexOf("."));
            }
            
            // Generate unique file name for storage
            String storedName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;
            
            // Save file to disk
            checkDiskSpace(file.getSize());
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
            } else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".avi") || lowerName.endsWith(".mkv")) {
                fileType = "video";
            } else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a") || lowerName.endsWith(".flac")) {
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

    // ----- Chunked upload: receive one chunk (writes directly to final file) -----
    @PostMapping("/upload/chunk")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileName") String fileName,
            @RequestParam("chunkSize") long chunkSize) {
        Map<String, Object> response = new HashMap<>();
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate target filename (same as merge would produce)
            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String storedName = uploadId + fileExtension;
            Path targetFile = Paths.get(UPLOAD_DIR, storedName);

            // Write chunk directly at its final position in the target file
            byte[] data = file.getBytes();
            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(targetFile.toFile(), "rw")) {
                raf.seek(chunkIndex * chunkSize);
                raf.write(data);
            }

            log.info("Chunk {}/{} written to target file for uploadId={}", chunkIndex + 1, totalChunks, uploadId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Chunk upload failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Chunk upload failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ----- Chunked upload: verify and finalize (chunks are already written directly to target file) -----
    @PostMapping("/upload/merge")
    public ResponseEntity<Map<String, Object>> mergeChunks(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileName") String fileName,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("chunkSize") long chunkSize,
            @RequestParam("fileHash") String fileHash) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate file type
            if (!isAllowedFileType(fileName)) {
                response.put("success", false);
                response.put("message", "File type not allowed");
                return ResponseEntity.status(400).body(response);
            }

            // Target file was already assembled by individual chunk uploads
            String fileExtension = "";
            if (fileName != null && fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            String storedName = uploadId + fileExtension;
            Path targetFile = Paths.get(UPLOAD_DIR, storedName);

            if (!Files.exists(targetFile)) {
                response.put("success", false);
                response.put("message", "Uploaded file not found");
                return ResponseEntity.status(400).body(response);
            }

            long fileSize = Files.size(targetFile);
            long expectedSize = (totalChunks - 1L) * chunkSize + (fileSize - (totalChunks - 1L) * chunkSize);
            log.info("File assembled: {} ({} bytes, {} chunks)", storedName, fileSize, totalChunks);

            // Compute server-side SHA-256
            String serverHash = calculateSha256(targetFile);

            // Verify hash (if client provided one)
            if (!"skipped".equalsIgnoreCase(fileHash)) {
                if (!serverHash.equalsIgnoreCase(fileHash)) {
                    Files.deleteIfExists(targetFile);
                    log.error("SHA-256 mismatch for uploadId={}, expected={}, actual={}", uploadId, fileHash, serverHash);
                    response.put("success", false);
                    response.put("message", "File integrity check failed (SHA-256 mismatch)");
                    return ResponseEntity.status(400).body(response);
                }
            } else {
                log.info("Client skipped hash, server SHA-256={} for uploadId={}", serverHash, uploadId);
            }

            // Determine file type
            String fileType = "document";
            String lowerName = fileName != null ? fileName.toLowerCase() : "";
            if (lowerName.endsWith(".pdf")) fileType = "pdf";
            else if (lowerName.endsWith(".doc") || lowerName.endsWith(".docx")) fileType = "word";
            else if (lowerName.endsWith(".mp4") || lowerName.endsWith(".mov") || lowerName.endsWith(".avi") || lowerName.endsWith(".mkv")) fileType = "video";
            else if (lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a") || lowerName.endsWith(".flac")) fileType = "audio";
            else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") || lowerName.endsWith(".gif") || lowerName.endsWith(".bmp") || lowerName.endsWith(".webp")) fileType = "image";

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
            log.error("Finalize failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Finalize failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Hash failed: " + e.getMessage());
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

    private boolean isAllowedFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) return false;
        String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private void checkDiskSpace(long additionalBytes) throws IOException {
        FileStore store = Files.getFileStore(Paths.get(UPLOAD_DIR));
        long available = store.getUsableSpace();
        long minFree = 500L * 1024 * 1024;
        if (available - additionalBytes < minFree) {
            throw new IOException("Insufficient disk space on server");
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
        Path resolved = Paths.get(UPLOAD_DIR, attachment.getStoredName()).normalize();
        if (!resolved.startsWith(Paths.get(UPLOAD_DIR).normalize())) {
            throw new SecurityException("Path traversal detected");
        }
        return resolved;
    }

    private String calculateSha256(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder(hashBytes.length * 2);
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
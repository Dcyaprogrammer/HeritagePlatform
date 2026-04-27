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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/attachments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AttachmentController {
    private static final Logger log = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // Allowed file extensions for heritage platform (images, documents, video, audio)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
        ".pdf", ".doc", ".docx", ".txt",
        ".mp4", ".mov", ".avi", ".mkv",
        ".mp3", ".wav", ".m4a", ".flac"
    );

    // uploadId must be alphanumeric/underscore/hyphen only — prevents path traversal
    private static final Pattern UPLOAD_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,128}$");

    // Hard cap on a single chunk's declared size to bound seek offset and write size
    private static final long MAX_CHUNK_SIZE = 64L * 1024 * 1024; // 64 MB
    private static final int MAX_TOTAL_CHUNKS = 100_000;
    // Hard cap on a single assembled file (per uploadId). 10 GiB feels sane for a heritage archive;
    // adjust via configuration later if needed.
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10 GiB

    // Per-uploadId received-chunk bitmap. Keys live only while an upload is in progress —
    // populated on first chunk, cleared on successful merge or rejection.
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.BitSet> receivedChunks =
            new java.util.concurrent.ConcurrentHashMap<>();

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
            log.error("Upload failed: {}", e.getMessage(), e);
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
            String validation = validateChunkParams(uploadId, fileName, chunkIndex, totalChunks, chunkSize);
            if (validation != null) {
                response.put("success", false);
                response.put("message", validation);
                return ResponseEntity.status(400).body(response);
            }

            // Reject chunks larger than declared chunkSize (last chunk may be smaller)
            if (file.getSize() > chunkSize) {
                response.put("success", false);
                response.put("message", "Chunk payload exceeds declared chunkSize");
                return ResponseEntity.status(400).body(response);
            }

            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fileExtension = extractExtension(fileName);
            String storedName = uploadId + fileExtension;
            Path targetFile = resolveUnderUploadDir(storedName);

            long chunkLen = file.getSize();

            // Per-uploadId quota: don't let any single upload exceed MAX_FILE_SIZE, and reject early
            // if the declared totalChunks * chunkSize would already blow past the cap.
            long declaredMax = (long) totalChunks * chunkSize;
            if (declaredMax > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("message", "Declared upload size exceeds server limit");
                return ResponseEntity.status(400).body(response);
            }
            long currentSize = Files.exists(targetFile) ? Files.size(targetFile) : 0L;
            if (currentSize + chunkLen > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("message", "Upload exceeds server file-size limit");
                return ResponseEntity.status(413).body(response);
            }

            // Disk-space pre-check (same guard as single-file upload).
            checkDiskSpace(chunkLen);

            // Stream chunk directly to its final offset — no full-chunk byte[] allocation on heap.
            try (java.io.InputStream in = file.getInputStream();
                 java.io.RandomAccessFile raf = new java.io.RandomAccessFile(targetFile.toFile(), "rw")) {
                raf.seek((long) chunkIndex * chunkSize);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    raf.write(buffer, 0, read);
                }
            }

            // Track received chunks so mergeChunks can verify completeness even when client
            // skipped SHA-256 (raf.seek + write past EOF leaves sparse zeros that a size check alone
            // would not catch). BitSet itself isn't thread-safe — synchronize for concurrent chunks.
            java.util.BitSet bitmap = receivedChunks.computeIfAbsent(uploadId,
                    k -> new java.util.BitSet(totalChunks));
            synchronized (bitmap) {
                bitmap.set(chunkIndex);
            }

            log.info("Chunk {}/{} written to target file for uploadId={}", chunkIndex + 1, totalChunks, uploadId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Rejected chunk upload: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
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
            String validation = validateChunkParams(uploadId, fileName, 0, totalChunks, chunkSize);
            if (validation != null) {
                response.put("success", false);
                response.put("message", validation);
                return ResponseEntity.status(400).body(response);
            }
            if (!isAllowedFileType(fileName)) {
                response.put("success", false);
                response.put("message", "File type not allowed");
                return ResponseEntity.status(400).body(response);
            }

            String fileExtension = extractExtension(fileName);
            String storedName = uploadId + fileExtension;
            Path targetFile = resolveUnderUploadDir(storedName);

            if (!Files.exists(targetFile)) {
                receivedChunks.remove(uploadId);
                response.put("success", false);
                response.put("message", "Uploaded file not found");
                return ResponseEntity.status(400).body(response);
            }

            long fileSize = Files.size(targetFile);
            log.info("File assembled: {} ({} bytes, {} chunks)", storedName, fileSize, totalChunks);

            // Completeness check (covers both skip-hash and hash-verify paths). Because chunks are
            // written via raf.seek + write, a missing middle chunk leaves a sparse hole that
            // size-only checks miss. The bitmap is authoritative. Synchronize the read because
            // concurrent uploadChunk calls write to the same BitSet.
            java.util.BitSet received = receivedChunks.get(uploadId);
            int receivedCount;
            if (received == null) {
                receivedCount = 0;
            } else {
                synchronized (received) {
                    receivedCount = received.cardinality();
                }
            }
            if (receivedCount != totalChunks) {
                Files.deleteIfExists(targetFile);
                receivedChunks.remove(uploadId);
                log.warn("Incomplete upload uploadId={}: received {}/{} chunks", uploadId, receivedCount, totalChunks);
                response.put("success", false);
                response.put("message", "Incomplete upload: missing chunks");
                return ResponseEntity.status(400).body(response);
            }

            // Sanity bound on assembled size: last chunk must be in (0, chunkSize], all others == chunkSize.
            long minSize = (long) (totalChunks - 1) * chunkSize + 1;
            long maxSize = (long) totalChunks * chunkSize;
            if (fileSize < minSize || fileSize > maxSize) {
                Files.deleteIfExists(targetFile);
                receivedChunks.remove(uploadId);
                log.warn("Size out of range uploadId={}: size={}, expected [{}, {}]",
                        uploadId, fileSize, minSize, maxSize);
                response.put("success", false);
                response.put("message", "Assembled size out of expected range");
                return ResponseEntity.status(400).body(response);
            }

            // Verify hash only if client supplied one — skip the expensive SHA-256 pass when "skipped".
            // Bitmap+size already guarantee structural completeness; SHA-256 is end-to-end integrity.
            boolean skipHash = "skipped".equalsIgnoreCase(fileHash);
            if (skipHash) {
                log.info("Client skipped hash for uploadId={}, relying on bitmap+size validation", uploadId);
            } else {
                String serverHash = calculateSha256(targetFile);
                if (!serverHash.equalsIgnoreCase(fileHash)) {
                    Files.deleteIfExists(targetFile);
                    receivedChunks.remove(uploadId);
                    log.error("SHA-256 mismatch for uploadId={}, expected={}, actual={}", uploadId, fileHash, serverHash);
                    response.put("success", false);
                    response.put("message", "File integrity check failed (SHA-256 mismatch)");
                    return ResponseEntity.status(400).body(response);
                }
            }

            receivedChunks.remove(uploadId);

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
        } catch (SecurityException e) {
            receivedChunks.remove(uploadId);
            log.warn("Rejected merge: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (IOException | NoSuchAlgorithmException e) {
            receivedChunks.remove(uploadId);
            log.error("Finalize failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Finalize failed: " + e.getMessage());
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

    private Path resolveStoredFilePath(Attachment attachment) {
        return resolveUnderUploadDir(attachment.getStoredName());
    }

    /**
     * Resolves a relative name under UPLOAD_DIR and asserts the result stays inside it,
     * blocking path-traversal sequences ("..", absolute paths, symlinks) in user-supplied input.
     */
    private Path resolveUnderUploadDir(String relativeName) {
        Path base = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path resolved = base.resolve(relativeName).toAbsolutePath().normalize();
        if (!resolved.startsWith(base)) {
            throw new SecurityException("Path traversal detected");
        }
        return resolved;
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    /**
     * Returns null when params look sane; otherwise a user-facing rejection reason.
     * Validates uploadId shape (no traversal), bounds chunkIndex/totalChunks/chunkSize.
     */
    private String validateChunkParams(String uploadId, String fileName, int chunkIndex,
                                       int totalChunks, long chunkSize) {
        if (uploadId == null || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) {
            return "Invalid uploadId";
        }
        if (totalChunks <= 0 || totalChunks > MAX_TOTAL_CHUNKS) {
            return "Invalid totalChunks";
        }
        if (chunkIndex < 0 || chunkIndex >= totalChunks) {
            return "Invalid chunkIndex";
        }
        if (chunkSize <= 0 || chunkSize > MAX_CHUNK_SIZE) {
            return "Invalid chunkSize";
        }
        if (!isAllowedFileType(fileName)) {
            return "File type not allowed";
        }
        return null;
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
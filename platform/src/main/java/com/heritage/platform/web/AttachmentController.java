package com.heritage.platform.web;

import com.heritage.platform.model.Attachment;
import com.heritage.platform.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("========== Upload method called ==========");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {}", file.getSize());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 创建上传目录
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 获取原始文件名和扩展名
            String originalName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalName != null && originalName.contains(".")) {
                fileExtension = originalName.substring(originalName.lastIndexOf("."));
            }
            
            // 生成存储用的唯一文件名
            String storedName = System.currentTimeMillis() + "_" + System.nanoTime() + fileExtension;
            
            // 保存文件到磁盘
            Path filePath = Paths.get(UPLOAD_DIR + storedName);
            Files.copy(file.getInputStream(), filePath);
            log.info("File saved successfully, path: {}", filePath.toString());
            
            // 判断文件类型
            String fileType = "document";
            if (file.getContentType() != null && file.getContentType().startsWith("image")) {
                fileType = "image";
            } else if (originalName != null && originalName.endsWith(".pdf")) {
                fileType = "pdf";
            } else if (originalName != null && (originalName.endsWith(".doc") || originalName.endsWith(".docx"))) {
                fileType = "word";
            }
            
            // 保存到数据库
            Attachment attachment = new Attachment();
            attachment.setStoredName(storedName);
            attachment.setDisplayName(originalName);
            attachment.setFilePath(filePath.toString());
            attachment.setFileType(fileType);
            attachment.setFileSize(file.getSize());
            attachment.setCreatedAt(LocalDateTime.now());
            attachment.setResourceId(0L);  // 临时设为0，等模块4对接后再改
            attachmentRepository.save(attachment);
            
            log.info("Database saved successfully, ID: {}", attachment.getId());
            
            // 返回成功信息（返回 displayName 给前端显示）
            response.put("success", true);
            response.put("attachmentId", attachment.getId());
            response.put("displayName", originalName);
            response.put("storedName", storedName);
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
   @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从数据库查询文件记录
            Attachment attachment = attachmentRepository.findById(id).orElse(null);
            if (attachment == null) {
                response.put("success", false);
                response.put("message", "File not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // 删除服务器上的物理文件
            File file = new File(attachment.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                System.out.println("删除物理文件：" + attachment.getFilePath() + "，结果：" + deleted);
            } else {
                System.out.println("文件不存在，跳过物理删除：" + attachment.getFilePath());
            }
            
            // 从数据库删除记录
            attachmentRepository.deleteById(id);
            System.out.println("删除数据库记录，ID：" + id);
            
            response.put("success", true);
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("删除失败：" + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Delete failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    } 
}

<template>
  <div class="upload-container">
    <!-- Drop zone -->
    <div
      class="drop-zone"
      role="button"
      tabindex="0"
      @dragover.prevent
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
      @keydown.enter="triggerFileInput"
      @keydown.space.prevent="triggerFileInput"
    >
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        @change="handleFileSelect"
        multiple
      />
      <p>Drag & drop files here, or click to select</p>
      <p style="font-size: 12px; color: #999">
        Supports images, PDF, Word, etc.
      </p>
    </div>

    <!-- File list -->
    <div class="file-list" v-if="uploadedFiles.length > 0">
      <h4>Uploaded Files:</h4>
      <div
        v-for="file in uploadedFiles"
        :key="file.uid"
        class="file-item"
        @click="openPreview(file)"
      >
        <!-- image preview -->
        <div v-if="file.isImage" class="image-preview-container">
          <img :src="file.preview" class="file-preview" />
          <div class="image-hover">
            <img :src="file.preview" class="hover-image" />
          </div>
        </div>
        <!-- Document icon -->
        <div v-else class="file-icon">
          <i
            v-if="file.name.endsWith('.pdf')"
            class="fas fa-file-pdf"
            style="color: #e74c3c; font-size: 24px"
          ></i>
          <i
            v-else-if="
              file.name.endsWith('.doc') || file.name.endsWith('.docx')
            "
            class="fas fa-file-word"
            style="color: #2b579a; font-size: 24px"
          ></i>
          <i
            v-else-if="
              file.name.endsWith('.mp4') ||
              file.name.endsWith('.mov') ||
              file.name.endsWith('.avi')
            "
            class="fas fa-file-video"
            style="color: #9b59b6; font-size: 24px"
          ></i>
          <i
            v-else-if="
              file.name.endsWith('.mp3') || file.name.endsWith('.m4a')
            "
            class="fas fa-file-audio"
            style="color: #f39c12; font-size: 24px"
          ></i>
          <i
            v-else-if="file.name.endsWith('.jpg') || file.name.endsWith('.png')"
            class="fas fa-file-image"
            style="color: #27ae60; font-size: 24px"
          ></i>
          <i
            v-else
            class="fas fa-file"
            style="color: #7f8c8d; font-size: 24px"
          ></i>
        </div>
        <span class="file-name">{{ file.displayName || file.name }}</span>
        <span v-if="file.uploaded && !file.uploading" class="success-check">
          <i class="fas fa-check-circle"></i>
        </span>
        <span class="file-size">{{ formatFileSize(file.size) }}</span>
        <!-- Per-file progress bar -->
        <div v-if="file.uploading" class="file-progress">
          <div class="file-progress-fill" :style="{ width: file.progress + '%' }"></div>
        </div>
        <button @click.stop="removeFile(file.uid)" class="delete-btn">×</button>
      </div>
    </div>

    <!-- Error message -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
  <!-- Preview Modal -->
  <div v-if="previewFile" class="modal" @click="closePreview">
    <div class="modal-content" @click.stop>
      <span class="modal-close" @click="closePreview">&times;</span>

      <!-- Image preview -->
      <img
        v-if="previewFile.isImage"
        :src="previewFile.preview"
        class="modal-image"
      />

      <!-- Video preview -->
      <video
        v-else-if="previewFile.isVideo"
        :src="previewFile.preview"
        class="modal-video"
        controls
        autoplay
      ></video>

      <!-- PDF preview (embedded) -->
      <div
        v-else-if="previewFile.name && previewFile.name.endsWith('.pdf')"
        class="modal-pdf"
      >
        <iframe
          v-if="previewFile.previewUrl"
          :src="previewFile.previewUrl"
          class="modal-iframe"
          frameborder="0"
        ></iframe>
        <div v-else class="modal-error">
          <p>Cannot preview PDF</p>
          <a href="#" @click.prevent="downloadFile(previewFile)">Download</a>
        </div>
      </div>

      <!-- Word document preview -->
      <div
        v-else-if="
          previewFile.name &&
          (previewFile.name.endsWith('.doc') ||
            previewFile.name.endsWith('.docx'))
        "
        class="modal-doc"
      >
        <div>
          <p>
            Current environment does not support online preview of Word, please
            download and view
          </p>
          <p>{{ previewFile.displayName || previewFile.name }}</p>
          <a
            href="#"
            @click.prevent="downloadFile(previewFile)"
            class="modal-link"
            >Download to view</a
          >
        </div>
      </div>

      <!-- Other file types -->
      <div v-else class="modal-other">
        <i class="fas fa-file" style="font-size: 64px; color: #7f8c8d"></i>
        <p>{{ previewFile.displayName || previewFile.name }}</p>
        <a
          href="#"
          @click.prevent="downloadFile(previewFile)"
          class="modal-link"
          >Download file</a
        >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";

const uploadedFiles = ref([]);
const errorMessage = ref("");
const fileInput = ref(null);

let uidCounter = 0;

// Trigger hidden file input dialog
const triggerFileInput = () => {
  fileInput.value.click();
};

// Handle File Selection
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files);
  addFiles(files);
  event.target.value = "";
};

/// Handle file selection from system file picker
const handleDrop = (event) => {
  const files = Array.from(event.dataTransfer.files);
  addFiles(files);
};

// Handle dropped files from drag-and-drop
const addFiles = (files) => {
  files.forEach((file) => {
    const isImage = file.type.startsWith("image/");
    const isVideo = file.type.startsWith("video/");
    const fileItem = {
      uid: ++uidCounter,
      name: file.name,
      size: file.size,
      isImage,
      isVideo,
      preview: isImage || isVideo ? URL.createObjectURL(file) : null,
      rawFile: file,
      uploading: true,
      progress: 0,
      removed: false,
    };
    uploadedFiles.value.push(fileItem);

    // Upload to backend
    uploadFile(file, fileItem);
  });
};

// ----- Chunked upload constants -----
const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk
const CHUNK_CONCURRENCY = 3; // parallel uploads

// Upload entry: small files → single upload, large files → chunked upload
const uploadFile = async (file, fileItem) => {
  fileItem.uploading = true;
  fileItem.progress = 0;
  errorMessage.value = "";

  // Files > 50MB use chunked upload, no upper limit
  if (file.size > 50 * 1024 * 1024) {
    await uploadByChunks(file, fileItem);
  } else {
    await uploadSingle(file, fileItem);
  }

  fileItem.uploading = false;
};

// ----- Standard single upload (files ≤ 50MB) -----
const uploadSingle = async (file, fileItem) => {
  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await axios.post("/api/attachments/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 180000,
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && progressEvent.total > 0) {
          fileItem.progress = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total,
          );
        }
      },
    });
    handleUploadSuccess(response.data, fileItem, file);
  } catch (error) {
    handleUploadError(error, fileItem);
  }
};

// ----- Chunked upload (files > 50MB) -----
const uploadByChunks = async (file, fileItem) => {
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  const uploadId = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

  let completedBytes = 0;

  const uploadOneChunk = async (chunkIndex) => {
    const start = chunkIndex * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const blob = file.slice(start, end);

    const formData = new FormData();
    formData.append("file", blob, file.name);
    formData.append("uploadId", uploadId);
    formData.append("chunkIndex", chunkIndex);
    formData.append("totalChunks", totalChunks);

    await axios.post("/api/attachments/upload/chunk", formData, {
      timeout: 60000,
    });
    completedBytes += blob.size;
    fileItem.progress = Math.round((completedBytes * 100) / file.size);
  };

  const uploadWithRetry = async (chunkIndex, retries = 3) => {
    for (let attempt = 1; attempt <= retries; attempt++) {
      try {
        await uploadOneChunk(chunkIndex);
        return;
      } catch (err) {
        if (attempt === retries) throw err;
        await new Promise((r) => setTimeout(r, 2000 * attempt));
      }
    }
  };

  try {
    // Upload chunks in parallel batches
    for (let i = 0; i < totalChunks; i += CHUNK_CONCURRENCY) {
      const batch = [];
      for (let j = i; j < i + CHUNK_CONCURRENCY && j < totalChunks; j++) {
        batch.push(uploadWithRetry(j));
      }
      await Promise.all(batch);
    }

    // All chunks done — trigger merge
    const mergeResp = await axios.post("/api/attachments/upload/merge", null, {
      params: { uploadId, fileName: file.name, totalChunks },
      timeout: 120000,
    });
    handleUploadSuccess(mergeResp.data, fileItem, file);
  } catch (error) {
    handleUploadError(error, fileItem);
  }
};

// ----- Shared helpers -----
const handleUploadSuccess = (data, fileItem, file) => {
  // If the file was removed during upload, clean up on the server immediately
  if (fileItem.removed) {
    if (data.attachmentId) {
      axios.delete(`/api/attachments/${data.attachmentId}`).catch(() => {});
    }
    return;
  }

  if (data.success) {
    fileItem.id = data.attachmentId;
    fileItem.displayName = data.displayName;
    fileItem.uploaded = true;
    fileItem.serverPath = data.filePath;
    fileItem.previewUrl =
      data.previewUrl ||
      (data.attachmentId ? `/api/attachments/${data.attachmentId}/preview` : "");
    fileItem.downloadUrl =
      data.downloadUrl ||
      (data.attachmentId ? `/api/attachments/${data.attachmentId}/download` : "");
  }
};

const handleUploadError = (error, fileItem) => {
  let userMessage = "";
  if (error.code === "ERR_NETWORK") {
    userMessage = "Network error: Cannot connect to server.";
  } else if (error.code === "ECONNABORTED" || error.message.includes("timeout")) {
    userMessage = "Upload timeout.";
  } else if (error.response) {
    const s = error.response.status;
    const d = error.response.data;
    if (s === 413) userMessage = "File too large for server.";
    else if (s === 500) userMessage = "Server error.";
    else userMessage = d?.message || `Upload failed (${s})`;
  } else {
    userMessage = error.message || "Upload failed.";
  }
  errorMessage.value = userMessage;
  fileItem.uploadError = true;
  setTimeout(() => { errorMessage.value = ""; }, 5000);
};

// Delete file from list and server
const removeFile = (uid) => {
  const idx = uploadedFiles.value.findIndex((f) => f.uid === uid);
  if (idx === -1) return;
  const file = uploadedFiles.value[idx];

  // If still uploading, mark as removed so handleUploadSuccess cleans up
  if (file.uploading) {
    file.removed = true;
    uploadedFiles.value.splice(idx, 1);
    return;
  }

  // Call backend delete API if file was already uploaded
  if (file.id) {
    axios.delete(`/api/attachments/${file.id}`).catch((error) => {
      console.error("Delete failed", error);
    });
  }

  // Release preview URL
  if (file.preview) {
    URL.revokeObjectURL(file.preview);
  }

  // Remove from list
  uploadedFiles.value.splice(idx, 1);
};

// Preview modal
const previewFile = ref(null);

const openPreview = (file) => {
  previewFile.value = file;
};

const closePreview = () => {
  previewFile.value = null;
};

const downloadFile = async (file) => {
  if (!file?.id) {
    errorMessage.value = "File not uploaded yet, cannot download";
    return;
  }

  try {
    const response = await axios.get(
      file.downloadUrl || `/api/attachments/${file.id}/download`,
      {
        responseType: "blob",
      },
    );
    const blobUrl = URL.createObjectURL(response.data);
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = file.displayName || file.name || "downloaded-file";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(blobUrl);
  } catch (error) {
    console.error("Download failed:", error);
    errorMessage.value = "Download failed, please try again";
    setTimeout(() => {
      errorMessage.value = "";
    }, 5000);
  }
};

// Format file size in bytes to human-readable string
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
};
</script>

<style scoped>
.upload-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}
.drop-zone {
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}
.drop-zone:hover {
  border-color: #409eff;
  background: #f5f7fa;
}
.drop-zone:focus {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}
.file-list {
  margin-top: 20px;
}
.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
  gap: 10px;
}
.image-preview-container {
  position: relative;
  display: inline-block;
}

.file-preview {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}

.image-hover {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-bottom: 10px;
  display: none;
  z-index: 100;
  background: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.image-preview-container:hover .image-hover {
  display: block;
}

.hover-image {
  width: 200px;
  height: auto;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
}
.file-icon {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.file-name {
  flex: 1;
  font-size: 14px;
}
.file-size {
  font-size: 12px;
  color: #999;
}
.file-progress {
  width: 80px;
  height: 6px;
  background: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}
.file-progress-fill {
  height: 100%;
  background: #409eff;
  transition: width 0.3s;
  border-radius: 3px;
}
.delete-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}
.delete-btn:hover {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}
.error-message {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  border: 1px solid #fde2e2;
  font-size: 14px;
}
.success-check {
  color: #67c23a;
  font-size: 14px;
  margin-left: 6px;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  background: white;
  border-radius: 8px;
  padding: 20px;
}
.modal-close {
  position: absolute;
  top: 10px;
  right: 20px;
  font-size: 28px;
  cursor: pointer;
}
.modal-image,
.modal-video {
  max-width: 100%;
  max-height: 80vh;
}
.modal-iframe {
  width: 800px;
  height: 600px;
  max-width: 90vw;
  max-height: 70vh;
  border: none;
}

.modal-pdf,
.modal-doc,
.modal-other {
  text-align: center;
  padding: 20px;
  min-width: 300px;
}

.modal-link {
  display: inline-block;
  margin-top: 15px;
  padding: 8px 16px;
  background: #409eff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}

.modal-link:hover {
  background: #66b1ff;
}

.modal-error {
  text-align: center;
  padding: 40px;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>

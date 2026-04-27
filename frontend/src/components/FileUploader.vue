<template>
  <div class="upload-container">
    <!-- Drop zone -->
    <div
      class="drop-zone"
      role="button"
      tabindex="0"
      aria-label="Click or press Enter to choose files, or drag files here"
      @dragover.prevent
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
      @keydown.enter.prevent="triggerFileInput"
      @keydown.space.prevent="triggerFileInput"
    >
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        accept=".jpg,.jpeg,.png,.gif,.bmp,.webp,.pdf,.doc,.docx,.txt,.mp4,.mov,.avi,.mkv,.mp3,.wav,.m4a,.flac"
        @change="handleFileSelect"
        multiple
      />
      <p>Drag & drop files here, or click to select</p>
      <p style="font-size: 12px; color: #999">
        Supports images, documents (PDF/Word), video, and audio files
      </p>
    </div>

    <!-- File list -->
    <div class="file-list" v-if="uploadedFiles.length > 0">
      <h4>Uploaded Files:</h4>
      <div
        v-for="file in uploadedFiles"
        :key="file.id"
        class="file-item-wrapper"
      >
        <div class="file-item" @click="openPreview(file)">
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
              v-if="file.name.toLowerCase().endsWith('.pdf')"
              class="fas fa-file-pdf"
              style="color: #e74c3c; font-size: 24px"
            ></i>
            <i
              v-else-if="file.name.toLowerCase().endsWith('.doc') || file.name.toLowerCase().endsWith('.docx')"
              class="fas fa-file-word"
              style="color: #2b579a; font-size: 24px"
            ></i>
            <i
              v-else-if="file.name.toLowerCase().endsWith('.mp4') || file.name.toLowerCase().endsWith('.mov') || file.name.toLowerCase().endsWith('.avi') || file.name.toLowerCase().endsWith('.mkv')"
              class="fas fa-file-video"
              style="color: #9b59b6; font-size: 24px"
            ></i>
            <i
              v-else-if="file.name.toLowerCase().endsWith('.mp3') || file.name.toLowerCase().endsWith('.wav') || file.name.toLowerCase().endsWith('.m4a') || file.name.toLowerCase().endsWith('.flac')"
              class="fas fa-file-audio"
              style="color: #f39c12; font-size: 24px"
            ></i>
            <i
              v-else-if="file.name.toLowerCase().endsWith('.jpg') || file.name.toLowerCase().endsWith('.jpeg') || file.name.toLowerCase().endsWith('.png') || file.name.toLowerCase().endsWith('.gif') || file.name.toLowerCase().endsWith('.bmp') || file.name.toLowerCase().endsWith('.webp')"
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
          <span v-if="file.uploaded" class="success-check">
            <i class="fas fa-check-circle"></i>
          </span>
          <span class="file-size">{{ formatFileSize(file.size) }}</span>
          <button @click.stop="removeFile(file)" class="delete-btn">×</button>
        </div>
        <!-- Per-file upload progress -->
        <div v-if="file.uploading" class="progress-bar">
          <div class="progress-fill" :style="{ width: file.uploadProgress + '%' }"></div>
          <span>{{ file.mergingPhase ? 'Verifying...' : file.uploadProgress + '%' }}</span>
        </div>
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
      <!-- Sits in the top padding band, above the video — outside any
           <video>/<iframe> shadow DOM that would otherwise eat the click. -->
      <span class="modal-close" @click.stop="closePreview">&times;</span>

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
        v-else-if="previewFile.name && previewFile.name.toLowerCase().endsWith('.pdf')"
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
          (previewFile.name.toLowerCase().endsWith('.doc') ||
            previewFile.name.toLowerCase().endsWith('.docx'))
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
import { ref, reactive } from "vue";
import axios from "axios";

const uploadedFiles = ref([]);
const errorMessage = ref("");
const fileInput = ref(null);

// Stable per-item id for v-for keys (independent of array position)
let nextFileId = 0;

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
    // Validate file type before adding to list
    if (!isAllowedFileExt(file.name)) {
      errorMessage.value = `"${file.name}" is not a supported file type`;
      setTimeout(() => { errorMessage.value = ""; }, 5000);
      return;
    }

    // Friendly reminder for very large files (still allowed)
    if (file.size > 500 * 1024 * 1024) {
      const ok = confirm(
        `${file.name} is ${formatFileSize(file.size)}. Large files may take a while to upload. Continue?`
      );
      if (!ok) return;
    }

    // Wrap with reactive() so the SAME reference we hand to uploadFile is the proxy
    // observed by the template. Plain objects pushed into a ref([]) get auto-proxied,
    // but the outer reference would still point to the raw object — writes to it
    // wouldn't trigger re-renders. reactive() keeps both views in sync.
    const fileItem = reactive({
      id: ++nextFileId,
      name: file.name,
      size: file.size,
      isImage: file.type.startsWith("image/"),
      isVideo: file.type.startsWith("video/"),
      preview: URL.createObjectURL(file),
      rawFile: file,
      // Per-file upload state (so concurrent uploads don't clobber each other)
      uploading: false,
      uploadProgress: 0,
      mergingPhase: false,
      uploaded: false,
      uploadError: false,
      // Server-assigned id and abort plumbing for in-flight uploads
      attachmentId: null,
      uploadController: null,
      uploadPromise: null,
      _removed: false,
    });
    uploadedFiles.value.push(fileItem);

    // Upload to backend; track promise so removeFile can await settlement.
    fileItem.uploadPromise = uploadFile(file, fileItem).catch(() => {});
  });
};

// ----- Chunked upload constants -----
const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk
const CHUNK_CONCURRENCY = 3; // parallel uploads

// Allowed file extensions matching backend whitelist
const ALLOWED_EXTS = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
  ".pdf", ".doc", ".docx", ".txt", ".mp4", ".mov", ".avi", ".mkv",
  ".mp3", ".wav", ".m4a", ".flac"];

const isAllowedFileExt = (name) => {
  const dot = name.lastIndexOf(".");
  if (dot === -1) return false;
  return ALLOWED_EXTS.includes(name.substring(dot).toLowerCase());
};

const toHex = (bytes) =>
  Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");

const calculateFileHash = async (file) => {
  const buffer = await file.arrayBuffer();
  const digest = await crypto.subtle.digest("SHA-256", buffer);
  return toHex(new Uint8Array(digest));
};

// Upload entry: small files → single upload, large files → chunked upload
const uploadFile = async (file, fileItem) => {
  fileItem.uploading = true;
  fileItem.uploadProgress = 0;
  fileItem.mergingPhase = false;
  fileItem.uploadController = new AbortController();
  errorMessage.value = "";

  try {
    // Files > 50MB use chunked upload, no upper limit
    if (file.size > 50 * 1024 * 1024) {
      await uploadByChunks(file, fileItem);
    } else {
      await uploadSingle(file, fileItem);
    }
  } finally {
    fileItem.uploading = false;
    fileItem.mergingPhase = false;
    setTimeout(() => { fileItem.uploadProgress = 0; }, 1000);
  }
};

// ----- Standard single upload (files ≤ 50MB) -----
const uploadSingle = async (file, fileItem) => {
  const formData = new FormData();
  formData.append("file", file);

  try {
    const response = await axios.post("/api/attachments/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
      timeout: 180000,
      signal: fileItem.uploadController?.signal,
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && progressEvent.total > 0) {
          fileItem.uploadProgress = Math.round(
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
  // 122 bits of entropy from crypto.randomUUID() — collision-resistant across concurrent uploads
  // and removes the guessable-id surface on the unauthenticated /upload/merge endpoint.
  // Result still matches the backend regex ^[A-Za-z0-9_-]{8,128}$.
  const uploadId = `${Date.now()}_${crypto.randomUUID().replace(/-/g, "")}`;

  // Skip client-side hash for files > 100MB to avoid loading entire file into memory.
  // Server-side SHA-256 check still runs during merge.
  const fileHash = file.size > 100 * 1024 * 1024
    ? "skipped"
    : await calculateFileHash(file);

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
    formData.append("fileName", file.name);
    formData.append("chunkSize", CHUNK_SIZE);

    await axios.post("/api/attachments/upload/chunk", formData, {
      timeout: 60000,
      signal: fileItem.uploadController?.signal,
    });
    completedBytes += blob.size;
    fileItem.uploadProgress = Math.round((completedBytes * 100) / file.size);
  };

  // Abort/cancel must short-circuit retries so removeFile (which awaits uploadPromise)
  // resolves promptly instead of stalling through the 2s/4s/6s backoff.
  const isCancelError = (err) =>
    axios.isCancel?.(err) ||
    err?.code === "ERR_CANCELED" ||
    err?.name === "CanceledError" ||
    err?.name === "AbortError";

  const uploadWithRetry = async (chunkIndex, retries = 3) => {
    for (let attempt = 1; attempt <= retries; attempt++) {
      if (fileItem.uploadController?.signal.aborted) {
        const e = new Error("Upload aborted");
        e.name = "AbortError";
        throw e;
      }
      try {
        await uploadOneChunk(chunkIndex);
        return;
      } catch (err) {
        if (isCancelError(err) || attempt === retries) throw err;
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
    fileItem.mergingPhase = true;
    fileItem.uploadProgress = 100;
    const verifyTimeout = Math.max(60000, Math.ceil(file.size / (1024 * 1024)) * 30);
    const mergeResp = await axios.post("/api/attachments/upload/merge", null, {
      params: { uploadId, fileName: file.name, totalChunks, chunkSize: CHUNK_SIZE, fileHash },
      timeout: verifyTimeout,
      signal: fileItem.uploadController?.signal,
    });
    fileItem.mergingPhase = false;
    handleUploadSuccess(mergeResp.data, fileItem, file);
  } catch (error) {
    fileItem.mergingPhase = false;
    handleUploadError(error, fileItem);
  }
};

// ----- Shared helpers -----
const handleUploadSuccess = (data, fileItem, file) => {
  if (!data.success) return;
  // attachmentId is the server-side row id; always set so removeFile can clean up,
  // even if the user removed the row mid-flight.
  fileItem.attachmentId = data.attachmentId;
  fileItem.displayName = data.displayName;
  fileItem.serverPath = data.filePath;
  fileItem.previewUrl =
    data.previewUrl ||
    (data.attachmentId ? `/api/attachments/${data.attachmentId}/preview` : "");
  fileItem.downloadUrl =
    data.downloadUrl ||
    (data.attachmentId ? `/api/attachments/${data.attachmentId}/download` : "");
  // Don't mark uploaded if the row was already removed — removeFile will handle server cleanup.
  if (!fileItem._removed) {
    fileItem.uploaded = true;
  }
};

const handleUploadError = (error, fileItem) => {
  // Aborts triggered by removeFile aren't real errors — ignore silently.
  if (axios.isCancel?.(error) || error?.code === "ERR_CANCELED" || error?.name === "CanceledError") {
    return;
  }
  let userMessage = "";
  if (error.code === "ERR_NETWORK") {
    userMessage = "Network error: Cannot connect to server.";
  } else if (error.code === "ECONNABORTED" || (error.message && error.message.includes("timeout"))) {
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
const removeFile = async (file) => {
  // Mark first so a late-arriving handleUploadSuccess won't flip uploaded=true on a removed row.
  file._removed = true;

  // Abort any in-flight upload for this row.
  if (file.uploadController) {
    try { file.uploadController.abort(); } catch (_) { /* noop */ }
  }

  // Wait for the upload promise to settle — even after abort, axios may have already received
  // a successful response and called handleUploadSuccess (which sets attachmentId).
  if (file.uploadPromise) {
    try { await file.uploadPromise; } catch (_) { /* swallowed by uploadFile already */ }
  }

  // If the upload completed before/despite the abort, clean up the server-side record.
  if (file.attachmentId) {
    try {
      await axios.delete(`/api/attachments/${file.attachmentId}`);
    } catch (error) {
      console.error("Delete failed", error);
      // If the row is still in the list, surface the error and keep the row so user can retry.
      if (uploadedFiles.value.includes(file)) {
        file._removed = false;
        alert("Delete failed：" + (error.response?.data?.message || error.message));
        return;
      }
    }
  }

  // Release preview URL
  if (file.preview) {
    URL.revokeObjectURL(file.preview);
  }

  // Remove by identity (not index) so concurrent removals stay correct.
  const idx = uploadedFiles.value.indexOf(file);
  if (idx >= 0) uploadedFiles.value.splice(idx, 1);
};

// Preview modal
const previewFile = ref(null);

const openPreview = (file) => {
  previewFile.value = file;
};

const closePreview = () => {
  previewFile.value = null;
};

const downloadFile = (file) => {
  if (!file?.attachmentId) {
    errorMessage.value = "File not uploaded yet, cannot download";
    return;
  }

  // Use a plain anchor click so the browser streams the response straight to disk.
  // Buffering multi-GB downloads as a Blob via axios crashes the tab; the backend already
  // sets Content-Disposition: attachment, and the endpoint is unauthenticated so no headers
  // need to be attached. (If auth headers become required later, switch to fetch + streamsaver.)
  const link = document.createElement("a");
  link.href = file.downloadUrl || `/api/attachments/${file.attachmentId}/download`;
  link.download = file.displayName || file.name || "";
  link.rel = "noopener";
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
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
.drop-zone:focus-visible {
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
.progress-bar {
  margin-top: 10px;
  height: 20px;
  background: #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}
.progress-fill {
  height: 100%;
  background: #409eff;
  transition: width 0.3s;
}
.progress-bar span {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  line-height: 20px;
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
  padding: 36px 20px 20px;
}
.modal-close {
  position: absolute;
  top: 6px;
  right: 14px;
  font-size: 26px;
  line-height: 1;
  color: #888;
  cursor: pointer;
  user-select: none;
  z-index: 10;
}
.modal-close:hover {
  color: #333;
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

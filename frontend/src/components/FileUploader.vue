<template>
  <div class="upload-container">
    <!-- 拖拽区域 -->
    <div 
      class="drop-zone"
      @dragover.prevent
      @drop.prevent="handleDrop"
      @click="triggerFileInput"
    >
      <input 
        type="file" 
        ref="fileInput" 
        style="display: none" 
        @change="handleFileSelect"
        multiple
      />
      <p>Drag & drop files here, or click to select</p>
      <p style="font-size: 12px; color: #999;">Supports images, PDF, Word, etc.</p>
    </div>
    
    <!-- 文件列表 -->
    <div class="file-list" v-if="uploadedFiles.length > 0">
      <h4>Uploaded Files:</h4>
      <div v-for="(file, index) in uploadedFiles" :key="index" class="file-item">
        <!-- 图片预览 -->
        <div v-if="file.isImage" class="image-preview-container">
          <img :src="file.preview" class="file-preview" />
          <div class="image-hover">
            <img :src="file.preview" class="hover-image" />
          </div>
        </div>
        <!-- 文档图标 -->
        <div v-else class="file-icon">
          <i v-if="file.name.endsWith('.pdf')" class="fas fa-file-pdf" style="color: #e74c3c; font-size: 24px;"></i>
          <i v-else-if="file.name.endsWith('.doc') || file.name.endsWith('.docx')" class="fas fa-file-word" style="color: #2b579a; font-size: 24px;"></i>
          <i v-else-if="file.name.endsWith('.jpg') || file.name.endsWith('.png')" class="fas fa-file-image" style="color: #27ae60; font-size: 24px;"></i>
          <i v-else class="fas fa-file" style="color: #7f8c8d; font-size: 24px;"></i>
        </div>
        <span class="file-name">{{ file.displayName || file.name }}</span>
        <span v-if="file.uploaded" class="success-check">
          <i class="fas fa-check-circle"></i>
        </span>
        <span class="file-size">{{ formatFileSize(file.size) }}</span>
        <button @click="removeFile(index)" class="delete-btn">×</button>
      </div>
    </div>
    
    <!-- 上传进度 -->
    <div v-if="uploading" class="progress-bar">
      <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
      <span>{{ uploadProgress }}%</span>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const uploadedFiles = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const errorMessage = ref('')
const fileInput = ref(null)

// 触发文件选择框
const triggerFileInput = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  addFiles(files)
}

// 处理拖拽
const handleDrop = (event) => {
  const files = Array.from(event.dataTransfer.files)
  addFiles(files)
}

// 添加文件到列表并上传
const addFiles = (files) => {
  files.forEach(file => {
    // 创建预览
    const fileItem = {
      name: file.name,
      size: file.size,
      isImage: file.type.startsWith('image/'),
      preview: file.type.startsWith('image/') ? URL.createObjectURL(file) : null,
      rawFile: file
    }
    uploadedFiles.value.push(fileItem)
    
    // 上传到后端
    uploadFile(file, fileItem)
  })
}

// 上传文件到后端
const uploadFile = async (file, fileItem) => {
  const formData = new FormData()
  formData.append('file', file)
  
  uploading.value = true
  uploadProgress.value = 0
  errorMessage.value = ''
  
  // 前端文件大小检查（10MB限制）
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    errorMessage.value = `File too large: ${formatFileSize(file.size)}. Maximum size is 10MB`
    fileItem.uploadError = true
    uploading.value = false
    setTimeout(() => { errorMessage.value = '' }, 5000)
    return
  }
  
  try {
    const response = await axios.post('/api/attachments/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000, // 30秒超时
      onUploadProgress: (progressEvent) => {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        uploadProgress.value = percent
      }
    })
    
    if (response.data.success) {
      fileItem.id = response.data.attachmentId
      fileItem.displayName = response.data.displayName
      fileItem.uploaded = true
      fileItem.serverPath = response.data.filePath
      console.log('Upload success', response.data)
    }
  } catch (error) {
    console.error('Upload failed:', error)
    
    // 根据错误类型设置不同的用户提示
    let userMessage = ''
    
    if (error.code === 'ERR_NETWORK') {
      userMessage = 'Network error: Cannot connect to server. Please check if backend is running on port 8080'
      console.error('Backend may not be running or network is down')
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      userMessage = 'Upload timeout: Connection took too long. Please check your network and try again'
      console.error('Upload timeout after 30 seconds')
    } else if (error.response) {
      // 服务器返回了错误响应
      const status = error.response.status
      const data = error.response.data
      
      if (status === 413) {
        userMessage = 'File too large for server. Maximum size is 10MB'
        console.error('File size exceeded server limit')
      } else if (status === 500) {
        userMessage = 'Server error occurred. Please try again later'
        console.error('Server error:', data)
      } else if (data?.message) {
        userMessage = `Upload failed: ${data.message}`
      } else {
        userMessage = `Upload failed with status ${status}`
      }
    } else if (error.message) {
      userMessage = `Upload failed: ${error.message}`
    } else {
      userMessage = 'Upload failed for unknown reason. Please try again'
    }
    
    errorMessage.value = userMessage
    fileItem.uploadError = true
    
    // 5秒后自动清除错误提示
    setTimeout(() => {
      errorMessage.value = ''
    }, 5000)
  } finally {
    uploading.value = false
    setTimeout(() => { uploadProgress.value = 0 }, 1000)
  }
}

// 删除文件
const removeFile = async (index) => {
  const file = uploadedFiles.value[index]
  
  // 如果文件已经上传到服务器，调用后端删除接口
  if (file.id) {
    try {
      await axios.delete(`/api/attachments/${file.id}`)
      console.log('文件已从服务器删除')
    } catch (error) {
      console.error('删除失败', error)
      alert('删除失败：' + (error.response?.data?.message || error.message))
      return  // 删除失败就不从列表中移除
    }
  }
  
  // 释放预览URL
  if (file.preview) {
    URL.revokeObjectURL(file.preview)
  }
  
  // 从列表中移除
  uploadedFiles.value.splice(index, 1)
  console.log('已从列表中移除')
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}
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
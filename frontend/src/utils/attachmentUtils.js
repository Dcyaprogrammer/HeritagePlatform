/**
 * Attachment type utilities
 * Used across ResourceCard, ResourceImageCarousel, and ResourceDetailView
 */

export function getAttachmentType(attachment) {
  const fileType = attachment?.file_type || attachment?.fileType || ''
  if (fileType === 'image' || fileType.startsWith('image/')) return 'image'
  if (fileType === 'video' || fileType.startsWith('video/')) return 'video'
  if (fileType === 'audio' || fileType.startsWith('audio/')) return 'audio'
  if (fileType === 'pdf' || fileType.startsWith('application/pdf')) return 'pdf'
  if (fileType === 'word' || fileType.includes('document') || fileType.includes('word')) return 'word'
  return 'other'
}

export function getAttachmentSrc(attachment) {
  return attachment?.file_path || attachment?.filePath || ''
}

export function filterByType(attachments, type) {
  return (attachments || []).filter(a => getAttachmentType(a) === type)
}

export const TYPE_META = {
  video:   { label: 'Video',   icon: '▶',  color: '#dc2626', bg: '#fef2f2' },
  audio:   { label: 'Audio',   icon: '♪',  color: '#7c3aed', bg: '#f5f3ff' },
  pdf:     { label: 'PDF',     icon: '📄', color: '#dc2626', bg: '#fef2f2' },
  word:    { label: 'Doc',     icon: '📝', color: '#2563eb', bg: '#eff6ff' },
  image:   { label: 'Image',   icon: '🖼', color: '#16a34a', bg: '#f0fdf4' },
  other:   { label: 'File',    icon: '📎', color: '#6b7280', bg: '#f9fafb' },
}

export function getTypeMeta(type) {
  return TYPE_META[type] || TYPE_META.other
}

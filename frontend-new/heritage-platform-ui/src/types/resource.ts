export type ResourceStatus =
  | 'DRAFT'
  | 'PENDING_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'ARCHIVED'

export interface Tag {
  id: number
  name: string
}

export interface Category {
  id: number
  name: string
  description?: string
}

export interface Attachment {
  id: number
  resource_id: number
  file_path: string
  file_type: string
  created_at: string
}

export interface ResourceListItem {
  id: number
  title: string
  description: string
  location_name: string | null
  copyright_declaration: string | null
  status: ResourceStatus
  category: Category | null
  tags: Tag[]
  coverUrl: string | null
}

export interface ResourceDetail extends ResourceListItem {
  contributorName?: string
  created_at: string
  updated_at: string
  attachments: Attachment[]
  externalLinks?: string[]
}

export interface Comment {
  id: number
  resource_id: number
  user_id: number
  authorName: string
  content: string
  created_at: string
  updated_at: string
}
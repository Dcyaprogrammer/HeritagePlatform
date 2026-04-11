/**
 * 演示数据：配图与正文都在这里改。
 * - 封面：每条资源的 coverUrl
 * - 详情轮播：同一条下 attachments 里 file_path（建议 image/jpeg 等图片类型）
 * - 也可用本地图：把图片放进 public/ 例如 public/heritage/alley.jpg，再写 '/heritage/alley.jpg'
 */
import type { Comment, ResourceDetail } from '@/types/resource'
import pjlA from '@/assets/heritage/pjl.a.jpg'
import pjlB from '@/assets/heritage/pjl.b.jpg'
import lyhbA from '@/assets/heritage/lyhb.a.jpg'
import lyhbB from '@/assets/heritage/lyhb.b.jpg'
import ygA from '@/assets/heritage/yg.a.jpg'
import ygB from '@/assets/heritage/yg.b.jpg'

export const mockResources: ResourceDetail[] = [
  {
    id: 1,
    title: '平江路历史街区口述史节选',
    description:
      '关于春申君对苏州的治理，汉代的《越绝书》以及苏州各时期地方志中多有记载。“平江历史街区是苏州古城保存最完整的区域，这里特殊又精妙的水陆双棋盘格局，能追溯到春申君时期的河道体系基础。”',
    location_name: '苏州市 · 平江路',
    copyright_declaration: 'CC BY-NC-SA 4.0 · 社区工作坊授权',
    status: 'APPROVED',
    category: { id: 1, name: '口述传统', description: '' },
    tags: [
      { id: 1, name: '口述史' },
      { id: 2, name: '街巷' },
    ],
    coverUrl: pjlA,
    contributorName: '社区档案组',
    created_at: '2025-11-02T10:00:00Z',
    updated_at: '2025-11-15T14:30:00Z',
    attachments: [
      {
        id: 101,
        resource_id: 1,
        file_path: pjlA,
        file_type: 'image/jpeg',
        created_at: '2025-11-02T10:00:00Z',
      },
      {
        id: 102,
        resource_id: 1,
        file_path: pjlB,
        file_type: 'image/jpeg',
        created_at: '2025-11-02T10:01:00Z',
      },
    ],
  },
  {
    id: 2,
    title: '传统蓝印花布',
    description:
      '桐乡蓝印花布是以植物靛蓝为染料的传统防染技艺制品,起源于秦汉时期,元代传入桐乡地区。其核心工艺采用镂刻花版与物理防染技术，形成蓝白相间的花纹体系，图案多取材民间吉祥纹样。该技艺于2005年列入浙江省首批非物质文化遗产名录，2014年升格为国家级非遗项目，崇福镇与石门镇为主要传承地，丰同裕染坊与桐乡蓝印花布厂为代表性生产保护单位',
    location_name: '嘉兴桐乡 · 蓝染工坊',
    copyright_declaration: '仅限教育用途 · 工坊署名须保留',
    status: 'APPROVED',
    category: { id: 2, name: '工艺与器物', description: '' },
    tags: [
      { id: 4, name: '蓝染' },
      { id: 5, name: '纹样' },
      { id: 6, name: '教育材料' },
    ],
    coverUrl: lyhbA,
    contributorName: '浙江省传统手工研究院',
    created_at: '2025-10-18T09:00:00Z',
    updated_at: '2025-10-20T11:00:00Z',
    attachments: [
      {
        id: 201,
        resource_id: 2,
        file_path: lyhbA,
        file_type: 'image/jpeg',
        created_at: '2025-10-18T09:00:00Z',
      },
      {
        id: 202,
        resource_id: 2,
        file_path: lyhbB,
        file_type: 'image/jpeg',
        created_at: '2025-10-18T09:05:00Z',
      },
    ],
  },
  {
    id: 3,
    title: '渔村潮汛谚语集',
    description:
      '收集沿海社区流传的潮汛与气象谚语五十余条，并附老一辈渔民的简短释义，用于地方志补充与防灾教育参考。',
    location_name: '海宁市 · 盐官镇',
    copyright_declaration: '社区共享 · 转载需注明出处',
    status: 'APPROVED',
    category: { id: 3, name: '语言与知识', description: '' },
    tags: [
      { id: 7, name: '谚语' },
      { id: 8, name: '海洋' },
    ],
    coverUrl: ygA,
    contributorName: '盐官镇文化站',
    created_at: '2025-09-05T08:00:00Z',
    updated_at: '2025-09-10T16:20:00Z',
    attachments: [
      {
        id: 301,
        resource_id: 3,
        file_path: ygA,
        file_type: 'image/jpeg',
        created_at: '2025-09-05T08:00:00Z',
      },
      {
        id: 302,
        resource_id: 3,
        file_path: ygB,
        file_type: 'image/jpeg',
        created_at: '2025-09-05T08:01:00Z',
      },
    ],
  },
]

const mockCommentsByResource: Record<number, Comment[]> = {
  1: [
    {
      id: 1,
      resource_id: 1,
      user_id: 10,
      authorName: '李老师',
      content: '非常适合课堂导读，请问是否有完整音频？',
      created_at: '2025-11-20T12:00:00Z',
      updated_at: '2025-11-20T12:00:00Z',
    },
    {
      id: 2,
      resource_id: 1,
      user_id: 11,
      authorName: '访客_青禾',
      content: '石板巷的照片很有年代感，感谢整理。',
      created_at: '2025-11-22T09:30:00Z',
      updated_at: '2025-11-22T09:30:00Z',
    },
  ],
  2: [],
  3: [
    {
      id: 3,
      resource_id: 3,
      user_id: 12,
      authorName: '海事志愿者',
      content: '谚语和防灾结合讲解会很有用。',
      created_at: '2025-10-01T15:00:00Z',
      updated_at: '2025-10-01T15:00:00Z',
    },
  ],
}

export function getApprovedResources(): ResourceDetail[] {
  return mockResources.filter((r) => r.status === 'APPROVED')
}

export function getResourceById(id: number): ResourceDetail | undefined {
  const r = mockResources.find((x) => x.id === id)
  if (!r || r.status !== 'APPROVED') return undefined
  return r
}

export function getComments(resourceId: number): Comment[] {
  return mockCommentsByResource[resourceId] ?? []
}
/**
 * Demo data — edit copy and images here.
 * - Cover: each resource’s coverUrl
 * - Detail carousel: attachments[].file_path (use image/jpeg or other image/* MIME types)
 * - Or use public/: e.g. public/heritage/alley.jpg → '/heritage/alley.jpg'
 */
import pjlA from '../assets/heritage/pjl.a.jpg'
import pjlB from '../assets/heritage/pjl.b.jpg'
import lyhbA from '../assets/heritage/lyhb.a.jpg'
import lyhbB from '../assets/heritage/lyhb.b.jpg'
import ygA from '../assets/heritage/yg.a.jpg'
import ygB from '../assets/heritage/yg.b.jpg'

export const mockResources = [
  {
    id: 1,
    title: 'Oral history excerpt: Pingjiang Road historic district',
    description:
      'Local chronicles from the Han dynasty’s Yuejue shu to later Suzhou gazetteers record Lord Chunshen’s work on the region’s waterways. Pingjiang Road preserves one of the most intact historic quarters of the old city; its distinctive land–water chessboard layout reflects canal systems that reach back to that early period.',
    location_name: 'Suzhou, China · Pingjiang Road',
    copyright_declaration: 'CC BY-NC-SA 4.0 · Community workshop licence',
    status: 'APPROVED',
    category: { id: 1, name: 'Oral tradition', description: '' },
    tags: [
      { id: 1, name: 'oral history' },
      { id: 2, name: 'historic streets' },
    ],
    coverUrl: pjlA,
    contributorName: 'Neighbourhood archives group',
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
    title: 'Traditional indigo resist-dyed cloth (lan yin hua bu)',
    description:
      'Tongxiang blue calico uses plant indigo and resist dyeing with carved paper stencils, producing bold blue-and-white folk patterns. The craft entered the Tongxiang area by the Yuan period; it was listed among Zhejiang’s first provincial ICH items in 2005 and recognised nationally in 2014. Chongfu and Shimen are key centres; historic workshops such as Fengtongyu remain reference points for safeguarding and production.',
    location_name: 'Tongxiang, Jiaxing · Indigo workshop',
    copyright_declaration: 'Educational use only · Workshop attribution required',
    status: 'APPROVED',
    category: { id: 2, name: 'Craft & objects', description: '' },
    tags: [
      { id: 4, name: 'indigo dyeing' },
      { id: 5, name: 'patterns' },
      { id: 6, name: 'learning materials' },
    ],
    coverUrl: lyhbA,
    contributorName: 'Zhejiang Institute of Traditional Crafts',
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
    title: 'Coastal tide and weather proverbs',
    description:
      'A community-compiled set of more than fifty proverbs about tides and weather along the coast, with short explanations from elder fishers. Intended as supplementary material for local gazetteers and hazard-awareness education.',
    location_name: 'Haining · Yanguan Town',
    copyright_declaration: 'Community share-alike · Credit the source when reusing',
    status: 'APPROVED',
    category: { id: 3, name: 'Language & knowledge', description: '' },
    tags: [
      { id: 7, name: 'proverbs' },
      { id: 8, name: 'maritime' },
    ],
    coverUrl: ygA,
    contributorName: 'Yanguan Town cultural station',
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

const mockCommentsByResource = {
  1: [
    {
      id: 1,
      resource_id: 1,
      user_id: 11,
      authorName: 'guest_qinghe',
      content: 'The stone-lane photos feel very atmospheric. Thanks for compiling this.',
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
      authorName: 'Maritime volunteer',
      content: 'Pairing the proverbs with disaster-prep talks would work really well.',
      created_at: '2025-10-01T15:00:00Z',
      updated_at: '2025-10-01T15:00:00Z',
    },
  ],
}

let mockCommentSeq = Math.max(
  0,
  ...Object.values(mockCommentsByResource)
    .flat()
    .map((c) => c.id),
)

export function getApprovedResources() {
  return mockResources.filter((r) => r.status === 'APPROVED')
}

export function getResourceById(id) {
  const r = mockResources.find((x) => x.id === id)
  if (!r || r.status !== 'APPROVED') return undefined
  return r
}

export function getComments(resourceId) {
  return mockCommentsByResource[resourceId] ?? []
}

export function postComment(resourceId, userId, content, authorName = 'Demo user') {
  const now = new Date().toISOString()
  const next = {
    id: ++mockCommentSeq,
    resource_id: resourceId,
    user_id: userId,
    authorName,
    content: content.trim(),
    created_at: now,
    updated_at: now,
  }
  mockCommentsByResource[resourceId] = [next, ...(mockCommentsByResource[resourceId] ?? [])]
  return next
}

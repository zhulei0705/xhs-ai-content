export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  records: T[]
}

export interface Topic {
  id: string
  topicTitle: string
  coreAngle: string
  targetAudience: string
  contentType: string
  keywords: string[]
  targetMatchScore: number
  viralPotentialScore: number
  overallScore: number
  potentialAnalysis: string
  publishPriority: number
  sourceType: string
  generationDate: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface ContentListItem {
  id: string
  topicId?: string
  selectedTitle: string
  coverTitle: string
  tags: string[]
  status: ContentStatus
  createdAt: string
  updatedAt: string
}

export type ContentStatus = 'DRAFT' | 'PENDING_REVIEW' | 'READY_TO_PUBLISH' | 'PUBLISHED' | 'ARCHIVED'

export interface ContentTitle {
  id?: string
  text: string
  sortOrder: number
  selected?: boolean
  attractionScore?: number
}

export interface ContentCard {
  id?: string
  cardNo: number
  title: string
  body: string
}

export interface AiReview {
  id: string
  contentId: string
  aiToneScore: number
  titleAttractionScore: number
  authenticityScore: number
  platformFitScore: number
  exaggeratedIncomeRisk: boolean
  sensitiveExpressionRisk: boolean
  marketingRisk: boolean
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH'
  summary: string
  suggestions: string[]
  provider: string
  modelName: string
  reviewedAt: string
}

export interface ContentDetail {
  id: string
  topicId?: string
  selectedTitle: string
  body: string
  coverTitle: string
  coverSubtitle: string
  tags: string[]
  interactionGuide: string
  status: ContentStatus
  titles: ContentTitle[]
  cards: ContentCard[]
  latestReview?: AiReview
  createdAt: string
  updatedAt: string
}

export interface PublishRecord {
  id: string
  contentId: string
  contentTitle: string
  publishedAt: string
  noteUrl: string
  exposureCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  followerGrowth: number
  favoriteRate: number
  interactionRate: number
  updatedAt: string
}

export interface PerformanceAnalysis {
  id: string
  publishRecordId: string
  overallGrade: string
  performanceSummary: string
  topicAnalysis: string
  titleAnalysis: string
  favoriteRateAnalysis: string
  interactionRateAnalysis: string
  nextTopicSuggestions: string[]
  provider: string
  modelName: string
  analyzedAt: string
}

export interface Dashboard {
  publishedCount: number
  totalExposure: number
  totalLikes: number
  totalFavorites: number
  totalComments: number
  totalFollowerGrowth: number
  averageFavoriteRate: number
  averageInteractionRate: number
  topContents: Array<{ contentId: string; title: string; exposureCount: number; interactionRate: number }>
}

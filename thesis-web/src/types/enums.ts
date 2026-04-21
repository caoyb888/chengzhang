export enum UserType {
  SCHOOL_ADMIN = 'SCHOOL_ADMIN',
  POINT_ADMIN = 'POINT_ADMIN',
  TEACHER = 'TEACHER',
  ASSIST_TEACHER = 'ASSIST_TEACHER',
  STUDENT = 'STUDENT',
}

export enum PaperStatus {
  PENDING = 'PENDING',
  SUBMITTED = 'SUBMITTED',
  REVIEWING = 'REVIEWING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export enum FlowNodeType {
  SIGN = 'SIGN',
  TOPIC = 'TOPIC',
  OUTLINE = 'OUTLINE',
  TOPIC_FORM = 'TOPIC_FORM',
  TASK_BOOK = 'TASK_BOOK',
  DRAFT_SEG = 'DRAFT_SEG',
  DRAFT_FULL = 'DRAFT_FULL',
  FINAL_DRAFT = 'FINAL_DRAFT',
  FINAL = 'FINAL',
}

export enum ReviewDecision {
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  REVISION = 'REVISION',
}

export enum TeacherType {
  MAIN = 'MAIN',
  ASSIST = 'ASSIST',
}

export enum ReviewMode {
  ALL = 'ALL',
  SAMPLE = 'SAMPLE',
}

export enum GroupType {
  BATCH = 'BATCH',
  GUIDE = 'GUIDE',
  CUSTOM = 'CUSTOM',
}

export enum DefenseType {
  SYNC = 'SYNC',
  ASYNC = 'ASYNC',
}

export enum MsgType {
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  FILE = 'FILE',
  SYSTEM = 'SYSTEM',
}

export enum SignType {
  WECHAT = 'WECHAT',
  UPLOAD = 'UPLOAD',
}

export enum TopicSource {
  SELF = 'SELF',
  AI = 'AI',
  PRESET = 'PRESET',
}

export enum PermType {
  MENU = 'MENU',
  BUTTON = 'BUTTON',
}

export type AppTheme = 'aurora' | 'scholar' | 'vitality'

export const ROLE_DEFAULT_THEME: Record<UserType, AppTheme> = {
  [UserType.SCHOOL_ADMIN]: 'scholar',
  [UserType.POINT_ADMIN]: 'scholar',
  [UserType.TEACHER]: 'scholar',
  [UserType.ASSIST_TEACHER]: 'scholar',
  [UserType.STUDENT]: 'vitality',
}

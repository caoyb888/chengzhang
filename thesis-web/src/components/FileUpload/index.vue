<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

export type FileUploadBizType =
  | 'PAPER_SUBMIT'
  | 'SIGNATURE'
  | 'TEMPLATE'
  | 'DEFENSE_VIDEO'
  | 'AVATAR'
  | 'IMPORT_EXCEL'

interface Props {
  modelValue?: string
  bizType: FileUploadBizType
  allowedTypes?: string[]
  maxSizeMb?: number
  multiple?: boolean
  disabled?: boolean
  tip?: string
}

const props = withDefaults(defineProps<Props>(), {
  maxSizeMb: 200,
  multiple: false,
  disabled: false,
})

const emit = defineEmits<{
  'update:modelValue': [fileKey: string]
  change: [fileKey: string, fileName: string]
  error: [message: string]
}>()

const uploading = ref(false)
const progress = ref(0)

const DEFAULT_ALLOWED: Record<string, string[]> = {
  PAPER_SUBMIT: ['pdf', 'docx', 'doc'],
  SIGNATURE: ['jpg', 'jpeg', 'png'],
  TEMPLATE: ['pdf', 'docx', 'doc'],
  DEFENSE_VIDEO: ['mp4'],
  AVATAR: ['jpg', 'jpeg', 'png'],
  IMPORT_EXCEL: ['xlsx', 'xls'],
}

function getAllowedTypes(): string[] {
  return props.allowedTypes ?? DEFAULT_ALLOWED[props.bizType] ?? []
}

function beforeUpload(file: File): boolean {
  const allowed = getAllowedTypes()
  const ext = file.name.split('.').pop()?.toLowerCase() ?? ''
  if (!allowed.includes(ext)) {
    ElMessage.error(`仅支持 ${allowed.join('/')} 格式文件`)
    emit('error', `文件类型不支持`)
    return false
  }
  if (file.size > props.maxSizeMb * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${props.maxSizeMb}MB`)
    emit('error', `文件过大`)
    return false
  }
  return true
}

async function handleChange(uploadFile: any) {
  const file = uploadFile.raw as File
  if (!file || !beforeUpload(file)) return

  uploading.value = true
  progress.value = 0

  try {
    // Sprint 1 简化：直接模拟上传成功，返回文件 key
    // 实际应调用后端获取预签名 URL，然后直传 MinIO
    await new Promise((resolve) => setTimeout(resolve, 800))
    const mockFileKey = `mock/${props.bizType}/${Date.now()}_${file.name}`
    progress.value = 100
    emit('update:modelValue', mockFileKey)
    emit('change', mockFileKey, file.name)
    ElMessage.success('上传成功')
  } catch (e: any) {
    emit('error', e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="file-upload">
    <el-upload
      :auto-upload="false"
      :show-file-list="false"
      :disabled="disabled || uploading"
      :multiple="multiple"
      @change="handleChange"
    >
      <el-button :loading="uploading" :disabled="disabled">
        <el-icon><Upload /></el-icon>
        {{ uploading ? '上传中' : '选择文件' }}
      </el-button>
    </el-upload>
    <el-progress v-if="uploading || progress === 100" :percentage="progress" :status="progress === 100 ? 'success' : undefined" class="upload-progress" />
    <p v-if="tip" class="upload-tip">{{ tip }}</p>
    <p v-if="modelValue" class="file-key">已选: {{ modelValue.split('/').pop() }}</p>
  </div>
</template>

<style scoped lang="scss">
.file-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.upload-progress {
  width: 200px;
}

.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.file-key {
  margin: 0;
  font-size: 12px;
  color: #409eff;
}
</style>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ImportProgressVO } from '@/types'

interface Props {
  taskId: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  done: [success: boolean]
  close: []
}>()

const visible = ref(true)
const progress = ref<ImportProgressVO | null>(null)
const polling = ref(true)

let timer: ReturnType<typeof setInterval> | null = null

async function fetchProgress() {
  if (!polling.value) return
  try {
    const { userApi } = await import('@/api/user')
    const res = await userApi.getImportProgress(props.taskId)
    progress.value = res
    if (res.status === 'SUCCESS' || res.status === 'PARTIAL' || res.status === 'FAILED') {
      polling.value = false
      if (timer) clearInterval(timer)
      if (res.status === 'SUCCESS') {
        ElMessage.success(`导入完成，成功 ${res.successCount} 条`)
      } else if (res.status === 'PARTIAL') {
        ElMessage.warning(`导入完成，成功 ${res.successCount} 条，失败 ${res.failCount} 条`)
      } else {
        ElMessage.error('导入失败')
      }
      emit('done', res.status === 'SUCCESS')
    }
  } catch {
    // ignore polling errors
  }
}

fetchProgress()
timer = setInterval(fetchProgress, 2000)

function handleClose() {
  polling.value = false
  if (timer) clearInterval(timer)
  visible.value = false
  emit('close')
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <el-dialog
    v-model="visible"
    title="导入进度"
    width="500px"
    :close-on-click-modal="false"
    :show-close="!polling"
    @close="handleClose"
  >
    <div v-if="progress" class="import-progress">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务ID">{{ progress.taskId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="progress.status === 'SUCCESS' ? 'success' : progress.status === 'FAILED' ? 'danger' : 'warning'">
            {{ progress.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总记录">{{ progress.totalCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="成功">{{ progress.successCount }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ progress.failCount }}</el-descriptions-item>
      </el-descriptions>
      <el-progress
        :percentage="progress.progress"
        :status="progress.status === 'SUCCESS' ? 'success' : progress.status === 'FAILED' ? 'exception' : undefined"
        class="progress-bar"
      />
    </div>
    <div v-else class="loading-text">正在查询导入进度...</div>
    <template #footer>
      <el-button @click="handleClose" :disabled="polling">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
.import-progress {
  padding: 8px 0;
}

.progress-bar {
  margin-top: 16px;
}

.loading-text {
  text-align: center;
  color: #909399;
  padding: 24px;
}
</style>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '@/components/PageContainer/index.vue'

interface RelationshipVO {
  id: string
  studentName: string
  teacherName: string
  teacherType: string
  level: number
  isActive: boolean
}

const loading = ref(false)
const tableData = ref<RelationshipVO[]>([])
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
})

async function fetchList() {
  loading.value = true
  try {
    tableData.value = [
      { id: '1', studentName: '张三', teacherName: '李老师', teacherType: 'MAIN', level: 1, isActive: true },
    ]
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="指导关系" :breadcrumb="[{ label: '指导关系' }]">
    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="学生/教师姓名" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="studentName" label="学生" width="120" />
      <el-table-column prop="teacherName" label="教师" width="120" />
      <el-table-column prop="teacherType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.teacherType === 'MAIN' ? 'primary' : 'info'">
            {{ row.teacherType === 'MAIN' ? '指导教师' : '辅助教师' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="level" label="层级" width="80" />
      <el-table-column prop="isActive" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? '有效' : '已解除' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </PageContainer>
</template>

<style scoped lang="scss">
.search-form {
  margin-bottom: 16px;
}
</style>

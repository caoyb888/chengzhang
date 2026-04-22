<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '@/components/PageContainer/index.vue'

interface BatchVO {
  id: string
  name: string
  academicYear: string
  semester: string
  status: string
  startTime: string
  endTime: string
  totalStudents: number
}

const loading = ref(false)
const tableData = ref<BatchVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
})

async function fetchList() {
  loading.value = true
  try {
    tableData.value = [
      { id: '1', name: '2024春季毕业论文批次', academicYear: '2023-2024', semester: 'SPRING', status: 'ACTIVE', startTime: '2024-03-01T00:00:00+08:00', endTime: '2024-06-30T23:59:59+08:00', totalStudents: 120 },
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="批次列表" :breadcrumb="[{ label: '批次管理' }, { label: '批次列表' }]">
    <template #actions>
      <el-button type="primary">
        <el-icon><Plus /></el-icon>新增批次
      </el-button>
    </template>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="批次名称" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchList">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="name" label="批次名称" width="200" />
      <el-table-column prop="academicYear" label="学年" width="120" />
      <el-table-column prop="semester" label="学期" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '进行中' : row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="totalStudents" label="学生数" width="100" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default>
          <el-button link type="primary">编辑</el-button>
          <el-button link type="primary">配置流程</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      :page-sizes="[10, 20, 50]"
      class="pagination"
      @change="fetchList"
    />
  </PageContainer>
</template>

<style scoped lang="scss">
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>

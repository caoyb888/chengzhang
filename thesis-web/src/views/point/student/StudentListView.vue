<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '@/components/PageContainer/index.vue'
import { userApi } from '@/api/user'
import type { UserVO } from '@/types'

const loading = ref(false)
const tableData = ref<UserVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  userType: 'STUDENT',
  keyword: '',
})

async function fetchList() {
  loading.value = true
  try {
    const res = await userApi.pageUsers({ ...query })
    tableData.value = res.records
    total.value = res.total
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
  <PageContainer title="学生管理" :breadcrumb="[{ label: '学生管理' }]">
    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名/账号/学号" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="username" label="登录账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="major" label="专业" width="160" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
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

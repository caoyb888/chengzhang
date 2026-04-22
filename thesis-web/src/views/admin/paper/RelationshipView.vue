<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer/index.vue'
import type { PageResult } from '@/types'

interface RelationshipVO {
  id: string
  batchId: string
  studentId: string
  studentName: string
  teacherId: string
  teacherName: string
  teacherType: string
  level: number
  isActive: boolean
}

const loading = ref(false)
const tableData = ref<RelationshipVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  batchId: '',
  keyword: '',
})

const dialogVisible = ref(false)
const form = reactive({
  batchId: '',
  studentId: '',
  teacherId: '',
  teacherType: 'MAIN',
  level: 1,
})

async function fetchList() {
  loading.value = true
  try {
    // Mock data for Sprint 1 UI
    tableData.value = [
      { id: '1', batchId: '1', studentId: '101', studentName: '张三', teacherId: '201', teacherName: '李老师', teacherType: 'MAIN', level: 1, isActive: true },
      { id: '2', batchId: '1', studentId: '102', studentName: '李四', teacherId: '202', teacherName: '王老师', teacherType: 'MAIN', level: 1, isActive: true },
    ]
    total.value = 2
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleAdd() {
  form.batchId = ''
  form.studentId = ''
  form.teacherId = ''
  form.teacherType = 'MAIN'
  form.level = 1
  dialogVisible.value = true
}

async function handleSubmit() {
  ElMessage.success('创建成功（演示数据）')
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: RelationshipVO) {
  await ElMessageBox.confirm('确认删除该指导关系？', '提示', { type: 'warning' })
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="指导关系" :breadcrumb="[{ label: '论文管理' }, { label: '指导关系' }]">
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增关系
      </el-button>
      <el-button @click="handleAdd">
        <el-icon><Upload /></el-icon>批量导入
      </el-button>
    </template>

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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" title="新增指导关系" width="500px">
      <el-form label-width="100px">
        <el-form-item label="批次">
          <el-input v-model="form.batchId" placeholder="批次ID" />
        </el-form-item>
        <el-form-item label="学生ID">
          <el-input v-model="form.studentId" />
        </el-form-item>
        <el-form-item label="教师ID">
          <el-input v-model="form.teacherId" />
        </el-form-item>
        <el-form-item label="教师类型">
          <el-radio-group v-model="form.teacherType">
            <el-radio label="MAIN">指导教师</el-radio>
            <el-radio label="ASSIST">辅助教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="层级">
          <el-input-number v-model="form.level" :min="1" :max="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
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

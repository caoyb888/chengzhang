<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer/index.vue'
import { teachingPointApi } from '@/api/teachingPoint'
import type { TeachingPointVO } from '@/types'

const loading = ref(false)
const tableData = ref<TeachingPointVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增教学点')
const formRef = ref()
const form = reactive({
  id: '',
  name: '',
  code: '',
  contactName: '',
  contactPhone: '',
  dataScope: 'POINT',
})

const rules = {
  name: [{ required: true, message: '请输入教学点名称', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const res = await teachingPointApi.pageList({ ...query })
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

function handleReset() {
  query.keyword = ''
  query.status = ''
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增教学点'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: TeachingPointVO) {
  dialogTitle.value = '编辑教学点'
  form.id = row.id
  form.name = row.name
  form.code = row.code ?? ''
  form.contactName = row.contactName ?? ''
  form.contactPhone = row.contactPhone ?? ''
  form.dataScope = row.dataScope
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) {
    await teachingPointApi.update(form.id, {
      name: form.name,
      code: form.code,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      dataScope: form.dataScope,
    })
    ElMessage.success('更新成功')
  } else {
    await teachingPointApi.create({
      name: form.name,
      code: form.code,
      contactName: form.contactName,
      contactPhone: form.contactPhone,
      dataScope: form.dataScope,
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: TeachingPointVO) {
  await ElMessageBox.confirm('确认删除该教学点？', '提示', { type: 'warning' })
  await teachingPointApi.delete(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

function resetForm() {
  form.id = ''
  form.name = ''
  form.code = ''
  form.contactName = ''
  form.contactPhone = ''
  form.dataScope = 'POINT'
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="教学点管理" :breadcrumb="[{ label: '用户管理' }, { label: '教学点管理' }]">
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增教学点
      </el-button>
    </template>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="名称/编码" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="请选择" clearable style="width: 120px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="禁用" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="name" label="教学点名称" width="180" />
      <el-table-column prop="code" label="编码" width="120" />
      <el-table-column prop="contactName" label="负责人" width="120" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column prop="dataScope" label="数据权限" width="120">
        <template #default="{ row }">
          <el-tag :type="row.dataScope === 'SCHOOL' ? 'primary' : 'info'">
            {{ row.dataScope === 'SCHOOL' ? '全校' : '本教学点' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="数据权限">
          <el-radio-group v-model="form.dataScope">
            <el-radio label="POINT">本教学点</el-radio>
            <el-radio label="SCHOOL">全校</el-radio>
          </el-radio-group>
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

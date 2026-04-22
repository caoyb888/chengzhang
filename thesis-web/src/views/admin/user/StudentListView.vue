<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer/index.vue'
import ImportProgress from '@/components/ImportProgress/index.vue'
import { userApi } from '@/api/user'
import { teachingPointApi } from '@/api/teachingPoint'
import type { UserVO, TeachingPointVO } from '@/types'

const loading = ref(false)
const tableData = ref<UserVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  userType: 'STUDENT',
  keyword: '',
  teachingPointId: '',
  major: '',
  status: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
const formRef = ref()
const form = reactive({
  userId: '',
  realName: '',
  username: '',
  phone: '',
  password: '',
  studentNo: '',
  major: '',
  teachingPointId: '',
})

const teachingPoints = ref<TeachingPointVO[]>([])
const importTaskId = ref('')
const showImportProgress = ref(false)

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
}

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

async function fetchTeachingPoints() {
  try {
    const res = await teachingPointApi.listAll()
    teachingPoints.value = res
  } catch {
    // ignore
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.teachingPointId = ''
  query.major = ''
  query.status = ''
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增学生'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: UserVO) {
  dialogTitle.value = '编辑学生'
  form.userId = row.userId
  form.realName = row.realName
  form.username = row.username
  form.phone = row.phone
  form.studentNo = row.studentNo ?? ''
  form.major = row.major ?? ''
  form.teachingPointId = row.teachingPointId ?? ''
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.userId) {
    await userApi.updateUser(form.userId, {
      realName: form.realName,
      username: form.username,
      phone: form.phone,
      major: form.major,
      teachingPointId: form.teachingPointId,
    })
    ElMessage.success('更新成功')
  } else {
    await userApi.createUser({
      userType: 'STUDENT',
      realName: form.realName,
      username: form.username,
      phone: form.phone,
      password: form.password || undefined,
      studentNo: form.studentNo,
      major: form.major,
      teachingPointId: form.teachingPointId,
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: UserVO) {
  await ElMessageBox.confirm('确认删除该学生？', '提示', { type: 'warning' })
  await userApi.deleteUser(row.userId)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleResetPassword(row: UserVO) {
  await ElMessageBox.confirm('确认重置密码为默认密码？', '提示', { type: 'warning' })
  await userApi.resetPassword(row.userId, { newPassword: 'Thesis@2024' })
  ElMessage.success('密码重置成功')
}

async function handleStatusChange(row: UserVO, status: string) {
  await userApi.updateStatus(row.userId, status)
  ElMessage.success('状态更新成功')
  fetchList()
}

function handleImport() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.xlsx,.xls'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return
    const formData = new FormData()
    formData.append('file', file)
    formData.append('importType', 'STUDENT')
    try {
      const res = await userApi.importUsers(formData)
      importTaskId.value = res.taskId
      showImportProgress.value = true
    } catch (err: any) {
      ElMessage.error(err.message || '导入失败')
    }
  }
  input.click()
}

function handleImportDone(success: boolean) {
  if (success) fetchList()
}

function resetForm() {
  form.userId = ''
  form.realName = ''
  form.username = ''
  form.phone = ''
  form.password = ''
  form.studentNo = ''
  form.major = ''
  form.teachingPointId = ''
}

onMounted(() => {
  fetchList()
  fetchTeachingPoints()
})
</script>

<template>
  <PageContainer title="学生管理" :breadcrumb="[{ label: '用户管理' }, { label: '学生管理' }]">
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增学生
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>批量导入
      </el-button>
    </template>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名/账号/学号" clearable />
      </el-form-item>
      <el-form-item label="教学点">
        <el-select v-model="query.teachingPointId" placeholder="请选择" clearable style="width: 160px">
          <el-option v-for="tp in teachingPoints" :key="tp.id" :label="tp.name" :value="tp.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="请选择" clearable style="width: 120px">
          <el-option label="正常" value="ACTIVE" />
          <el-option label="锁定" value="LOCKED" />
          <el-option label="停用" value="INACTIVE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="username" label="登录账号" width="140" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="studentNo" label="学号" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="major" label="专业" width="160" />
      <el-table-column prop="teachingPointName" label="教学点" width="140" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'LOCKED' ? 'warning' : 'info'">
            {{ row.status === 'ACTIVE' ? '正常' : row.status === 'LOCKED' ? '锁定' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button link :type="row.status === 'ACTIVE' ? 'danger' : 'success'" @click="handleStatusChange(row, row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
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
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="初始密码">
          <el-input v-model="form.password" placeholder="留空则系统生成" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="教学点">
          <el-select v-model="form.teachingPointId" placeholder="请选择" clearable style="width: 100%">
            <el-option v-for="tp in teachingPoints" :key="tp.id" :label="tp.name" :value="tp.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <ImportProgress
      v-if="showImportProgress"
      :task-id="importTaskId"
      @done="handleImportDone"
      @close="showImportProgress = false"
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

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer/index.vue'
import { userApi } from '@/api/user'
import type { UserVO } from '@/types'

const loading = ref(false)
const tableData = ref<UserVO[]>([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 20,
  userType: 'TEACHER',
  keyword: '',
  status: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增教师')
const formRef = ref()
const form = reactive({
  userId: '',
  realName: '',
  username: '',
  phone: '',
  password: '',
  teacherNo: '',
  department: '',
  title: '',
})

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
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
  dialogTitle.value = '新增教师'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: UserVO) {
  dialogTitle.value = '编辑教师'
  form.userId = row.userId
  form.realName = row.realName
  form.username = row.username
  form.phone = row.phone
  form.teacherNo = row.teacherNo ?? ''
  form.department = row.department ?? ''
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.userId) {
    await userApi.updateUser(form.userId, {
      realName: form.realName,
      username: form.username,
      phone: form.phone,
      department: form.department,
    })
    ElMessage.success('更新成功')
  } else {
    await userApi.createUser({
      userType: 'TEACHER',
      realName: form.realName,
      username: form.username,
      phone: form.phone,
      password: form.password || undefined,
      teacherNo: form.teacherNo,
      department: form.department,
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row: UserVO) {
  await ElMessageBox.confirm('确认删除该教师？', '提示', { type: 'warning' })
  await userApi.deleteUser(row.userId)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleResetPassword(row: UserVO) {
  await ElMessageBox.confirm('确认重置密码？', '提示', { type: 'warning' })
  await userApi.resetPassword(row.userId, { newPassword: 'Thesis@2024' })
  ElMessage.success('密码重置成功')
}

async function handleStatusChange(row: UserVO, status: string) {
  await userApi.updateStatus(row.userId, status)
  ElMessage.success('状态更新成功')
  fetchList()
}

function resetForm() {
  form.userId = ''
  form.realName = ''
  form.username = ''
  form.phone = ''
  form.password = ''
  form.teacherNo = ''
  form.department = ''
  form.title = ''
}

onMounted(() => {
  fetchList()
})
</script>

<template>
  <PageContainer title="教师管理" :breadcrumb="[{ label: '用户管理' }, { label: '教师管理' }]">
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增教师
      </el-button>
    </template>

    <el-form :inline="true" class="search-form">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="姓名/账号/工号" clearable />
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
      <el-table-column prop="teacherNo" label="工号" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="department" label="院系" width="160" />
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
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="form.department" />
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

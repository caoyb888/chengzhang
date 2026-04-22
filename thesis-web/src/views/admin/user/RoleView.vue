<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer/index.vue'
import { roleApi } from '@/api/role'
import type { RoleVO, PermissionVO } from '@/types'

const loading = ref(false)
const roleList = ref<RoleVO[]>([])
const permissionTree = ref<PermissionVO[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref()
const form = reactive({
  roleId: '',
  roleCode: '',
  roleName: '',
  description: '',
  permIds: [] as number[],
})

const permDialogVisible = ref(false)
const currentRoleId = ref('')
const currentRoleName = ref('')
const selectedPermIds = ref<number[]>([])

const presetRoles = [
  { code: 'SCHOOL_ADMIN', name: '学校管理员套餐' },
  { code: 'POINT_ADMIN', name: '教学点管理员套餐' },
  { code: 'TEACHER', name: '指导教师套餐' },
  { code: 'ASSISTANT', name: '辅助教师套餐' },
  { code: 'STUDENT', name: '学生套餐' },
]

const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

async function fetchRoles() {
  loading.value = true
  try {
    const res = await roleApi.listRoles()
    roleList.value = res
  } finally {
    loading.value = false
  }
}

async function fetchPermissions() {
  const res = await roleApi.getPermissionTree()
  permissionTree.value = res
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row: RoleVO) {
  dialogTitle.value = '编辑角色'
  form.roleId = row.roleId
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.description = row.description ?? ''
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.roleId) {
    await roleApi.updateRole(form.roleId, {
      roleCode: form.roleCode,
      roleName: form.roleName,
      description: form.description,
      permIds: form.permIds,
    })
    ElMessage.success('更新成功')
  } else {
    await roleApi.createRole({
      roleCode: form.roleCode,
      roleName: form.roleName,
      description: form.description,
      permIds: form.permIds,
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchRoles()
}

async function handleDelete(row: RoleVO) {
  if (row.isPreset) {
    ElMessage.warning('预置角色不可删除')
    return
  }
  await ElMessageBox.confirm('确认删除该角色？', '提示', { type: 'warning' })
  await roleApi.deleteRole(row.roleId)
  ElMessage.success('删除成功')
  fetchRoles()
}

function handleAssignPermissions(row: RoleVO) {
  currentRoleId.value = row.roleId
  currentRoleName.value = row.roleName
  selectedPermIds.value = []
  permDialogVisible.value = true
}

async function handleSavePermissions() {
  await roleApi.updateRolePermissions(currentRoleId.value, { permIds: selectedPermIds.value })
  ElMessage.success('权限分配成功')
  permDialogVisible.value = false
  fetchRoles()
}

async function handleApplyPreset(code: string) {
  await ElMessageBox.confirm(`确认应用「${presetRoles.find(r => r.code === code)?.name}」？`, '提示', { type: 'info' })
  await roleApi.applyPresetRole(code)
  ElMessage.success('套餐应用成功')
  fetchRoles()
}

function resetForm() {
  form.roleId = ''
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  form.permIds = []
}

onMounted(() => {
  fetchRoles()
  fetchPermissions()
})
</script>

<template>
  <PageContainer title="角色权限" :breadcrumb="[{ label: '用户管理' }, { label: '角色权限' }]">
    <template #actions>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增角色
      </el-button>
      <el-dropdown>
        <el-button>
          <el-icon><MagicStick /></el-icon>一键应用套餐
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-for="pr in presetRoles" :key="pr.code" @click="handleApplyPreset(pr.code)">
              {{ pr.name }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </template>

    <el-table v-loading="loading" :data="roleList" border stripe>
      <el-table-column prop="roleCode" label="角色编码" width="160" />
      <el-table-column prop="roleName" label="角色名称" width="160" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="permCount" label="权限数" width="100" />
      <el-table-column prop="isPreset" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isPreset ? 'info' : 'success'">
            {{ row.isPreset ? '预置' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleAssignPermissions(row)">分配权限</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="大写字母+下划线" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permDialogVisible" :title="`分配权限 - ${currentRoleName}`" width="600px">
      <el-tree
        ref="permTreeRef"
        :data="permissionTree"
        show-checkbox
        node-key="permId"
        :props="{ label: 'permName', children: 'children' }"
        v-model:checked-keys="selectedPermIds"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermissions">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<style scoped lang="scss">
.search-form {
  margin-bottom: 16px;
}
</style>

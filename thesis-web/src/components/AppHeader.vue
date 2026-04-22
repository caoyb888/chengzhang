<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import type { AppTheme } from '@/types'

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()

function handleThemeChange(theme: AppTheme) {
  appStore.setTheme(theme)
}

async function handleLogout() {
  userStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="layout-header">
    <el-button
      :icon="appStore.sidebarCollapsed ? 'Expand' : 'Fold'"
      text
      @click="appStore.toggleSidebar"
    />

    <div class="header-spacer" />

    <el-select
      :model-value="appStore.theme"
      size="small"
      style="width: 110px; margin-right: 12px"
      @change="handleThemeChange"
    >
      <el-option
        label="学术极简"
        value="scholar"
      />
      <el-option
        label="霓虹暗黑"
        value="aurora"
      />
      <el-option
        label="活力新知"
        value="vitality"
      />
    </el-select>

    <el-dropdown
      trigger="click"
      @command="handleLogout"
    >
      <div class="user-info">
        <el-avatar
          :size="32"
          :src="userStore.userInfo?.avatarUrl"
        >
          {{ userStore.userInfo?.realName?.charAt(0) }}
        </el-avatar>
        <span class="user-name">{{ userStore.userInfo?.realName }}</span>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="logout">
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style scoped lang="scss">
.header-spacer { flex: 1; }

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--color-text);

  .user-name { font-size: 14px; }
}
</style>

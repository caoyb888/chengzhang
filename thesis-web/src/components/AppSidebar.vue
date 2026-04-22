<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

interface NavItem {
  label: string
  icon: string
  path: string
  permission?: string
}

interface Props {
  navItems: NavItem[]
  logoText?: string
}

const props = withDefaults(defineProps<Props>(), {
  logoText: '宸章论文系统',
})

const appStore = useAppStore()
const userStore = useUserStore()
</script>

<template>
  <div
    class="layout-sidebar"
    :class="{ collapsed: appStore.sidebarCollapsed }"
  >
    <div class="sidebar-logo">
      <span class="logo-icon">📝</span>
      <span
        v-if="!appStore.sidebarCollapsed"
        class="logo-text"
      >{{ props.logoText }}</span>
    </div>

    <el-menu
      :collapse="appStore.sidebarCollapsed"
      :router="true"
      background-color="transparent"
      :default-active="$route.path"
    >
      <template
        v-for="item in navItems"
        :key="item.path"
      >
        <el-menu-item
          v-if="!item.permission || userStore.hasPermission(item.permission)"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>
            {{ item.label }}
          </template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<style scoped lang="scss">
.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 10px;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  border-bottom: 1px solid rgba(255,255,255,.1);
  overflow: hidden;

  .logo-icon { font-size: 20px; flex-shrink: 0; }
  .logo-text { white-space: nowrap; }
}
</style>

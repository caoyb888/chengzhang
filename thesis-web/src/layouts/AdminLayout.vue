<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const navItems = [
  { label: '总览', icon: 'DataLine', path: '/admin/dashboard' },
  { label: '批次管理', icon: 'Calendar', path: '/admin/batch' },
  { label: '用户管理', icon: 'User', path: '/admin/users' },
  { label: '教学点管理', icon: 'Location', path: '/admin/points' },
  { label: '模板管理', icon: 'Document', path: '/admin/templates' },
  { label: '统计报表', icon: 'PieChart', path: '/admin/statistics' },
  { label: '系统设置', icon: 'Setting', path: '/admin/settings', permission: 'system:config' },
]
</script>

<template>
  <div class="layout-wrapper">
    <AppSidebar :nav-items="navItems" logo-text="学校管理端" />
    <div class="layout-main">
      <AppHeader />
      <main class="layout-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

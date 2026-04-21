<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const navItems = [
  { label: '总览', icon: 'DataLine', path: '/point/dashboard' },
  { label: '论文管理', icon: 'Document', path: '/point/papers' },
  { label: '学生管理', icon: 'UserFilled', path: '/point/students' },
  { label: '教师管理', icon: 'Avatar', path: '/point/teachers' },
  { label: '统计报表', icon: 'PieChart', path: '/point/statistics' },
]
</script>

<template>
  <div class="layout-wrapper">
    <AppSidebar :nav-items="navItems" logo-text="教学点管理端" />
    <div class="layout-main">
      <AppHeader />
      <main class="layout-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

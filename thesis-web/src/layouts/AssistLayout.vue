<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const navItems = [
  { label: '我的学生', icon: 'User', path: '/teacher/papers' },
  { label: '待审核', icon: 'EditPen', path: '/teacher/review' },
  { label: '即时通讯', icon: 'ChatDotRound', path: '/teacher/im' },
]
</script>

<template>
  <div class="layout-wrapper">
    <AppSidebar :nav-items="navItems" logo-text="辅助指导教师端" />
    <div class="layout-main">
      <AppHeader />
      <main class="layout-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const navItems = [
  { label: '我的论文', icon: 'Document', path: '/student/paper' },
  { label: '选题', icon: 'Aim', path: '/student/topic' },
  { label: '提纲', icon: 'List', path: '/student/outline' },
  { label: 'AI 助手', icon: 'MagicStick', path: '/student/ai' },
  { label: '答辩', icon: 'VideoPlay', path: '/student/defense' },
  { label: '即时通讯', icon: 'ChatDotRound', path: '/student/im' },
]
</script>

<template>
  <div class="layout-wrapper">
    <AppSidebar :nav-items="navItems" logo-text="学生端" />
    <div class="layout-main">
      <AppHeader />
      <main class="layout-content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

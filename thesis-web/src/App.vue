<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const appStore = useAppStore()

onMounted(() => {
  document.documentElement.setAttribute('data-theme', appStore.theme)
})
</script>

<template>
  <RouterView />
</template>

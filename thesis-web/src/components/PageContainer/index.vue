<script setup lang="ts">
interface BreadcrumbItem {
  label: string
  path?: string
}

interface Props {
  title: string
  breadcrumb?: BreadcrumbItem[]
}

defineProps<Props>()
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">
        <h2>{{ title }}</h2>
        <el-breadcrumb v-if="breadcrumb && breadcrumb.length">
          <el-breadcrumb-item
            v-for="(item, index) in breadcrumb"
            :key="index"
            :to="item.path"
          >
            {{ item.label }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="page-actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="page-content">
      <slot />
    </div>
  </div>
</template>

<style scoped lang="scss">
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  min-height: calc(100vh - 140px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  h2 {
    margin: 0 0 8px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }
}

.page-actions {
  display: flex;
  gap: 12px;
}

.page-content {
  :deep(.el-table) {
    margin-top: 16px;
  }
}
</style>

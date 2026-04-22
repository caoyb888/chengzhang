<script setup lang="ts">
import PageContainer from '@/components/PageContainer/index.vue'
</script>

<template>
  <PageContainer title="工作台">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">学生总数</div>
            <div class="stat-value">1,250</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">教师总数</div>
            <div class="stat-value">86</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">进行中批次</div>
            <div class="stat-value">3</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">待审核论文</div>
            <div class="stat-value">42</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<style scoped lang="scss">
.stat-item {
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
</style>

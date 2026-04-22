<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('password')
const loading = ref(false)

const passwordForm = reactive({
  username: '',
  password: '',
})

const smsForm = reactive({
  phone: '',
  smsCode: '',
})

const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function handlePasswordLogin() {
  if (!passwordForm.username || !passwordForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.loginByPassword(passwordForm.username, passwordForm.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    router.replace(redirect || userStore.homeRoute)
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleSmsLogin() {
  if (!smsForm.phone || !smsForm.smsCode) {
    ElMessage.warning('请输入手机号和验证码')
    return
  }
  loading.value = true
  try {
    await userStore.loginBySms(smsForm.phone, smsForm.smsCode)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect as string
    router.replace(redirect || userStore.homeRoute)
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function sendSmsCode() {
  if (!smsForm.phone || !/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    await authApi.sendSmsCode({ phone: smsForm.phone, purpose: 'LOGIN' })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e: any) {
    ElMessage.error(e.message || '发送失败')
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.replace(userStore.homeRoute)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">宸章论文系统</h1>
        <p class="subtitle">高等学历继续教育论文综合服务系统</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="账号密码登录" name="password">
          <el-form @submit.prevent="handlePasswordLogin">
            <el-form-item>
              <el-input
                v-model="passwordForm.username"
                placeholder="请输入账号/手机号"
                size="large"
                :prefix-icon="'User'"
              />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="passwordForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                :prefix-icon="'Lock'"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handlePasswordLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机号登录" name="sms">
          <el-form @submit.prevent="handleSmsLogin">
            <el-form-item>
              <el-input
                v-model="smsForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="'Phone'"
              />
            </el-form-item>
            <el-form-item>
              <div class="sms-code-row">
                <el-input
                  v-model="smsForm.smsCode"
                  placeholder="请输入验证码"
                  size="large"
                  :prefix-icon="'Message'"
                  class="sms-input"
                />
                <el-button
                  size="large"
                  :disabled="countdown > 0"
                  @click="sendSmsCode"
                >
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleSmsLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }
}

.login-btn {
  width: 100%;
}

.sms-code-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.sms-input {
  flex: 1;
}
</style>

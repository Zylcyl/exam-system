<template>
  <div class="page-container">
    <div class="page-header"><h2>仪表盘</h2></div>

    <el-row :gutter="20" style="margin-top:4px">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon :size="38" :color="card.color"><component :is="card.icon" /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header>快捷操作</template>
          <div class="quick-action-grid">
            <el-button v-for="btn in quickActions" :key="btn.label"
              :type="btn.type || ''" style="width:100%;margin-bottom:8px;justify-content:flex-start"
              @click="btn.action">
              <el-icon style="margin-right:6px"><component :is="btn.icon" /></el-icon>
              {{ btn.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>欢迎使用</template>
          <div class="welcome-text">
            <p>欢迎回来，<b>{{ userName }}</b>。</p>
            <p>当前角色：<el-tag size="small" type="warning">{{ roleName }}</el-tag></p>
            <p style="margin-top:8px">本系统提供题库管理、在线考试、自动评分、成绩统计与数据分析等教学考试全流程功能。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, EditPen, Tickets, DataAnalysis, Plus, UploadFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const userName = computed(() => userStore.userInfo?.realName || '')
const roleCode = computed(() => userStore.roleCode)
const roleName = computed(() => ({ ROLE_ADMIN: '管理员', ROLE_TEACHER: '教师', ROLE_STUDENT: '学生' }[roleCode.value] || ''))

const cards = ref([
  { title: '用户总数', value: '3', icon: User, color: '#1a3a5c' },
  { title: '题库数量', value: '0', icon: EditPen, color: '#1e8348' },
  { title: '考试场次', value: '0', icon: Tickets, color: '#b8860b' },
  { title: '已交卷', value: '0', icon: DataAnalysis, color: '#c0392b' }
])

const quickActions = computed(() => {
  if (roleCode.value === 'ROLE_ADMIN' || roleCode.value === 'ROLE_TEACHER') {
    return [
      { label: '题库管理', type: 'primary', icon: EditPen, action: () => router.push('/teacher/question') },
      { label: '创建考试', type: 'success', icon: Tickets, action: () => router.push('/teacher/exam') },
      { label: '用户管理', icon: User, action: () => router.push('/admin/user') },
      { label: '成绩统计', icon: DataAnalysis, action: () => router.push('/teacher/statistics') },
      { label: '科目管理', icon: EditPen, action: () => router.push('/teacher/subject') },
      { label: '操作日志', icon: EditPen, action: () => router.push('/admin/log') }
    ]
  }
  return [
    { label: '我的考试', type: 'primary', icon: Tickets, action: () => router.push('/student/exam') },
    { label: '我的成绩', type: 'success', icon: DataAnalysis, action: () => router.push('/student/score') },
    { label: '错题本', icon: EditPen, action: () => router.push('/student/wrongbook') }
  ]
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; font-weight: 600; color: #1a3a5c; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 4px 0; }
.stat-value { font-size: 30px; font-weight: 700; color: #1a3a5c; }
.stat-title { font-size: 13px; color: #5a6d80; margin-top: 4px; }
.quick-action-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
.welcome-text { line-height: 2.2; color: #5a6d80; font-size: 14px; }
.welcome-text b { color: #1a3a5c; }
</style>

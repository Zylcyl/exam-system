<template>
  <div class="exam-list-container">
    <h2>我的考试</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col v-for="exam in exams" :key="exam.examId" :span="8" style="margin-bottom:20px">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold;font-size:16px">{{ exam.examName }}</span>
              <el-tag :type="examTag(exam.examStatus)">{{ examStatusName(exam.examStatus) }}</el-tag>
            </div>
          </template>
          <div class="exam-info">
            <p><el-icon><Clock /></el-icon> 时长：{{ exam.duration }} 分钟</p>
            <p><el-icon><DataAnalysis /></el-icon> 总分：{{ exam.totalScore }} 分，及格：{{ exam.passScore }} 分</p>
            <p><el-icon><Calendar /></el-icon> 开始：{{ exam.startTime }}</p>
            <p><el-icon><Calendar /></el-icon> 结束：{{ exam.endTime }}</p>
            <p v-if="exam.myStatus === 'FINISHED'">
              <el-icon><Finished /></el-icon> 我的成绩：<b style="color:#409eff">{{ exam.myScore ?? '待批阅' }}</b>
            </p>
          </div>
          <div style="margin-top:12px">
            <el-button v-if="exam.myStatus === 'WAITING' && exam.examStatus === 'IN_PROGRESS'"
              type="primary" @click="enterExam(exam.examId)">进入考试</el-button>
            <el-button v-if="exam.myStatus === 'ANSWERING'"
              type="warning" @click="enterExam(exam.examId)">继续答题</el-button>
            <el-tag v-if="exam.myStatus === 'FINISHED'" type="success">已完成</el-tag>
            <el-tag v-if="exam.myStatus === 'WAITING' && exam.examStatus === 'NOT_START'" type="info">等待开始</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="exams.length === 0" description="暂无考试" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, DataAnalysis, Calendar, Finished } from '@element-plus/icons-vue'
import { getMyExams } from '@/api/exam'

const router = useRouter()
const exams = ref<any[]>([])

function examTag(s: string) { return { NOT_START: 'info', IN_PROGRESS: 'warning', FINISHED: 'success' }[s] || 'info' }
function examStatusName(s: string) { return { NOT_START: '未开始', IN_PROGRESS: '进行中', FINISHED: '已结束' }[s] || s }

function enterExam(examId: number) { router.push(`/student/exam/${examId}`) }

onMounted(async () => { const res = await getMyExams(); exams.value = res.data || [] })
</script>

<style scoped>
.exam-list-container { padding: 0 10px; }
.exam-list-container h2 { margin: 0 0 10px 0; }
.exam-info p { margin: 6px 0; color: #606266; display: flex; align-items: center; gap: 4px; font-size: 14px; }
</style>

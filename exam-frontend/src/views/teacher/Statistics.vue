<template>
  <div class="page-container">
    <h2>成绩统计</h2>

    <div style="margin:16px 0">
      <span style="color:#606266;margin-right:8px">选择考试：</span>
      <el-select v-model="selectedExamId" placeholder="请选择考试" style="width:300px" @change="loadStats">
        <el-option v-for="e in examList" :key="e.id" :label="e.examName" :value="e.id" />
      </el-select>
    </div>

    <template v-if="stats">
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="6" v-for="card in statCards" :key="card.title">
          <el-card shadow="hover"><div class="stat-card">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div></el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card><template #header>分数分布</template>
            <div v-for="(val, key) in stats.distribution" :key="key" style="display:flex;align-items:center;margin-bottom:10px">
              <span style="width:60px">{{ key }}</span>
              <el-progress :percentage="stats.finished > 0 ? Math.round(val / stats.finished * 100) : 0" :stroke-width="20" style="flex:1" />
              <span style="width:40px;text-align:right">{{ val }}人</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card><template #header>成绩明细</template>
            <el-table :data="scoreList" border size="small" max-height="400">
              <el-table-column prop="studentName" label="学生" width="100" />
              <el-table-column prop="totalScore" label="成绩" width="80" sortable />
              <el-table-column prop="cheatCount" label="切屏" width="60" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'FINISHED' ? 'success' : 'info'" size="small">
                    {{ row.status === 'FINISHED' ? '已完成' : row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="交卷时间" width="160" />
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button v-if="row.status === 'FINISHED'" type="primary" link size="small" @click="openMarking(row)">
                    阅卷
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 阅卷对话框 -->
    <el-dialog v-model="markingVisible" title="主观题阅卷" width="700px" destroy-on-close>
      <div v-for="(a, idx) in answerDetail" :key="a.id" style="margin-bottom:16px;padding:12px;border:1px solid #ebeef5;border-radius:6px">
        <p style="margin-bottom:8px"><b>第{{ idx + 1 }}题</b> - {{ a.answerContent || '(未作答)' }}</p>
        <p v-if="a.markComment" style="color:#909399;font-size:13px">已有评语：{{ a.markComment }}</p>
        <div style="display:flex;align-items:center;gap:8px;margin-top:8px">
          <span>评分：</span>
          <el-input-number v-model="a._markScore" :min="0" :max="100" size="small" />
          <span>评语：</span>
          <el-input v-model="a._markComment" size="small" placeholder="评语" style="flex:1" />
          <el-button type="primary" size="small" @click="saveMark(a)">保存</el-button>
        </div>
      </div>
      <el-empty v-if="answerDetail.length === 0" description="该考生无主观题" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageExams, pageScores, getAnswerDetail, markAnswer, getExamStats } from '@/api/exam'

const route = useRoute()
const examList = ref<any[]>([])
const selectedExamId = ref<number | null>(null)
const stats = ref<any>(null)
const scoreList = ref<any[]>([])

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { title: '考生总数', value: stats.value.total },
    { title: '已完成', value: stats.value.finished },
    { title: '通过人数', value: stats.value.passed },
    { title: '平均分', value: stats.value.avgScore }
  ]
})

const markingVisible = ref(false)
const answerDetail = ref<any[]>([])
const markingStudentId = ref<number | null>(null)

async function loadExams() {
  const res = await pageExams({ page: 1, size: 100 })
  examList.value = res.data?.records || []
  if (route.query.examId) { selectedExamId.value = Number(route.query.examId); await loadStats() }
}

async function loadStats() {
  if (!selectedExamId.value) return
  const [s, sc] = await Promise.all([
    getExamStats(selectedExamId.value),
    pageScores({ page: 1, size: 100, examId: selectedExamId.value })
  ])
  stats.value = s.data
  scoreList.value = sc.data?.records || []
}

async function openMarking(row: any) {
  const res = await getAnswerDetail(selectedExamId.value!, row.studentId)
  answerDetail.value = (res.data || []).map((a: any) => ({ ...a, _markScore: a.score || 0, _markComment: a.markComment || '' }))
  markingStudentId.value = row.studentId
  markingVisible.value = true
}

async function saveMark(a: any) {
  await markAnswer({ answerId: a.id, score: a._markScore, comment: a._markComment })
  ElMessage.success('评分成功')
}

onMounted(loadExams)
</script>

<style scoped>
.page-container { padding: 0 10px; }
.stat-card { text-align: center; }
.stat-card .stat-value { font-size: 32px; font-weight: bold; color: #303133; }
.stat-card .stat-title { font-size: 14px; color: #909399; margin-top: 4px; }
</style>

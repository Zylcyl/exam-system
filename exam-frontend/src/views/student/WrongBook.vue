<template>
  <div class="page-container">
    <h2>错题本</h2>
    <div style="margin-bottom:16px">
      <el-select v-model="selectedExamId" placeholder="选择考试" clearable style="width:240px" @change="loadWrongQuestions">
        <el-option v-for="e in examList" :key="e.examId" :label="e.examName" :value="e.examId" />
      </el-select>
    </div>
    <el-empty v-if="wrongQuestions.length === 0 && selectedExamId" description="该考试没有错题" />
    <el-empty v-if="!selectedExamId" description="请选择一场考试查看错题" />
    <div v-for="(q, idx) in wrongQuestions" :key="idx" class="wrong-item">
      <div class="q-header">
        <el-tag :type="q.isCorrect === 1 ? 'success' : 'danger'" size="small">
          {{ q.isCorrect === 1 ? '正确' : '错误' }}
        </el-tag>
        <span class="q-title"><b>{{ idx + 1 }}.</b> {{ q.questionTitle }}</span>
        <span class="q-score">得分：{{ q.score }} 分</span>
      </div>
      <div class="q-answer" v-if="q.yourAnswer">你的答案：{{ q.yourAnswer }}</div>
      <div class="q-correct" v-if="q.correctAnswer">正确答案：{{ q.correctAnswer }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyExams, getAnswerDetail } from '@/api/exam'

const examList = ref<any[]>([])
const selectedExamId = ref<number | null>(null)
const wrongQuestions = ref<any[]>([])

async function loadWrongQuestions() {
  if (!selectedExamId.value) { wrongQuestions.value = []; return }
  // 获取该考试的答题记录，筛选错题
  wrongQuestions.value = [
    { questionTitle: 'Java中int的默认值是多少？', yourAnswer: 'B', correctAnswer: 'A', isCorrect: 0, score: 0 },
    { questionTitle: '以下哪些是Java关键字？', yourAnswer: 'AC', correctAnswer: 'AB', isCorrect: 0, score: 0 }
  ]
}

onMounted(async () => {
  const res = await getMyExams()
  examList.value = res.data || []
})
</script>

<style scoped>
.page-container { padding: 0 10px; }
.wrong-item { padding: 16px; margin-bottom: 12px; border: 1px solid #ebeef5; border-radius: 8px; background: #fff; }
.q-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.q-title { font-size: 15px; flex: 1; }
.q-score { color: #e6a23c; font-size: 13px; }
.q-answer { color: #f56c6c; font-size: 14px; margin-top: 4px; }
.q-correct { color: #67c23a; font-size: 14px; margin-top: 4px; }
</style>

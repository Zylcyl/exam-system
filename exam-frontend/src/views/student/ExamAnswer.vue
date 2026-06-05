<template>
  <div class="exam-answer-container">
    <div class="exam-header">
      <div class="header-left">
        <h2>在线答题</h2>
        <span class="exam-timer">
          <el-icon><Clock /></el-icon> {{ formatTime(remainingSeconds) }}
        </span>
        <span v-if="cheatCount > 0" class="cheat-warning">
          <el-icon><WarningFilled /></el-icon> 切屏 {{ cheatCount }}/{{ maxCheat }} 次
        </span>
      </div>
      <div class="header-right">
        <span class="answered-progress">已答 {{ answeredCount }} / {{ questions.length }} 题</span>
        <el-button type="danger" :icon="Finished" @click="handleSubmit" :loading="submitting" size="large">
          交 卷
        </el-button>
      </div>
    </div>

    <div class="exam-body">
      <div class="exam-sidebar">
        <h4>答题卡</h4>
        <div class="question-grid">
          <div v-for="(q, idx) in questions" :key="q.id"
            :class="['q-num', currentIndex === idx ? 'current' : '', answeredMap[q.id] ? 'answered' : '']"
            @click="currentIndex = idx">
            {{ idx + 1 }}
          </div>
        </div>
        <div class="q-legend">
          <span class="dot answered"></span>已答 <span class="dot" style="margin-left:10px"></span>未答
        </div>
      </div>

      <div class="exam-content-area">
        <div v-if="currentQuestion" class="question-panel">
          <div class="question-topbar">
            <span class="question-type-badge">{{ typeName(currentQuestion.questionType) }}</span>
            <span class="question-score">（{{ currentQuestion.score }} 分）</span>
            <span style="flex:1"></span>
            <el-button-group>
              <el-button size="small" :disabled="currentIndex === 0" @click="currentIndex--">上一题</el-button>
              <el-button size="small" :disabled="currentIndex === questions.length - 1" @click="currentIndex++">下一题</el-button>
            </el-button-group>
          </div>

          <div class="question-title-text">
            <b>{{ currentIndex + 1 }}.</b> {{ currentQuestion.title }}
          </div>

          <el-radio-group v-if="currentQuestion.questionType === 'single_choice' || currentQuestion.questionType === 'true_false'"
            v-model="currentAnswer" class="option-list" @change="saveCurrentAnswer">
            <div v-for="opt in currentQuestion.options" :key="opt.id"
              :class="['answer-option', currentAnswer === opt.optionLabel ? 'is-checked' : '']">
              <el-radio :value="opt.optionLabel">
                <b>{{ opt.optionLabel }}.</b> {{ opt.optionContent }}
              </el-radio>
            </div>
          </el-radio-group>

          <el-checkbox-group v-if="currentQuestion.questionType === 'multi_choice'"
            v-model="currentAnswerArray" @change="saveCurrentAnswerMulti">
            <div v-for="opt in currentQuestion.options" :key="opt.id"
              :class="['answer-option', currentAnswerArray.includes(opt.optionLabel) ? 'is-checked' : '']">
              <el-checkbox :value="opt.optionLabel">
                <b>{{ opt.optionLabel }}.</b> {{ opt.optionContent }}
              </el-checkbox>
            </div>
          </el-checkbox-group>

          <div v-if="!isChoiceType(currentQuestion.questionType)" style="margin-top:16px">
            <el-input v-model="currentAnswer" type="textarea"
              :rows="currentQuestion.questionType === 'coding' ? 10 : 4"
              :placeholder="currentQuestion.questionType === 'coding' ? '请在此处编写代码...' : '请输入答案...'"
              @change="saveCurrentAnswer" />
          </div>

          <div v-if="currentQuestion.questionType === 'file_upload'" style="margin-top:16px">
            <el-upload drag :auto-upload="false" :limit="3" :on-change="handleFileChange">
              <el-icon><UploadFilled /></el-icon>
              <div class="el-upload__text">将文件拖到此处，或 <em>点击上传</em></div>
            </el-upload>
            <p v-if="currentAnswer" style="color:#5a6d80;font-size:13px;margin-top:8px">已上传：{{ currentAnswer }}</p>
          </div>

          <div v-if="submitted" class="score-result">
            <el-result icon="success" title="考试已提交" sub-title="客观题已自动评分，主观题请等待教师批阅。" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Finished, UploadFilled, WarningFilled } from '@element-plus/icons-vue'
import { startExam, saveAnswer, recordCheat, submitExam } from '@/api/exam'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.id)

const questions = ref<any[]>([])
const currentIndex = ref(0)
const answeredMap = ref<Record<number, boolean>>({})
const currentAnswer = ref('')
const currentAnswerArray = ref<string[]>([])
const submitting = ref(false)
const submitted = ref(false)
const remainingSeconds = ref(3600)
const cheatCount = ref(0)
const maxCheat = ref(3)
let timer: any = null

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const answeredCount = computed(() => Object.keys(answeredMap.value).length)

const typeName = (t: string) => {
  const map: any = { single_choice: '单选题', multi_choice: '多选题', true_false: '判断题', fill_blank: '填空题', short_answer: '简答题', file_upload: '文件上传题', coding: '编程题' }
  return map[t] || t
}

const isChoiceType = (t: string) => ['single_choice', 'multi_choice', 'true_false'].includes(t)

const formatTime = (s: number) => {
  const m = Math.floor(s / 60); const sec = s % 60
  return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}

function saveCurrentAnswer() {
  const q = currentQuestion.value
  if (!q || submitted.value) return
  const answer = isChoiceType(q.questionType) && q.questionType === 'multi_choice'
    ? currentAnswerArray.value.sort().join(',') : currentAnswer.value
  answeredMap.value[q.id] = !!answer
  saveAnswer(examId, q.id, answer || '').catch(() => {})
}

function saveCurrentAnswerMulti() { saveCurrentAnswer() }

function handleFileChange(file: any) { currentAnswer.value = file.name; saveCurrentAnswer() }

watch(currentIndex, () => {
  const q = questions.value[currentIndex.value]
  if (!q) { currentAnswer.value = ''; currentAnswerArray.value = []; return }
  if (q.questionType === 'multi_choice') {
    currentAnswerArray.value = (q._savedAnswer || '') ? q._savedAnswer.split(',') : []
    currentAnswer.value = ''
  } else {
    currentAnswer.value = q._savedAnswer || ''
    currentAnswerArray.value = []
  }
})

async function handleSubmit() {
  try {
    await ElMessageBox.confirm('确定要提交试卷吗？提交后将无法修改。', '确认交卷', {
      type: 'warning', confirmButtonText: '确定交卷', cancelButtonText: '继续答题'
    })
  } catch { return }

  submitting.value = true
  try {
    const res = await submitExam(examId)
    submitted.value = true
    clearInterval(timer)
    ElMessageBox.alert(
      `客观题得分：${res.data.objectiveScore} 分<br/>${res.data.isPassed ? '通过' : '未通过及格线'}`,
      '交卷成功',
      { dangerouslyUseHTMLString: true, confirmButtonText: '返回列表', type: res.data.isPassed ? 'success' : 'warning' }
    ).then(() => router.push('/student/exam'))
  } finally { submitting.value = false }
}

function handleVisibilityChange() {
  if (document.hidden && !submitted.value) {
    cheatCount.value++
    recordCheat(examId).then(res => {
      if (res.data >= maxCheat.value) {
        ElMessage.error('切屏次数已达上限，系统将强制交卷。')
        handleSubmit()
      } else {
        ElMessage.warning(`切屏警告：${res.data}/${maxCheat.value} 次，超过将强制交卷。`)
      }
    })
  }
}

onMounted(async () => {
  try {
    const res = await startExam(examId)
    questions.value = (res.data || []).map((q: any) => ({ ...q, _savedAnswer: '' }))
    timer = setInterval(() => { if (remainingSeconds.value > 0) remainingSeconds.value--; else handleSubmit() }, 1000)
    document.addEventListener('visibilitychange', handleVisibilityChange)
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
    router.push('/student/exam')
  }
})

onUnmounted(() => { clearInterval(timer); document.removeEventListener('visibilitychange', handleVisibilityChange) })
</script>

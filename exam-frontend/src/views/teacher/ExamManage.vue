<template>
  <div class="page-container">
    <div class="page-header">
      <h2>考试管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">创建考试</el-button>
    </div>

    <div class="search-bar" style="margin-bottom:16px;display:flex;gap:10px">
      <el-input v-model="searchKeyword" placeholder="搜索考试名称" clearable style="width:220px" @clear="fetchData" />
      <el-select v-model="searchStatus" placeholder="考试状态" clearable style="width:140px" @change="fetchData">
        <el-option label="未开始" value="NOT_START" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已结束" value="FINISHED" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="examName" label="考试名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="subjectName" label="科目" width="120" />
      <el-table-column prop="startTime" label="开始时间" width="160" />
      <el-table-column prop="endTime" label="结束时间" width="160" />
      <el-table-column prop="duration" label="时长(分)" width="80" />
      <el-table-column prop="totalScore" label="总分" width="70" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="questionCount" label="题目数" width="80" />
      <el-table-column prop="studentCount" label="考生数" width="80" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button type="success" link size="small" @click="router.push('/teacher/statistics?examId='+row.id)">统计</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference><el-button type="danger" link size="small">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top:16px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试' : '创建考试'" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考试名称" prop="examName">
              <el-input v-model="form.examName" placeholder="请输入考试名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择" style="width:100%">
                <el-option v-for="s in subjects" :key="s.id" :label="s.subjectName" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="考试描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" value-format="YYYY-MM-DD HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="时长(分)" prop="duration"><el-input-number v-model="form.duration" :min="1" /></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总分" prop="totalScore"><el-input-number v-model="form.totalScore" :min="1" /></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="及格分" prop="passScore"><el-input-number v-model="form.passScore" :min="1" /></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="切屏限制"><el-input-number v-model="form.maxCheatCount" :min="1" :max="10" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="随机排列"><el-switch v-model="form.isRandomOrder" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许重考"><el-switch v-model="form.allowRetry" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">选题设置</el-divider>
        <el-transfer v-model="form.questionIds" :data="questionOptions" :titles="['题库', '已选题']"
          filterable filter-placeholder="搜索题目" style="width:100%" />

        <el-divider content-position="left">考生设置</el-divider>
        <el-transfer v-model="form.studentIds" :data="studentOptions" :titles="['可选学生', '已选考生']"
          filterable filter-placeholder="搜索学生" style="width:100%" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { pageExams, getExam, createExam, updateExam, deleteExam, getStudents } from '@/api/exam'
import { pageQuestions } from '@/api/question'
import { listSubjects } from '@/api/subject'

const router = useRouter()
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const searchStatus = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const submitLoading = ref(false)
const subjects = ref<any[]>([])
const questionOptions = ref<any[]>([])
const studentOptions = ref<any[]>([])

const form = reactive({
  examName: '', subjectId: null as number | null, description: '',
  startTime: '', endTime: '', duration: 60, totalScore: 100, passScore: 60,
  maxCheatCount: 3, isRandomOrder: 1, allowRetry: 0,
  questionIds: [] as number[], studentIds: [] as number[]
})

const rules = {
  examName: [{ required: true, message: '请输入考试名称' }],
  subjectId: [{ required: true, message: '请选择科目' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }],
  duration: [{ required: true, message: '请输入时长' }],
  totalScore: [{ required: true, message: '请输入总分' }],
  passScore: [{ required: true, message: '请输入及格分' }]
}

function statusTag(s: string) { return { NOT_START: 'info', IN_PROGRESS: 'warning', FINISHED: 'success' }[s] || 'info' }
function statusName(s: string) { return { NOT_START: '未开始', IN_PROGRESS: '进行中', FINISHED: '已结束' }[s] || s }

async function fetchData() {
  loading.value = true
  try {
    const res = await pageExams({ page: pageNum.value, size: pageSize.value, keyword: searchKeyword.value || undefined, status: searchStatus.value || undefined })
    tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

async function loadOptions() {
  const [subRes, qRes, sRes] = await Promise.all([
    listSubjects(), pageQuestions({ page: 1, size: 500 }), getStudents()
  ])
  subjects.value = subRes.data || []
  questionOptions.value = (qRes.data?.records || []).map((q: any) => ({ key: q.id, label: `[${typeName(q.questionType)}] ${q.title?.substring(0, 60)}` }))
  studentOptions.value = (sRes.data || []).map((s: any) => ({ key: s.id, label: `${s.realName}(${s.username})` }))
}

function typeName(t: string) { const map: any = { single_choice: '单选', multi_choice: '多选', true_false: '判断', fill_blank: '填空', short_answer: '简答', file_upload: '文件', coding: '编程' }; return map[t] || t }

function resetForm() {
  form.examName = ''; form.subjectId = null; form.description = ''; form.startTime = ''; form.endTime = ''
  form.duration = 60; form.totalScore = 100; form.passScore = 60; form.maxCheatCount = 3
  form.isRandomOrder = 1; form.allowRetry = 0; form.questionIds = []; form.studentIds = []
}

async function openCreateDialog() {
  isEdit.value = false; editingId.value = null; resetForm(); dialogVisible.value = true
}

async function openEditDialog(row: any) {
  isEdit.value = true; editingId.value = row.id
  const res = await getExam(row.id)
  const d = res.data
  form.examName = d.examName; form.subjectId = d.subjectId; form.description = d.description || ''
  form.startTime = d.startTime; form.endTime = d.endTime; form.duration = d.duration
  form.totalScore = d.totalScore; form.passScore = d.passScore; form.maxCheatCount = d.maxCheatCount
  form.isRandomOrder = d.isRandomOrder; form.allowRetry = d.allowRetry
  form.questionIds = d.questionIds || []; form.studentIds = d.studentIds || []
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = { ...form }
    if (isEdit.value && editingId.value) {
      await updateExam(editingId.value, payload)
    } else { await createExam(payload) }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(id: number) { await deleteExam(id); ElMessage.success('删除成功'); fetchData() }

onMounted(async () => { await loadOptions(); fetchData() })
</script>

<style scoped>
.page-container { padding: 0 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2>题库管理</h2>
      <div class="header-actions">
        <el-button type="success" :icon="Upload" @click="showImportDialog = true">批量导入</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增题目</el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索题目内容" clearable style="width:240px" @clear="fetchData" />
      <el-select v-model="searchSubjectId" placeholder="科目" clearable style="width:160px" @change="fetchData">
        <el-option v-for="s in subjects" :key="s.id" :label="s.subjectName" :value="s.id" />
      </el-select>
      <el-select v-model="searchType" placeholder="题型" clearable style="width:130px" @change="fetchData">
        <el-option label="单选题" value="single_choice" />
        <el-option label="多选题" value="multi_choice" />
        <el-option label="判断题" value="true_false" />
        <el-option label="填空题" value="fill_blank" />
        <el-option label="简答题" value="short_answer" />
        <el-option label="文件上传题" value="file_upload" />
        <el-option label="编程题" value="coding" />
      </el-select>
      <el-select v-model="searchDifficulty" placeholder="难度" clearable style="width:100px" @change="fetchData">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%;margin-top:16px" max-height="600">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="subjectName" label="科目" width="120" />
      <el-table-column prop="questionType" label="题型" width="100">
        <template #default="{ row }">{{ typeName(row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="题目内容" min-width="280" show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffName(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分值" width="70" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link size="small" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top:16px;display:flex;justify-content:flex-end">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @size-change="fetchData" @current-change="fetchData" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑题目' : '新增题目'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="科目" prop="subjectId">
          <el-select v-model="form.subjectId" placeholder="请选择科目" style="width:100%">
            <el-option v-for="s in subjects" :key="s.id" :label="s.subjectName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-select v-model="form.questionType" placeholder="请选择题型" style="width:100%" @change="onTypeChange">
            <el-option label="单选题" value="single_choice" />
            <el-option label="多选题" value="multi_choice" />
            <el-option label="判断题" value="true_false" />
            <el-option label="填空题" value="fill_blank" />
            <el-option label="简答题" value="short_answer" />
            <el-option label="文件上传题" value="file_upload" />
            <el-option label="编程题" value="coding" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="form.difficulty">
            <el-radio :value="1">简单</el-radio>
            <el-radio :value="2">中等</el-radio>
            <el-radio :value="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>

        <!-- 选择题选项 -->
        <template v-if="isChoiceType">
          <el-divider content-position="left">选项设置</el-divider>
          <div v-for="(opt, idx) in form.options" :key="idx" style="display:flex;align-items:center;gap:8px;margin-bottom:10px">
            <el-tag size="small">{{ optionLabel(idx) }}</el-tag>
            <el-input v-model="opt.optionContent" :placeholder="'请输入选项' + optionLabel(idx) + '内容'" style="flex:1" />
            <el-checkbox v-model="opt.isCorrect" :true-value="1" :false-value="0"
              :label="form.questionType === 'single_choice' ? '正确答案' : ''" />
            <el-button v-if="form.options.length > 2" type="danger" :icon="Delete" circle size="small" @click="form.options.splice(idx,1)" />
          </div>
          <el-button type="primary" link :icon="Plus" @click="form.options.push({ optionLabel: optionLabel(form.options.length), optionContent: '', isCorrect: 0, sortOrder: form.options.length })">
            添加选项
          </el-button>
        </template>

        <!-- 解析 -->
        <el-form-item label="解析" style="margin-top:12px">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="题目解析（选填）" />
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoints" placeholder="相关知识标签，逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="showImportDialog" title="批量导入题目" width="480px" destroy-on-close>
      <div style="margin-bottom:16px">
        <el-button type="primary" link :icon="Download" @click="downloadTemplate">下载导入模板</el-button>
      </div>
      <div style="margin-bottom:12px">
        <span style="color:#606266">导入到科目：</span>
        <el-select v-model="importSubjectId" placeholder="请选择科目" style="width:200px">
          <el-option v-for="s in subjects" :key="s.id" :label="s.subjectName" :value="s.id" />
        </el-select>
      </div>
      <el-upload ref="uploadRef" drag :auto-upload="false" :on-change="handleFileChange" accept=".xlsx,.xls" :limit="1">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <div v-if="importResult" style="margin-top:12px;padding:12px;background:#f5f7fa;border-radius:6px">
        <p>导入结果：成功 <b style="color:#67c23a">{{ importResult.success }}</b> 条，失败 <b style="color:#f56c6c">{{ importResult.fail }}</b> 条</p>
      </div>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import { pageQuestions, createQuestion, updateQuestion, deleteQuestion, importQuestions, downloadQuestionTemplate } from '@/api/question'
import { listSubjects } from '@/api/subject'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const subjects = ref<any[]>([])
const searchKeyword = ref('')
const searchSubjectId = ref<number | null>(null)
const searchType = ref('')
const searchDifficulty = ref<number | null>(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({
  subjectId: null as number | null, questionType: 'single_choice', title: '',
  difficulty: 1, score: 5, analysis: '', knowledgePoints: '',
  options: [{ optionLabel: 'A', optionContent: '', isCorrect: 0, sortOrder: 0 },
            { optionLabel: 'B', optionContent: '', isCorrect: 0, sortOrder: 1 },
            { optionLabel: 'C', optionContent: '', isCorrect: 0, sortOrder: 2 },
            { optionLabel: 'D', optionContent: '', isCorrect: 0, sortOrder: 3 }] as any[]
})
const rules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  title: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }]
}

const isChoiceType = computed(() => ['single_choice', 'multi_choice', 'true_false'].includes(form.questionType))
const showImportDialog = ref(false)
const uploadRef = ref()
const importFile = ref<File | null>(null)
const importSubjectId = ref<number | null>(null)
const importLoading = ref(false)
const importResult = ref<any>(null)

function typeName(t: string) {
  const map: any = { single_choice: '单选题', multi_choice: '多选题', true_false: '判断题', fill_blank: '填空题', short_answer: '简答题', file_upload: '文件上传题', coding: '编程题' }
  return map[t] || t
}

function diffName(d: number) {
  return { 1: '简单', 2: '中等', 3: '困难' }[d] || ''
}

function diffTag(d: number) {
  return { 1: 'success', 2: 'warning', 3: 'danger' }[d] || 'info'
}

function optionLabel(idx: number) {
  return String.fromCharCode(65 + idx)
}

async function fetchData() {
  loading.value = true
  try {
    const res = await pageQuestions({
      page: pageNum.value, size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      subjectId: searchSubjectId.value || undefined,
      questionType: searchType.value || undefined,
      difficulty: searchDifficulty.value || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function onTypeChange() {
  if (isChoiceType.value) {
    if (form.questionType === 'true_false') {
      form.options = [
        { optionLabel: 'A', optionContent: '正确', isCorrect: 0, sortOrder: 0 },
        { optionLabel: 'B', optionContent: '错误', isCorrect: 0, sortOrder: 1 }
      ]
    } else if (form.options.length === 0) {
      form.options = [
        { optionLabel: 'A', optionContent: '', isCorrect: 0, sortOrder: 0 },
        { optionLabel: 'B', optionContent: '', isCorrect: 0, sortOrder: 1 },
        { optionLabel: 'C', optionContent: '', isCorrect: 0, sortOrder: 2 },
        { optionLabel: 'D', optionContent: '', isCorrect: 0, sortOrder: 3 }
      ]
    }
  }
}

function resetForm() {
  form.subjectId = null; form.questionType = 'single_choice'; form.title = ''
  form.difficulty = 1; form.score = 5; form.analysis = ''; form.knowledgePoints = ''
  form.options = [
    { optionLabel: 'A', optionContent: '', isCorrect: 0, sortOrder: 0 },
    { optionLabel: 'B', optionContent: '', isCorrect: 0, sortOrder: 1 },
    { optionLabel: 'C', optionContent: '', isCorrect: 0, sortOrder: 2 },
    { optionLabel: 'D', optionContent: '', isCorrect: 0, sortOrder: 3 }
  ]
}

function openCreateDialog() {
  isEdit.value = false; editingId.value = null; resetForm(); dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true; editingId.value = row.id
  form.subjectId = row.subjectId; form.questionType = row.questionType; form.title = row.title
  form.difficulty = row.difficulty; form.score = row.score; form.analysis = row.analysis || ''
  form.knowledgePoints = row.knowledgePoints || ''
  form.options = row.options ? row.options.map((o: any) => ({ ...o })) : []
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload: any = { ...form }
    if (!isChoiceType.value) { payload.options = null }
    if (isEdit.value && editingId.value) {
      await updateQuestion(editingId.value, payload)
    } else {
      await createQuestion(payload)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(id: number) {
  await deleteQuestion(id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleFileChange(file: any) { importFile.value = file.raw; importResult.value = null }

async function handleImport() {
  if (!importFile.value || !importSubjectId.value) { ElMessage.warning('请选择文件和科目'); return }
  importLoading.value = true
  try {
    const res = await importQuestions(importFile.value, importSubjectId.value)
    importResult.value = res.data
    ElMessage.success('导入完成')
    fetchData()
  } finally { importLoading.value = false }
}

async function downloadTemplate() {
  const res: any = await downloadQuestionTemplate()
  const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = '题目导入模板.xlsx'
  a.click(); URL.revokeObjectURL(url)
}

onMounted(async () => {
  const subRes = await listSubjects()
  subjects.value = subRes.data || []
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 0 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.header-actions { display: flex; gap: 8px; }
.search-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
</style>

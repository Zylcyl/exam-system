<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button type="success" :icon="Upload" @click="showImportDialog = true">批量导入学生</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增用户</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/姓名/手机号" clearable style="width:240px"
        @clear="fetchData" @keyup.enter="fetchData" />
      <el-select v-model="searchRoleId" placeholder="角色筛选" clearable style="width:140px" @change="fetchData">
        <el-option label="管理员" :value="1" />
        <el-option label="教师" :value="2" />
        <el-option label="学生" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="roleId" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.roleId)" size="small">{{ roleName(row.roleId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? null : 'password'">
          <el-input v-model="form.password" type="password" show-password
            :placeholder="isEdit ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width:100%">
            <el-option label="管理员" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="学生" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="showImportDialog" title="批量导入学生" width="480px" destroy-on-close>
      <div style="margin-bottom:16px">
        <el-button type="primary" link :icon="Download" @click="downloadTemplate">下载导入模板</el-button>
      </div>
      <el-upload ref="uploadRef" drag :auto-upload="false" :on-change="handleFileChange"
        accept=".xlsx,.xls" :limit="1">
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将 Excel 文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 .xlsx .xls 格式，请先下载模板按格式填写</div>
        </template>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload, Download, UploadFilled } from '@element-plus/icons-vue'
import { pageUsers, createUser, updateUser, deleteUser, importStudents, downloadStudentTemplate } from '@/api/user'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const searchRoleId = ref<number | null>(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const form = reactive({ username: '', realName: '', password: '', roleId: null as number | null, email: '', phone: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const showImportDialog = ref(false)
const uploadRef = ref()
const importFile = ref<File | null>(null)
const importLoading = ref(false)
const importResult = ref<any>(null)

function roleName(roleId: number) {
  return { 1: '管理员', 2: '教师', 3: '学生' }[roleId] || '未知'
}

function roleTagType(roleId: number) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[roleId] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await pageUsers({
      page: pageNum.value, size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      roleId: searchRoleId.value || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openCreateDialog() {
  isEdit.value = false
  form.username = ''; form.realName = ''; form.password = ''; form.roleId = null; form.email = ''; form.phone = ''
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  form.username = row.username; form.realName = row.realName; form.password = ''
  form.roleId = row.roleId; form.email = row.email || ''; form.phone = row.phone || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      const editingRow = tableData.value.find(r => r.username === form.username)
      if (editingRow) await updateUser(editingRow.id, { ...form })
    } else {
      await createUser({ ...form })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(id: number) {
  await deleteUser(id)
  ElMessage.success('删除成功')
  fetchData()
}

function handleFileChange(file: any) {
  importFile.value = file.raw
  importResult.value = null
}

async function handleImport() {
  if (!importFile.value) { ElMessage.warning('请选择文件'); return }
  importLoading.value = true
  try {
    const res = await importStudents(importFile.value)
    importResult.value = res.data
    ElMessage.success('导入完成')
    fetchData()
  } finally { importLoading.value = false }
}

async function downloadTemplate() {
  const res: any = await downloadStudentTemplate()
  const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = '学生导入模板.xlsx'
  a.click(); URL.revokeObjectURL(url)
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.header-actions { display: flex; gap: 8px; }
.search-bar { display: flex; gap: 10px; align-items: center; }
</style>

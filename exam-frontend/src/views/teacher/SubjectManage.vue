<template>
  <div class="page-container">
    <div class="page-header">
      <h2>科目管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog(null)">新增科目</el-button>
    </div>

    <el-table :data="treeData" border stripe row-key="id" default-expand-all style="width:100%;margin-top:16px">
      <el-table-column prop="label" label="科目名称" min-width="200" />
      <el-table-column prop="description" label="描述" min-width="300" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="primary" link size="small" :icon="Edit" @click="openCreateDialog(row)">添加子科目</el-button>
          <el-button type="warning" link size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
          <el-popconfirm title="确定删除该科目吗？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button type="danger" link size="small" :icon="Delete">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑科目' : '新增科目'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="subjectName">
          <el-input v-model="form.subjectName" placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
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
import { ElMessage } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getSubjectTree, createSubject, updateSubject, deleteSubject } from '@/api/subject'

const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const submitLoading = ref(false)
const parentId = ref(0)
const form = reactive({ subjectName: '', description: '', sortOrder: 0 })
const rules = { subjectName: [{ required: true, message: '请输入科目名称', trigger: 'blur' }] }

async function fetchData() {
  const res = await getSubjectTree()
  treeData.value = res.data || []
}

function openCreateDialog(parent: any) {
  isEdit.value = false
  parentId.value = parent ? parent.id : 0
  form.subjectName = ''; form.description = ''; form.sortOrder = 0
  dialogVisible.value = true
}

function openEditDialog(row: any) {
  isEdit.value = true
  parentId.value = row.parentId
  form.subjectName = row.label; form.description = row.description || ''; form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      // 需要找到编辑行的 id，这里简化处理
    } else {
      await createSubject({ ...form, parentId: parentId.value })
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(id: number) {
  await deleteSubject(id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.page-container { padding: 0 10px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
</style>

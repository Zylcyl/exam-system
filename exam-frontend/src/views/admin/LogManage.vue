<template>
  <div class="page-container">
    <h2>操作日志</h2>
    <div style="margin:16px 0;display:flex;gap:10px">
      <el-input v-model="searchKeyword" placeholder="搜索操作/用户名" clearable style="width:260px" @keyup.enter="fetchData" />
      <el-button type="primary" :icon="Search" @click="fetchData">搜索</el-button>
    </div>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="操作用户" width="100" />
      <el-table-column prop="operation" label="操作" width="120" />
      <el-table-column prop="method" label="请求方法" width="180" />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="status" label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="executeTime" label="耗时(ms)" width="90" />
      <el-table-column prop="createTime" label="操作时间" width="170" />
      <el-table-column label="参数" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ row.params || '' }}</template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end"
      @size-change="fetchData" @current-change="fetchData" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { pageLogs } from '@/api/log'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await pageLogs({ page: pageNum.value, size: pageSize.value, keyword: searchKeyword.value || undefined })
    tableData.value = res.data.records; total.value = res.data.total
  } finally { loading.value = false }
}

onMounted(fetchData)
</script>

<style scoped>.page-container { padding: 0 10px; }</style>

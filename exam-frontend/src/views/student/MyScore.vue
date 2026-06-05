<template>
  <div class="page-container">
    <h2>我的成绩</h2>
    <el-table :data="scores" border stripe style="width:100%;margin-top:16px">
      <el-table-column prop="examName" label="考试名称" min-width="180" />
      <el-table-column prop="totalScore" label="卷面总分" width="100" />
      <el-table-column label="我的成绩" width="120">
        <template #default="{ row }">
          <b :style="{color: row.myScore != null ? (row.isPassed ? '#67c23a' : '#f56c6c') : '#909399'}">
            {{ row.myScore ?? '待批阅' }}
          </b>
        </template>
      </el-table-column>
      <el-table-column prop="passScore" label="及格分" width="80" />
      <el-table-column label="是否通过" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.myScore != null" :type="row.isPassed ? 'success' : 'danger'">
            {{ row.isPassed ? '通过' : '未通过' }}
          </el-tag>
          <el-tag v-else type="info">待批阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="交卷时间" width="170" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyScores } from '@/api/exam'

const scores = ref<any[]>([])

onMounted(async () => { const res = await getMyScores(); scores.value = res.data || [] })
</script>

<style scoped>.page-container { padding: 0 10px; }</style>

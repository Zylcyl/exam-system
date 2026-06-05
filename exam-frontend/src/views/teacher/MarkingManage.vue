<template>
  <div class="page-container">
    <h2>阅卷管理</h2>
    <p style="color:#909399;margin-top:10px">请在"成绩统计"页面选择考试后，点击学生的"阅卷"按钮进行主观题批阅。</p>
    <el-button type="primary" style="margin-top:12px" @click="router.push('/teacher/statistics')">前往成绩统计</el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

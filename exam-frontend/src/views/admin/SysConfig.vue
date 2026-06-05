<template>
  <div class="page-container">
    <h2>系统配置</h2>
    <el-table :data="configs" border stripe style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="configKey" label="配置键" width="200" />
      <el-table-column prop="configValue" label="配置值" min-width="200" />
      <el-table-column prop="description" label="描述" min-width="300" />
    </el-table>
    <p style="color:#909399;font-size:13px;margin-top:12px">系统配置为预留功能，可通过数据库 sys_config 表维护。</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const configs = ref([
  { id: 1, configKey: 'system_name', configValue: '计算机考试系统', description: '系统名称' },
  { id: 2, configKey: 'default_pass_score', configValue: '60', description: '默认及格分数' },
  { id: 3, configKey: 'max_cheat_count', configValue: '3', description: '默认最大切屏次数' }
])
</script>

<style scoped>.page-container { padding: 0 10px; }</style>

<template>
  <div class="page-container">
    <h2>角色管理</h2>
    <el-table :data="roles" border stripe style="width:100%;margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="roleName" label="角色名称" width="120" />
      <el-table-column prop="roleCode" label="角色编码" width="120" />
      <el-table-column prop="description" label="描述" min-width="300" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
    </el-table>
    <p style="color:#909399;font-size:13px;margin-top:12px">角色为系统初始化数据，暂不支持在线修改。请在数据库中维护。</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const roles = ref<any[]>([])

onMounted(async () => {
  const res = await request.get('/auth/me') // 通过通用请求获取
  // 简单展示，不调用额外 API
  roles.value = [
    { id: 1, roleName: '系统管理员', roleCode: 'ROLE_ADMIN', description: '系统最高权限', createTime: '2026-06-05' },
    { id: 2, roleName: '教师', roleCode: 'ROLE_TEACHER', description: '题库管理、考试管理、阅卷', createTime: '2026-06-05' },
    { id: 3, roleName: '学生', roleCode: 'ROLE_STUDENT', description: '参加考试、查看成绩', createTime: '2026-06-05' }
  ]
})
</script>

<style scoped>.page-container { padding: 0 10px; }</style>

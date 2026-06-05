<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="layout-aside">
      <div class="aside-logo" @click="router.push('/dashboard')">
        <el-icon :size="24" color="#e8c74a"><School /></el-icon>
        <span v-show="!isCollapsed" class="aside-logo-text">考试系统</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#1a2d40"
        text-color="#8fa4b8"
        active-text-color="#e8c74a"
        router
      >
        <template v-for="menu in userStore.menus" :key="menu.id">
          <el-menu-item v-if="!menu.children" :index="menu.path">
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.name }}</template>
          </el-menu-item>
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-button :icon="isCollapsed ? Expand : Fold" text @click="isCollapsed = !isCollapsed"
            style="color: #2c3e50" />
          <el-breadcrumb separator="›">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle" style="color:#1a3a5c;font-weight:500">
              {{ currentTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="header-user">
              <el-icon><UserFilled /></el-icon>
              {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              <el-icon style="margin-left:2px"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { School, Expand, Fold, UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title as string)

onMounted(async () => {
  if (!userStore.userInfo) {
    try { await userStore.fetchUserInfo() } catch { userStore.logout(); router.push('/login') }
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout { width: 100%; height: 100vh; }

.layout-aside {
  background-color: #1a2d40;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.25s ease;
}

.aside-logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  gap: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  user-select: none;
}

.aside-logo-text {
  color: #fff;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
  white-space: nowrap;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #d4d8dd;
  padding: 0 20px;
  height: 56px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #2c3e50;
  font-size: 13px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.header-user:hover {
  background: #f0f2f5;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px 24px;
  overflow-y: auto;
}

.el-menu--collapse { width: 64px; }
</style>

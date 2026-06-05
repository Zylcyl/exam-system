import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, getCurrentUserApi } from '@/api/auth'

interface UserInfo {
  userId: number
  username: string
  realName: string
  roleCode: string
  roleName?: string
  menus: any[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const roleCode = computed(() => userInfo.value?.roleCode || '')
  const menus = computed(() => userInfo.value?.menus || [])

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    const res = await getCurrentUserApi()
    userInfo.value = res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return { token, userInfo, roleCode, menus, login, fetchUserInfo, logout }
})

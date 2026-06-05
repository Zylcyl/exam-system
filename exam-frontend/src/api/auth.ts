import request from '@/utils/request'

export function loginApi(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

export function getCurrentUserApi() {
  return request.get('/auth/me')
}

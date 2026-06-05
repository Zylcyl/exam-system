import request from '@/utils/request'

export function pageUsers(params: any) {
  return request.get('/admin/user/page', { params })
}

export function createUser(data: any) {
  return request.post('/admin/user', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/admin/user/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/admin/user/${id}`)
}

export function importStudents(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/user/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadStudentTemplate() {
  return request.get('/admin/user/export-template', { responseType: 'blob' })
}

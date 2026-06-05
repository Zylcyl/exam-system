import request from '@/utils/request'

export function listSubjects() {
  return request.get('/subject/list')
}

export function getSubjectTree() {
  return request.get('/subject/tree')
}

export function createSubject(data: any) {
  return request.post('/subject', data)
}

export function updateSubject(id: number, data: any) {
  return request.put(`/subject/${id}`, data)
}

export function deleteSubject(id: number) {
  return request.delete(`/subject/${id}`)
}

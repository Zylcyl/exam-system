import request from '@/utils/request'

export function pageQuestions(params: any) {
  return request.get('/question/page', { params })
}

export function getQuestion(id: number) {
  return request.get(`/question/${id}`)
}

export function createQuestion(data: any) {
  return request.post('/question', data)
}

export function updateQuestion(id: number, data: any) {
  return request.put(`/question/${id}`, data)
}

export function deleteQuestion(id: number) {
  return request.delete(`/question/${id}`)
}

export function importQuestions(file: File, subjectId: number) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/question/import?subjectId=${subjectId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadQuestionTemplate() {
  return request.get('/question/export-template', { responseType: 'blob' })
}

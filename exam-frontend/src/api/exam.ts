import request from '@/utils/request'

export function pageExams(params: any) {
  return request.get('/exam/page', { params })
}

export function getExam(id: number) {
  return request.get(`/exam/${id}`)
}

export function createExam(data: any) {
  return request.post('/exam', data)
}

export function updateExam(id: number, data: any) {
  return request.put(`/exam/${id}`, data)
}

export function deleteExam(id: number) {
  return request.delete(`/exam/${id}`)
}

export function getStudents() {
  return request.get('/exam/students')
}

// 学生端
export function getMyExams() {
  return request.get('/exam/my-exams')
}

export function startExam(examId: number) {
  return request.get(`/exam/${examId}/start`)
}

export function saveAnswer(examId: number, questionId: number, answerContent: string) {
  return request.post(`/exam/${examId}/answer`, null, {
    params: { questionId, answerContent }
  })
}

export function recordCheat(examId: number) {
  return request.post(`/exam/${examId}/cheat`)
}

export function submitExam(examId: number) {
  return request.post(`/exam/${examId}/submit`)
}

// 成绩
export function pageScores(params: any) {
  return request.get('/score/page', { params })
}

export function getAnswerDetail(examId: number, studentId: number) {
  return request.get('/score/detail', { params: { examId, studentId } })
}

export function markAnswer(data: any) {
  return request.put('/score/mark', data)
}

export function getExamStats(examId: number) {
  return request.get('/score/stats', { params: { examId } })
}

export function getMyScores() {
  return request.get('/score/my-scores')
}

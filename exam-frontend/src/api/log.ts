import request from '@/utils/request'

export function pageLogs(params: any) {
  return request.get('/admin/log/page', { params })
}

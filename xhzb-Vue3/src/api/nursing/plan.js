import request from '@/utils/request'

// 查询护理计划列表
export function listPlan(query) {
  return request({
    url: '/nursing/plan/list',
    method: 'get',
    params: query
  })
}

// 查询全部护理计划（不分页，供下拉选择使用）
export function listAllPlan(query) {
  return request({
    url: '/nursing/plan/all',
    method: 'get',
    params: query
  })
}

// 查询护理计划详细
export function getPlan(id) {
  return request({
    url: '/nursing/plan/' + id,
    method: 'get'
  })
}

// 新增护理计划
export function addPlan(data) {
  return request({
    url: '/nursing/plan',
    method: 'post',
    data: data
  })
}

// 修改护理计划
export function updatePlan(data) {
  return request({
    url: '/nursing/plan',
    method: 'put',
    data: data
  })
}

// 删除护理计划
export function delPlan(id) {
  return request({
    url: '/nursing/plan/' + id,
    method: 'delete'
  })
}

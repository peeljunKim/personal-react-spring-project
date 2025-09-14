import axios from 'axios'

export const API_SERVER_HOST = 'http://localhost:8080'

const prefix = `${API_SERVER_HOST}/api/todo`

// todo detail api
// async 함수의 리턴은 무조건 Promise, 실제로 반환 값은 Promise<Todo>
export const getOne = async (tno: number) => {
  const res = await axios.get(`${prefix}/${tno}`)
  return res.data
}

// 페이징 목록 api
export const getList = async (pageParam: PageParam) => {
  const res = await axios.get(`${prefix}/list`, { params: pageParam })
  return res.data
}

// todo 등록 api
export const postAdd = async (todoObj: TodoAdd) => {
  const res = await axios.post(`${prefix}/`, todoObj)
  return res.data
}

// todo 삭제 api
export const deleteOne = async (tno: number) => {
  const res = await axios.delete(`${prefix}/${tno}`)
  return res.data
}

// todo 수정 api
export const putOne = async (todo: TodoModify) => {
  const res = await axios.put(`${prefix}/${todo.tno}`, todo)
  return res.data
}

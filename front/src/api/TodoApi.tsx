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

import axios from 'axios'

export const API_SERVER_HOST = 'http://localhost:8080'

const prefix = `${API_SERVER_HOST}/api/todo`

// async 함수의 리턴은 무조건 Promise, 실제로 반환 값은 Promise<Todo>
export const getOne = async (tno: number) => {
  const res = await axios.get(`${prefix}/${tno}`)
  return res.data
}

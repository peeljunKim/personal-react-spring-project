import axios, {
  AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { getCookie, setCookie } from './CookieUtil'

const jwtAxios = axios.create()

// HTTP 요청이 서버로 보내지기 전에 실행 - Access Token 전달
const beforeReq = (config: InternalAxiosRequestConfig) => {
  console.log('before request')

  const memberInfo = getCookie('member')

  if (!memberInfo) {
    console.log('Member Not Found')
    return Promise.reject(new Error('REQUIRE_LOGIN'))
  }

  const { accessToken } = memberInfo // Authorization 헤더 처리

  config.headers.Authorization = `Bearer ${accessToken}`

  return config
}

// 요청을 보내는 과정에서 오류가 발생했을 때 실행
const requestFail = (err: AxiosError) => {
  console.log('request error')
  return Promise.reject(err)
}

// 서버로 성공적인 응답을 받았을 때 실행 ++ Access Token이 만료 시 Access, refresh 토큰 갱신
const beforeRes = async (res: AxiosResponse): Promise<AxiosResponse> => {
  console.log('before return response')

  // 응답 전 처리 (필요한 경우 데이터 수정)
  const data = res.data

  if (data && data.error === 'ERROR_ACCESS_TOKEN') {
    const memberCookieValue = getCookie('member')
    const result = await refreshJWT(
      memberCookieValue.accessToken,
      memberCookieValue.refreshToken
    )

    console.log('refreshJWT RESULT', result)

    memberCookieValue.accessToken = result.accessToken
    memberCookieValue.refreshToken = result.refreshToken

    setCookie('member', JSON.stringify(memberCookieValue), 1)

    //원래의 호출
    const originalRequest = res.config
    originalRequest.headers.Authorization = `Bearer ${result.accessToken}`

    return await axios(originalRequest)
  }

  return res
}

// 오류 응답을 받았을 때 실행 - 200 상태코드 나오게 설계되어 있음
const responseFail = async (err: AxiosError) => {
  console.log('response fail error')
  console.log(err)
  return Promise.reject(err)
}

// 서버에 토큰 refresh 요청
const refreshJWT = async (accessToken: string, refreshToken: string) => {
  const header = { headers: { Authorization: `Bearer ${accessToken}` } }

  const res = await axios.get(
    `http://localhost:8080/api/member/refresh?refreshToken=${refreshToken}`,
    header
  )
  console.log('----------------------')
  console.log(res.data)
  return res.data
}

jwtAxios.interceptors.request.use(beforeReq, requestFail)
jwtAxios.interceptors.response.use(beforeRes, responseFail)

export default jwtAxios

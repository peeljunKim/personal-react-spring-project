import axios from 'axios'

const rest_api_key = import.meta.env.VITE_REST_API_KEY
const redirect_uri = import.meta.env.VITE_REDIRECT_URI
const auth_code_path = `https://kauth.kakao.com/oauth/authorize`

//엑세스 토큰 얻기
const access_token_url = `https://kauth.kakao.com/oauth/token`
const client_secret = import.meta.env.VITE_CLIENT_SECRET

export const getKakaoLoginLink = () => {
  const kakaoURL = `${auth_code_path}?client_id=${rest_api_key}&redirect_uri=${redirect_uri}&response_type=code`

  return kakaoURL
}

export const getAccessToken = async (authCode: string) => {
  const header = {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  }

  const params = {
    grant_type: 'authorization_code',
    client_id: rest_api_key,
    redirect_uri: redirect_uri,
    code: authCode,
    client_secret: client_secret,
  }

  const res = await axios.post(access_token_url, params, header)
  const accessToken = res.data.access_token

  return accessToken
}

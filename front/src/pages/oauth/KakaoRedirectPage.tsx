import { useEffect } from 'react'
import { useSearchParams } from 'react-router'
import { getAccessToken } from '../../api/KakaoApi'

const KakaoRedirectPage = () => {
  const [searchParams] = useSearchParams()
  const authCode = searchParams.get('code')

  useEffect(() => {
    if (authCode) {
      getAccessToken(authCode).then((data) => {
        // console.log(data)
      })
    }
  }, [authCode])

  return (
    <div>
      <div>Kakao Login Redirect</div>
      <div>{authCode}</div>
    </div>
  )
}

export default KakaoRedirectPage

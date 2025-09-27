import { useEffect } from 'react'
import { Navigate, useNavigate, useSearchParams } from 'react-router'
import { getAccessToken, getMemberWithAccessToken } from '../../api/KakaoApi'
import type { AppDispatch } from '../../store'
import { useDispatch } from 'react-redux'
import { save } from '../../slices/LoginSlice'

const KakaoRedirectPage = () => {
  const [searchParams] = useSearchParams()
  const authCode = searchParams.get('code')
  const dispatch = useDispatch<AppDispatch>()
  const navigate = useNavigate()

  useEffect(() => {
    if (authCode) {
      getAccessToken(authCode).then((accessToken) => {
        console.log(accessToken)

        if (accessToken) {
          getMemberWithAccessToken(accessToken).then((memberInfo) => {
            console.log('-------------------')
            console.log('memberInfo = ' + memberInfo)
            console.log('social  =  ' + memberInfo.social)
            dispatch(save(memberInfo))

            if (memberInfo.social) {
              navigate('/member/modify')
            }
          })
        }
      })
    }
  }, [authCode])

  return (
    <>
      <Navigate to={'/'}></Navigate>
    </>
  )
}

export default KakaoRedirectPage

import { useDispatch, useSelector } from 'react-redux'
import type { AppDispatch, RootState } from '../store'
import { Navigate, useNavigate } from 'react-router'
import { loginPostAsync, logout, save } from '../slices/LoginSlice'
import { useEffect } from 'react'
import { getCookie } from '../util/CookieUtil'
import useZustandMember from '../store/useZustandMember'

const useCustomLogin = () => {
  const { member, status, login, logout, save } = useZustandMember()
  //로그인 상태 객체
  const loginState = member

  //로그인 여부
  const loginStatus = status

  const doLogin = async (email: string, pw: string) => {
    login(email, pw)
  }

  const doLogout = () => {
    logout()
  }

  useEffect(() => {
    if (!loginStatus) {
      const member = getCookie('member')
      console.log('member from cookie ')
      console.log(member)

      if (member) {
        save(member)
      }
    }
  }, [])

  const navigate = useNavigate()
  // const dispatch = useDispatch<AppDispatch>()

  // redux store의 로그인 상태 객체
  // const loginState = useSelector((state: RootState) => state.loginSlice)

  // 현재 로그인 상태(fulfilled, pending, rejected)
  // const loginStatus = loginState.status

  // 로그인 처리 비동기 함수
  // const doLogin = async (email: string, pw: string) => {
  //   dispatch(loginPostAsync({ email, pw }))
  // }

  // 로그 아웃 - react redux store에 로그인 상태 null로 변경
  // const doLogout = () => {
  //   dispatch(logout(null))
  // }

  // 로그인 페이지로 이동 - 함수 호출 방법
  const moveToLogin = () => {
    navigate('/member/login')
  }

  //로그인 페이지로 이동 - 컴포넌트 반환 방법
  const moveToLoginReturn = () => {
    return <Navigate replace to="/member/login" />
  }

  const moveToPath = (path: string) => {
    navigate({ pathname: path }, { replace: true })
  }

  // 페이지 처음 로딩 시 쿠키 확인
  // useEffect(() => {
  //   if (!loginStatus) {
  //     const member = getCookie('member')
  //     console.log('member from cookie = ', member)

  //     if (member) {
  //       dispatch(save(member))
  //     }
  //   }
  // }, [])

  return {
    loginState,
    loginStatus,
    doLogin,
    doLogout,
    moveToLogin,
    moveToLoginReturn,
    moveToPath,
  }
}

export default useCustomLogin

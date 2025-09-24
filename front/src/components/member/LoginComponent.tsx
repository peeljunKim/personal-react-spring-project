import { useActionState, useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import type { AppDispatch, RootState } from '../../store'
import { loginPostAsync } from '../../slices/LoginSlice'
import ResultModal from '../common/ResultModal'
import { useNavigate } from 'react-router'
import useCustomLogin from '../../hooks/useCustomLogin'
import KakaoLoginComponent from '../oauth/KakaoLoginComponent'

interface LoginResult {
  email: string
  signed: boolean
}

const initState: LoginResult = {
  email: '',
  signed: false,
}

// async function loginAction(state: LoginResult, formData: FormData) {
//   await new Promise((resolve) => setTimeout(resolve, 2000))

//   const email = formData.get('email') as string
//   const pw = formData.get('pw') as string

//   console.log('email ', email, 'pw ', pw)

//   return { email: email, signed: true }
// }

function LoginComponent() {
  const { loginStatus, doLogin, moveToPath } = useCustomLogin()

  const [email, setEmail] = useState('')
  const [pw, setPw] = useState('')

  const handleLogin = () => {
    doLogin(email, pw)
  }

  const closeModal = () => {
    moveToPath('/')
  }

  // const [state, action, isPending] = useActionState(loginAction, initState)
  // const dispatch = useDispatch()

  // useEffect(() => {
  //   if (state.signed) {
  //     dispatch(login(state))
  //   }
  // }, [state.signed])

  return (
    <div className="border-2 border-sky-200 mt-10 m-2 p-4">
      {loginStatus === 'pending' && (
        <div className="bg-amber-300">로그인 중</div>
      )}

      {loginStatus === 'fulfilled' && (
        <ResultModal
          title="Login 결과"
          content="로그인 되었습니다."
          callbackFn={closeModal}
        />
      )}

      {loginStatus === 'rejected' && (
        <div className="bg-red-300">로그인 실패</div>
      )}

      <div className="flex justify-center">
        <div className="text-4xl m-4 p-4 font-extrabold text-blue-500">
          Login
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-full p-6 text-left font-bold">Email</div>
          <input
            className="w-full p-6 rounded-r border border-solid border-neutral-500 shadow-md"
            name="email"
            type={'text'}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-full p-6 text-left font-bold">Password</div>
          <input
            className="w-full p-6 rounded-r border border-solid border-neutral-500 shadow-md"
            name="pw"
            type={'password'}
            onChange={(e) => setPw(e.target.value)}
          />
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full justify-center">
          <div className="w-2/5 p-6 flex justify-center font-bold">
            <button
              type="submit"
              className="rounded p-4 w-36 bg-blue-500 text-xl text-white"
              onClick={() => handleLogin()}
            >
              LOGIN
            </button>
          </div>
        </div>
      </div>
      <KakaoLoginComponent />
    </div>
  )
}

export default LoginComponent

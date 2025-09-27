import { useActionState } from 'react'
import useCustomLogin from '../../hooks/useCustomLogin'
import jwtAxios from '../../util/JwtUtil'
import ResultModal from '../common/ResultModal'
import PendingModal from '../common/PendingModal'

interface ModifyResult {
  result: string
  error: string
}

const initState: ModifyResult = {
  result: '',
  error: '',
}

const modifyAction = async (state: ModifyResult, formData: FormData) => {
  const email = formData.get('email') as string
  const pw = formData.get('pw') as string
  const nickname = formData.get('nickname') as string

  if (pw.length < 8) {
    return { result: '', error: '패스워드는 8자 이상이어야 합니다.' }
  }

  try {
    await jwtAxios.put('http://localhost:8080/api/member/modify', {
      email,
      pw,
      nickname,
    })

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    return {
      result: '',
      error: err.response?.data?.message || '수정 중 오류가 발생했습니다.',
    }
  }

  return { result: 'Modified', error: '' }
}

function ModifyComponent() {
  const { loginState, moveToLogin } = useCustomLogin()
  const [state, action, isPending] = useActionState(modifyAction, initState)

  const closeModal = () => {
    moveToLogin()
  }

  return (
    <div className="mt-6">
      {isPending && <PendingModal />}

      {state.error && <div className="bg-amber-500"> {state.error} </div>}

      {state.result && (
        <ResultModal
          title="회원 정보 수정 결과 "
          content={'회원 정보가 수정되었습니다.'}
          callbackFn={closeModal}
        />
      )}

      <form action={action}>
        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">Email</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="email"
              type={'text'}
              defaultValue={loginState.email}
              readOnly
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">Password</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="pw"
              type={'password'}
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">Nickname</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="nickname"
              type={'text'}
              defaultValue={loginState.nickname}
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap justify-end">
            <button
              type="submit"
              className="rounded p-4 m-2 text-xl w-32 text-white bg-blue-500"
            >
              Modify
            </button>
          </div>
        </div>
      </form>
    </div>
  )
}
export default ModifyComponent

import useCustomLogin from '../../hooks/useCustomLogin'

function ModifyComponent() {
  const { loginState } = useCustomLogin()

  return (
    <div className="mt-6">
      <form>
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

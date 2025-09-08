import { Outlet, useNavigate } from 'react-router'

/**
 * useNavigate, Navigate의 차이점
 * Navigate: JSX 문법으로 사용하는 컴포넌트 ex) <Navigate to="/login" replace />;
 * 
 * useNavigate: 함수를 반환하는 훅 - 이벤트 핸들러(onClick 등)나 특정 로직이 실행될 때 명시적으로 페이지 이동을 실행할 때 사용
 * ex) 
 * const navigate = useNavigate();

  const handleSubmit = () => {
    navigate({ pathname: 'list' });
  };
 */
function IndexPage() {
  const navigate = useNavigate()

  const handleClickList = () => {
    navigate({ pathname: 'list' })
  }
  const handleClickAdd = () => {
    navigate({ pathname: 'add' })
  }

  return (
    <div>
      <div className="w-full flex m-2 p-2 ">
        <div
          className="text-xl m-1 p-2 w-20 font-extrabold text-center underline"
          onClick={handleClickList}
        >
          LIST
        </div>

        <div
          className="text-xl m-1 p-2 w-20 font-extrabold text-center underline"
          onClick={handleClickAdd}
        >
          ADD
        </div>
      </div>
      <div className="flex flex-wrap w-full">
        <Outlet />
      </div>
    </div>
  )
}

export default IndexPage

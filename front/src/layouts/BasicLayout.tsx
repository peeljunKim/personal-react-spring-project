import { Outlet } from 'react-router'
import BasicMenu from '../components/menus/BasicMenu' // Hearder
import CartComponent from '../components/menus/CartComponent'

/**
 * Outlet: 부모 컴포넌트(주로 레이아웃 컴포넌트)가 자식 라우트 컴포넌트를 어디에 렌더링할지 알려주는 "자리 표시자(placeholder)"
 */
function BasicLayout() {
  return (
    <>
      <BasicMenu />
      <div className="bg-white my-5 w-full flex flex-col space-y-4 md:flex-row md:space-x-4 md:space-y-0">
        <main className="bg-sky-300 md:w-4/5 lg:w-3/4 px-5 py-5">
          <Outlet />
        </main>
        <aside className="bg-green-300 md:w-1/3 lg:w-1/4 px-5 flex py-5">
          <CartComponent />
        </aside>
      </div>
    </>
  )
}

export default BasicLayout

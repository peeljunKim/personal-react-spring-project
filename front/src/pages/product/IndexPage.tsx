import { Outlet, useNavigate } from 'react-router'

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
      Products
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

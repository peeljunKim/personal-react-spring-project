// import { NavLink } from 'react-router'

import useZustandCount from '../store/useZustandCount'

/**
 * NavLink를 사용하면 MainPage, AboutPage를 가져옵니다 불 필요한 리소스 가져옴
 */
function MainPage() {
  const { current, inc, des, changeAmount } = useZustandCount()

  return (
    <div className="text-3xl">
      <div>mainPage</div>
      {/* <div className="flex">
        <NavLink to="/about">About</NavLink>
      </div> */}
      <div>{current}</div>
      <button className="bg-gray-500 text-white font-bold" onClick={inc}>
        +
      </button>
      <div>
        <button className="bg-gray-500 text-white font-bold" onClick={des}>
          - (아래 5, 15 버튼으로 변경 불가능)
        </button>
      </div>
      <div>
        <div onClick={() => changeAmount(1)}>1</div>
        <div onClick={() => changeAmount(5)}>5</div>
        <div onClick={() => changeAmount(15)}>15</div>
      </div>
    </div>
  )
}

export default MainPage

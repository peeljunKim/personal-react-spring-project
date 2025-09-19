import { createBrowserRouter } from 'react-router'
import { lazy, Suspense } from 'react'
import BasicLayout from '../layouts/BasicLayout.tsx'
import TodoRouter from './TodoRouter.tsx'
import ProductRouter from './ProductRouter.tsx'

/**
 * lazy: 레이징 로딩 (필요할 때 까지 로딩을 미루는 기능)
 * Suspense: fallback 옵션은 기다리는 동작 보여지는 페이지
 * 본문에는 React.lazy()를 이용해서 동적 임포트 작성 import() 함수를 사용하면 필요한 시점에 모듈을 로드할 수 있습니다.
 * 아래와 같이 작성하면 각 라우트에 접속할 때 필요한 컴포넌트만 따로 불러와 초기 로딩 시간을 단축하는 분할 로딩이 구현
 */
const Loading = () => <div>Loading...</div>
const Main = lazy(() => import('../pages/MainPage.tsx'))
const About = lazy(() => import('../pages/AboutPage.tsx'))
// const TodoIndex = lazy(() => import('../pages/todo/IndexPage.tsx'))

/**
 * React Router v6 이후에 라우트 구성 방식이 변경 - 두 번째 코드: React Router v6.4+의 최신 방식
 *
 */

// const router = createBrowserRouter([
//   {
//     path: '/',
//     element: (
//       <Suspense fallback={<Loading />}>
//         <Main />
//       </Suspense>
//     ),
//   },
//   {
//     path: '/aboutPage',
//     element: (
//       <Suspense fallback={<Loading />}>
//         <About />
//       </Suspense>
//     ),
//   },
// ])

const BasicRouter = createBrowserRouter([
  {
    path: '',
    Component: BasicLayout,
    children: [
      {
        index: true,
        element: (
          <Suspense fallback={<Loading />}>
            <Main />
          </Suspense>
        ),
      },
      {
        path: 'about',
        element: (
          <Suspense fallback={<Loading />}>
            <About />
          </Suspense>
        ),
      },
      TodoRouter(),
      ProductRouter(),
    ],
  },
])

export default BasicRouter

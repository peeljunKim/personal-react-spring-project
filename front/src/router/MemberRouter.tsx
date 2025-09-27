import { Suspense, lazy } from 'react'

const Loading = () => <div>Loading</div>

const Login = lazy(() => import('../pages/member/LoginPage'))
const Logout = lazy(() => import('../pages/member/LogoutPage'))
const MemberModify = lazy(() => import('../pages/member/ModifyPage'))

export default function memberRouter() {
  return {
    path: 'member',
    children: [
      {
        path: 'login',
        element: (
          <Suspense fallback={<Loading />}>
            <Login />
          </Suspense>
        ),
      },

      {
        path: 'logout',
        element: (
          <Suspense fallback={<Loading />}>
            <Logout />
          </Suspense>
        ),
      },

      {
        path: 'modify',
        element: (
          <Suspense fallback={<Loading />}>
            <MemberModify />
          </Suspense>
        ),
      },
    ],
  }
}

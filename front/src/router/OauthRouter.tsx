import { Suspense, lazy } from 'react'

const Loading = () => <div>Loading</div>

const KakaoRedirect = lazy(() => import('../pages/oauth/KakaoRedirectPage'))

export default function oauthRouter() {
  return {
    path: 'oauth',
    children: [
      {
        path: 'kakao',
        element: (
          <Suspense fallback={<Loading />}>
            <KakaoRedirect />
          </Suspense>
        ),
      },
    ],
  }
}

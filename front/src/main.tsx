import { createRoot } from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router'
import BasicRouter from './router/BasicRouter.tsx'
import { Provider } from 'react-redux'
import store from './store.tsx'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={queryClient}>
    <Provider store={store}>
      <RouterProvider router={BasicRouter} />
    </Provider>
    <ReactQueryDevtools initialIsOpen={true} />
  </QueryClientProvider>
)

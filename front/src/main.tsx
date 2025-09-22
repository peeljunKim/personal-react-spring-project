import { createRoot } from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router'
import BasicRouter from './router/BasicRouter.tsx'
import { Provider } from 'react-redux'
import store from './store.tsx'

createRoot(document.getElementById('root')!).render(
  <Provider store={store}>
    <RouterProvider router={BasicRouter}></RouterProvider>
  </Provider>
)

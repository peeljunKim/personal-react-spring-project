import { createRoot } from 'react-dom/client'
import './index.css'
import { RouterProvider } from 'react-router'
import BasicRouter from './router/BasicRouter.tsx'

createRoot(document.getElementById('root')!).render(
  <RouterProvider router={BasicRouter}></RouterProvider>
)

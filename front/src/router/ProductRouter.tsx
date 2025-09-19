import { lazy, Suspense } from 'react'
import { Navigate, Route } from 'react-router'
import { loadProducts } from '../pages/product/ListPage.tsx'

const ProductsIndex = lazy(() => import('../pages/product/IndexPage.tsx'))
const ProductsList = lazy(() => import('../pages/product/ListPage.tsx'))
const ProductsAdd = lazy(() => import('../pages/product/AddPage.tsx'))

const Loading = () => <div>Products Loading....</div>

export default function productsRouter() {
  return {
    path: 'products',
    Component: ProductsIndex,
    children: [
      {
        path: 'list',
        element: (
          <Suspense fallback={<Loading />}>
            <ProductsList />
          </Suspense>
        ),
        loader: loadProducts,
      },

      {
        path: 'add',
        element: (
          <Suspense fallback={<Loading />}>
            <ProductsAdd />
          </Suspense>
        ),
      },

      {
        path: '',
        element: <Navigate to={'/products/list'}></Navigate>,
      },
    ],
  }
}

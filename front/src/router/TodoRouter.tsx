import { lazy, Suspense } from 'react'
import { Navigate } from 'react-router'

/**
 * useNavigate, Navigate의 차이점
 * Navigate: JSX 문법으로 사용하는 컴포넌트 ex) <Navigate to="/login" replace />;
 * 
 * useNavigate: 함수를 반환하는 훅 - 이벤트 핸들러(onClick 등)나 특정 로직이 실행될 때 명시적으로 페이지 이동을 실행할 때 사용
 * ex) 
 * const navigate = useNavigate();

  const handleSubmit = () => {
    navigate({ pathname: 'list' });
  };
 */
const Loading = () => <div>Loading....</div>
const TodoIndex = lazy(() => import('../pages/todo/IndexPage'))
const TodoList = lazy(() => import('../pages/todo/ListPage'))
const TodoRead = lazy(() => import('../pages/todo/ReadPage'))

const TodoAdd = lazy(() => import('../pages/todo/AddPage'))
const TodoModify = lazy(() => import('../pages/todo/ModifyPage'))

const TodoRouter = () => {
  return {
    path: 'todo',
    Component: TodoIndex,
    children: [
      {
        path: 'list',
        element: (
          <Suspense fallback={<Loading />}>
            <TodoList />
          </Suspense>
        ),
      },
      {
        path: 'read/:tno',
        element: (
          <Suspense fallback={<Loading />}>
            <TodoRead />
          </Suspense>
        ),
      },
      {
        path: 'modify/:tno',
        element: (
          <Suspense fallback={<Loading />}>
            <TodoModify />
          </Suspense>
        ),
      },
      {
        path: 'add',
        element: (
          <Suspense fallback={<Loading />}>
            <TodoAdd />
          </Suspense>
        ),
      },
      // localhost/todo로 이동하면 바로 `/todo/list`로 이동 - 동적 페이지 이동
      {
        path: '',
        element: <Navigate to={'/todo/list'}></Navigate>,
      },
    ],
  }
}

export default TodoRouter

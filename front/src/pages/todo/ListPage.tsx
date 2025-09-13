import { useSearchParams } from 'react-router'
import ListComponent from '../../components/todo/ListComponent'

/**
 * useSearchParams: url에 쿼리 스트링을 가져오는 hooks
 */
function ListPage() {
  const [queryParams] = useSearchParams()
  console.log(queryParams)

  const page: string | null = queryParams.get('page')
  const size: string | null = queryParams.get('size')

  return (
    <div className="bg-white w-full">
      <div className="text-4xl">
        Todo List Page {page} {size}
      </div>

      <ListComponent></ListComponent>
    </div>
  )
}

export default ListPage

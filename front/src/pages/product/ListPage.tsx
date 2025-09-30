import axios from 'axios'
import {
  createSearchParams,
  useLoaderData,
  type LoaderFunctionArgs,
} from 'react-router'
import jwtAxios from '../../util/JwtUtil'
import ListComponent from '../../components/product/ListComponent'

export async function loadProducts({ request }: LoaderFunctionArgs) {
  const url = new URL(request.url)
  const page = url.searchParams.get('page') || '1'
  const size = url.searchParams.get('size') || '10'
  const queryStr = createSearchParams({ page, size }).toString()

  // api를 여기서 설정
  const res = await jwtAxios.get(
    `http://localhost:8080/api/products/list?${queryStr}`
  )

  return res.data
}

const ListPage = () => {
  const pageResponse = useLoaderData()
  console.log(pageResponse)

  return (
    <div className="w-full mt-4 border border-solid border-neutral-300 shadow-md">
      <div className="text-2xl m-4 font-extrabold">Products List Page</div>
      <ListComponent serverData={pageResponse}></ListComponent>
    </div>
  )
}

export default ListPage

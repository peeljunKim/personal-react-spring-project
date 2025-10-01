import axios from 'axios'
import {
  createSearchParams,
  useLoaderData,
  type LoaderFunctionArgs,
} from 'react-router'
import jwtAxios from '../../util/JwtUtil'
import ListComponent from '../../components/product/ListComponent'
import { useQuery } from '@tanstack/react-query'
import useCustomMove from '../../hooks/useCustomMove'
import PendingModal from '../../components/common/PendingModal'

// export async function loadProducts({ request }: LoaderFunctionArgs) {
//   const url = new URL(request.url)
//   const page = url.searchParams.get('page') || '1'
//   const size = url.searchParams.get('size') || '10'
//   const queryStr = createSearchParams({ page, size }).toString()

//   // api를 여기서 설정
//   const res = await jwtAxios.get(
//     `http://localhost:8080/api/products/list?${queryStr}`
//   )

//   return res.data
// }

const ListPage = () => {
  // const pageResponse = useLoaderData()
  // console.log(pageResponse)

  const { page, size } = useCustomMove()
  const queryStr = createSearchParams({
    page: String(page),
    size: String(size),
  }).toString()

  const { data, error, isPending } = useQuery({
    queryKey: ['products/list', page, size],
    queryFn: async () => {
      const res = await jwtAxios.get(
        `http://localhost:8080/api/products/list?${queryStr}`
      )

      return res.data
    },
    staleTime: 1000 * 60, // 1분 - 해당 시간 이후에는 늙은 데이터가 됨(stale) -> 해당 시간 이전에 데이터를 호출하면 서버에서 데이터를 가져오기 않고 기존 데이터를 가져옴(캐싱)
  })

  return (
    <div className="w-full mt-4 border border-solid border-neutral-300 shadow-md">
      {isPending && <PendingModal />}
      <div className="text-2xl m-4 font-extrabold">Products List Page</div>
      {data && <ListComponent serverData={data}></ListComponent>}
    </div>
  )
}

export default ListPage

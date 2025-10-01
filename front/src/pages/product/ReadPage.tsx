import axios from 'axios'
import { useLoaderData, useParams } from 'react-router'
import { type LoaderFunctionArgs } from 'react-router'
import ReadComponent from '../../components/product/ReadComponent'
import jwtAxios from '../../util/JwtUtil'
import { useQuery } from '@tanstack/react-query'
import PendingModal from '../../components/common/PendingModal'

// export async function loadProduct({ params }: LoaderFunctionArgs) {
//   const { pno } = params
//   const res = await jwtAxios.get(`http://localhost:8080/api/products/${pno}`)
//   return res.data
// }

function ReadPage() {
  // const product: ProductDTO = useLoaderData()
  // console.log(product)
  const { pno } = useParams()

  const { data, error, isPending } = useQuery({
    queryKey: ['products', pno],
    queryFn: async () => {
      const res = await jwtAxios.get(
        `http://localhost:8080/api/products/${pno}`
      )
      return res.data
    },
    staleTime: 1000 * 60 * 60,
  })

  const product: ProductDTO = data
  console.log(product)

  return (
    <div className="w-full">
      {isPending && <PendingModal />}
      <div>Product Read</div>
      {data && <ReadComponent product={product} />}
    </div>
  )
}

export default ReadPage

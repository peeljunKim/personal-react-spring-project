import { useLoaderData, useParams } from 'react-router'
import ModifyComponent from '../../components/product/ModifyComponent'
import { useQuery } from '@tanstack/react-query'
import jwtAxios from '../../util/JwtUtil'
import PendingModal from '../../components/common/PendingModal'

function ModifyPage() {
  // const product: ProductDTO = useLoaderData()
  const { pno } = useParams()

  const { data, error, isPending } = useQuery({
    queryKey: ['product', pno],
    queryFn: async () => {
      const res = await jwtAxios.get(
        `http://localhost:8080/api/products/${pno}`
      )
      return res.data
    },
    staleTime: 1000 * 60 * 60 * 24, // 24시간
  })

  const product: ProductDTO = data
  console.log(product)

  return (
    <div className="p-4 w-full bg-white">
      {isPending && <PendingModal />}

      <div className="text-3xl font-extrabold">Products Modify Page</div>
      {data && <ModifyComponent product={product} />}
    </div>
  )
}

export default ModifyPage

import { useParams } from 'react-router'
import ReadComponent from '../../components/todo/ReadComponent'

/**
 * useParams: 동적 매개변수의 값을 가져옴 (문자열(객체)로 가져옴)
 */
function ReadPage() {
  const { tno } = useParams()

  console.log(tno)

  return (
    <div className="bg-white w-full">
      <div className="text-4xl">Todo Read Page {tno}</div>
      <ReadComponent tno={Number(tno)} />
    </div>
  )
}

export default ReadPage

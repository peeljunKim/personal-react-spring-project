import { useParams } from 'react-router'
import ModifyComponent from '../../components/todo/ModifyComponent.tsx'

const ModifyPage = () => {
  const { tno } = useParams()

  return (
    <div className="p-4 w-full bg-white">
      <div className="text-3xl font-extrabold"> Todo Modify Page </div>
      <ModifyComponent tno={Number(tno)} />
    </div>
  )
}
export default ModifyPage

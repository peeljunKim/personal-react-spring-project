import { useParams } from 'react-router'

function ModifyPage() {
  const { tno } = useParams()

  return (
    <div className="bg-white w-full">
      <div className="text-4xl">Todo Modify Page {tno}</div>
    </div>
  )
}

export default ModifyPage

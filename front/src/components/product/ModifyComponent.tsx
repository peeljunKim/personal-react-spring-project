import { useActionState, useState, type MouseEvent } from 'react'
import useCustomMove from '../../hooks/useCustomMove'
import PendingModal from '../common/PendingModal'
import ResultModal from '../common/ResultModal'
import jwtAxios from '../../util/JwtUtil'

interface ProductTaskResult {
  actionType: string
  result: string
  error?: string
}

const initState: ProductTaskResult = {
  actionType: 'modify',
  result: '',
}

const modifyDeleteAsyncAction = async (
  state: ProductTaskResult,
  formData: FormData
): Promise<ProductTaskResult> => {
  const pno = formData.get('pno')
  const actionType = formData.get('actionType') as string

  let res
  if (actionType === 'modify') {
    res = await jwtAxios.put(
      `http://localhost:8080/api/products/${pno}`,
      formData
    )
  } else if (actionType === 'delete') {
    res = await jwtAxios.delete(`http://localhost:8080/api/products/${pno}`)
  }

  return { actionType: actionType, result: res?.data?.RESULT }
}

function ModifyComponent({ product }: { product: ProductDTO }) {
  const { moveToRead, moveToList } = useCustomMove()

  const [images, setImages] = useState<string[]>([...product.uploadedFileNames])

  const [state, action, isPending] = useActionState(
    modifyDeleteAsyncAction,
    initState
  )

  const deleteOldImages = (
    event: MouseEvent<HTMLButtonElement>,
    target: string
  ) => {
    event.preventDefault()
    event.stopPropagation()

    setImages((prev) => prev.filter((img) => img !== target))
  }

  return (
    <div className="border-2 border-sky-200 mt-10 m-2 p-4 bg-white">
      {isPending && <PendingModal />}

      {state.result && (
        <ResultModal
          title="처리완료"
          content="처리 완료"
          callbackFn={() => {
            if (state.actionType === 'modify') {
              moveToRead(product.pno)
            }
            if (state.actionType === 'delete') {
              moveToList()
            }
          }}
        />
      )}

      <form action={action}>
        <div className="flex justify-center mt-10">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">PNO</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="pno"
              required
              defaultValue={product.pno}
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">PNAME</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="pname"
              required
              defaultValue={product.pname}
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">PRICE</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              name="price"
              type={'number'}
              defaultValue={product.price}
            ></input>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">PDESC</div>
            <textarea
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md resize-y"
              name="pdesc"
              rows={4}
              required
              defaultValue={product.pdesc}
            ></textarea>
          </div>
        </div>

        <div className="flex justify-center">
          <div className="relative mb-4 flex w-full flex-wrap items-stretch">
            <div className="w-1/5 p-6 text-right font-bold">Files</div>
            <input
              className="w-4/5 p-6 rounded-r border border-solid border-neutral-300 shadow-md"
              type={'file'}
              name="files"
              multiple={true}
            ></input>
          </div>
        </div>

        <div className="w-full justify-center flex flex-col m-auto items-center">
          {images.map((imgFile, i) => (
            <div className="flex justify-center flex-col w-1/3" key={i}>
              <button
                className="bg-blue-500 text-3xl text-white"
                onClick={(event) => deleteOldImages(event, imgFile)}
              >
                DELETE
              </button>
              <img
                alt="img"
                src={`http://localhost:8080/api/products/view/s_${imgFile}`}
              />
              <input type="hidden" name="uploadedFileNames" value={imgFile} />
            </div>
          ))}
        </div>

        <div className="flex justify-end p-4">
          <button
            type="submit"
            name="actionType"
            value="delete"
            className="rounded p-4 m-2 text-xl w-32 text-white bg-red-500"
          >
            Delete
          </button>

          <button
            type="submit"
            name="actionType"
            value="modify"
            className="inline-block rounded p-4 m-2 text-xl w-32 text-white bg-orange-500"
          >
            Modify
          </button>

          <button
            type="button"
            className="rounded p-4 m-2 text-xl w-32 text-white bg-blue-500"
            onClick={() => moveToList()}
          >
            List
          </button>
        </div>
      </form>
    </div>
  )
}

export default ModifyComponent

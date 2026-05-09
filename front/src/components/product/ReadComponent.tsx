import useCustomCart from '../../hooks/useCustomCart'
import useCustomMove from '../../hooks/useCustomMove'

function ReadComponent({ product }: { product: ProductDTO }) {
  const { moveToModify, moveToList } = useCustomMove()
  const { changeCart, cartItems } = useCustomCart()

  const handleClickAddCart = () => {
    const addedItem = cartItems.items.filter(
      (item) => item.pno === product.pno
    )[0]

    if (addedItem) {
      if (
        window.confirm('이미 추가된 상품입니다. 추가하시겠습니까?') === false
      ) {
        return
      }
    }

    if (addedItem) {
      changeCart(addedItem.cino, product.pno, 1)
    } else {
      changeCart(null, product.pno, 1)
    }
  }

  return (
    <div className="border-2 border-sky-200 mt-10 m-2 p-4 bg-white">
      <div className="flex justify-center mt-10">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">PNO</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md">
            {product.pno}
          </div>
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">PNAME</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md">
            {product.pname}
          </div>
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">PRICE</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md">
            {product.price}
          </div>
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">STOCK</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md">
            {product.stock}
          </div>
        </div>
      </div>

      <div className="flex justify-center">
        <div className="relative mb-4 flex w-full flex-wrap items-stretch">
          <div className="w-1/5 p-6 text-right font-bold">PDESC</div>
          <div className="w-4/5 p-6 rounded-r border border-solid shadow-md">
            {product.pdesc}
          </div>
        </div>
      </div>

      <div className="w-full justify-center flex flex-col m-auto items-center">
        {product.uploadedFileNames.map((imgFile, i) => (
          <img
            alt="product"
            key={i}
            className="p-4 w-1/2"
            src={`http://localhost:8080/api/products/view/${imgFile}`}
          />
        ))}
      </div>

      <div className="flex justify-end p-4">
        <button
          type="button"
          className="inline-block rounded p-4 m-2 text-xl w-32 text-white bg-green-500"
          onClick={() => handleClickAddCart()}
        >
          Add Cart
        </button>

        <button
          type="button"
          className="inline-block rounded p-4 m-2 text-xl w-32 text-white bg-red-500"
          onClick={() => moveToModify(product.pno)}
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
    </div>
  )
}

export default ReadComponent

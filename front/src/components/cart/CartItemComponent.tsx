interface CartItemComponentProps {
  cartItem: CartItemResponse
  changeCart: (cino: number, pno: number, amount: number) => void
}

function CartItemComponent({ cartItem, changeCart }: CartItemComponentProps) {
  const { cino, pno, pname, price, qty, imageFile } = cartItem

  return (
    <li key={cino} className="border-2">
      <div className="w-full border-2">
        <div className=" m-1 p-1 ">
          <img src={`http://localhost:8080/api/products/view/s_${imageFile}`} />
        </div>

        <div className="justify-center p-2 text-xl ">
          <div className="justify-end w-full"></div>
          <div>Cart Item No: {cino}</div>
          <div>Pno: {pno}</div>
          <div>Name: {pname}</div>
          <div>Price: {price}</div>
          <div className="flex ">
            <div className="w-2/3">Qty: {qty}</div>
            <div>
              <button
                className="m-1 p-1 text-2xl bg-orange-500 w-8 rounded-lg"
                onClick={() => changeCart(cino, pno, 1)}
              >
                +
              </button>

              <button
                className="m-1 p-1 text-2xl bg-orange-500 w-8 rounded-lg"
                onClick={() => changeCart(cino, pno, -1)}
              >
                -
              </button>
            </div>
          </div>
          <div>
            <div className="flex text-white font-bold p-2 justify-center">
              <button
                className="m-1 p-1 text-xl text-white bg-red-500 w-8 rounded-lg"
                onClick={() => changeCart(cino, pno, -1 * qty)}
              >
                X
              </button>
            </div>

            <div className="font-extrabold border-t-2 text-right m-2 pr-4">
              {qty * price} 원
            </div>
          </div>
        </div>
      </div>
    </li>
  )
}

export default CartItemComponent

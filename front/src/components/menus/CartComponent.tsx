import useCustomCart from '../../hooks/useCustomCart'
import CartItemComponent from '../cart/CartItemComponent'

const CartComponent = () => {
  const { loginState, loginStatus, cartItems, changeCart } = useCustomCart()

  return (
    <div className="w-full">
      {loginStatus && (
        <>
          {cartItems.status === 'pending' && <div>Loading....</div>}
          {cartItems.status === 'fulfilled' && (
            <>
              <div>{loginState.email}님 Cart</div>
              <div className="bg-orange-600 text-center text-white font-bold w-1/5 rounded-full m-1">
                {cartItems.items.length}
              </div>
              <div>
                <ul>
                  {cartItems.items.map((item: CartItemResponse) => (
                    <CartItemComponent
                      key={item.cino}
                      cartItem={item}
                      changeCart={changeCart}
                    ></CartItemComponent>
                  ))}
                </ul>
              </div>
            </>
          )}
        </>
      )}
    </div>
  )
}

export default CartComponent

import useCustomCart from '../../hooks/useCustomCart'
import CartItemComponent from '../cart/CartItemComponent'

type PaymentGateway = {
  init?: (merchantId: string) => void
  request_pay: (
    payment: {
      pg: string
      pay_method: string
      merchant_uid: string
      name: string
      amount: number
      buyer_email: string
    },
    callback?: (response: unknown) => void
  ) => void
}

const CartComponent = () => {
  const { loginState, loginStatus, cartItems, changeCart } = useCustomCart()
  const isLoggedIn = loginStatus === 'fulfilled' && Boolean(loginState.email)
  const hasCartItems = cartItems.items.length > 0
  const canCheckout = isLoggedIn && hasCartItems
  const totalPrice = cartItems.items.reduce(
    (sum, item) => sum + item.price * item.qty,
    0
  )

  const handleClickCheckout = () => {
    const paymentGateway = (
      window as Window & {
        IMP?: PaymentGateway
      }
    ).IMP

    if (!paymentGateway?.request_pay) {
      window.alert('결제 모듈을 불러오지 못했습니다.')
      return
    }

    const merchantId = import.meta.env.VITE_PG_MERCHANT_ID
    if (merchantId) {
      paymentGateway.init?.(merchantId)
    }

    paymentGateway.request_pay(
      {
        pg: 'html5_inicis',
        pay_method: 'card',
        merchant_uid: `cart_${Date.now()}`,
        name:
          cartItems.items.length === 1
            ? cartItems.items[0].pname
            : `${cartItems.items[0].pname} 외 ${cartItems.items.length - 1}건`,
        amount: totalPrice,
        buyer_email: loginState.email,
      },
      (response) => {
        console.log('payment response', response)
      }
    )
  }

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
              {canCheckout && (
                <button
                  type="button"
                  className="mt-3 w-full rounded-xl bg-blue-500 px-4 py-3 text-white font-bold shadow-md hover:bg-blue-600"
                  onClick={handleClickCheckout}
                >
                  결제하기
                </button>
              )}
            </>
          )}
        </>
      )}
    </div>
  )
}

export default CartComponent

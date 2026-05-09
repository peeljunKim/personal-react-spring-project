import useCustomCart from '../../hooks/useCustomCart'
import { postCompletePayment, postPreparePayment } from '../../api/PaymentApi'
import CartItemComponent from '../cart/CartItemComponent'

const CartComponent = () => {
  const { loginState, loginStatus, cartItems, changeCart } = useCustomCart()
  const isLoggedIn = loginStatus === 'fulfilled' && Boolean(loginState.email)
  const hasCartItems = cartItems.items.length > 0
  const canCheckout = isLoggedIn && hasCartItems

  const handleClickCheckout = async () => {
    try {
      const preparedPayment = await postPreparePayment()
      const paymentGateway = window.PortOne
      // console.log('paymentId', preparedPayment.paymentId)

      // function randomId() {
      //   return [...crypto.getRandomValues(new Uint32Array(2))]
      //     .map((word) => word.toString(16).padStart(8, '0'))
      //     .join('')
      // }
      // preparedPayment.paymentId = randomId()
      // console.log('preparedPayment', preparedPayment)

      if (!paymentGateway?.requestPayment) {
        window.alert('결제 모듈을 불러오지 못했습니다.')
        return
      }

      const paymentResponse = await paymentGateway.requestPayment({
        storeId: preparedPayment.storeId,
        channelKey: preparedPayment.channelKey,
        paymentId: preparedPayment.paymentId,
        orderName: preparedPayment.orderName,
        totalAmount: preparedPayment.totalAmount,
        currency: preparedPayment.currency,
        payMethod: preparedPayment.payMethod,
        customer: {
          email: loginState.email,
        },
        noticeUrls: preparedPayment.noticeUrl
          ? [preparedPayment.noticeUrl]
          : undefined,
      })

      if (paymentResponse?.code) {
        window.alert(paymentResponse.message ?? '결제가 완료되지 않았습니다.')
        return
      }

      const syncedPayment = await postCompletePayment(preparedPayment.paymentId)
      window.alert(syncedPayment.message)
    } catch (error) {
      console.error(error)
      window.alert('결제 처리 중 오류가 발생했습니다.')
    }
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

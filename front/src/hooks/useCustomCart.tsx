import { useDispatch, useSelector } from 'react-redux'
import type { AppDispatch, RootState } from '../store'
import useCustomLogin from './useCustomLogin'
import { useEffect } from 'react'
import { getCartItemsAsync, postChangeCartAsync } from '../slices/CartSlice'

function useCustomCart() {
  const { loginState, loginStatus } = useCustomLogin()
  const cartItems = useSelector((state: RootState) => state.cartSlice)
  const dispatch = useDispatch<AppDispatch>()

  useEffect(() => {
    if (loginStatus) {
      dispatch(getCartItemsAsync())
    }
  }, [loginStatus])

  const changeCart = (cino: number | null, pno: number, amount: number) => {
    const email = loginState.email
    let qty = 1

    if (cino) {
      const targetArr = cartItems.items.filter((item) => item.cino === cino)
      if (targetArr.length > 0) {
        qty = targetArr[0].qty + amount
      }
    }

    const requestItem: CartItemRequest = cino
      ? { email, cino, pno, qty }
      : { email, pno, qty }
    console.log(requestItem)

    dispatch(postChangeCartAsync(requestItem))
  }

  return { loginState, loginStatus, cartItems, changeCart }
}

export default useCustomCart

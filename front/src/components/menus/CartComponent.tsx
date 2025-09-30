import { useDispatch, useSelector } from 'react-redux'
import useCustomLogin from '../../hooks/useCustomLogin'
import type { AppDispatch, RootState } from '../../store'
import { useEffect } from 'react'
import { getCartItemsAsync } from '../../slices/CartSlice'

const CartComponent = () => {
  const { loginState, loginStatus } = useCustomLogin()
  const cartItems = useSelector((state: RootState) => state.cartSlice)
  const dispatch = useDispatch<AppDispatch>()

  useEffect(() => {
    if (loginStatus) {
      dispatch(getCartItemsAsync())
    }
  }, [loginStatus])

  return (
    <div className="w-full">
      {loginStatus && <div>{loginState.email}님 Cart</div>}
    </div>
  )
}

export default CartComponent

import { create } from 'zustand'
import { getCartItems, postChangeCart } from '../api/CartApi'

export interface CartStore {
  items: CartItemResponse[]
  status: string
  getItems: () => void
  requestChangeCart: (cartItem: CartItemRequest) => void
}

const useZustandCart = create<CartStore>()((set) => ({
  items: [],
  status: '',
  getItems: async () => {
    set({ status: 'pending' })
    const cartData = await getCartItems()

    console.log('cartData: ', cartData)

    set({ items: cartData, status: 'fulfilled' })
  },
  requestChangeCart: async (cartItem: CartItemRequest) => {
    set({ status: 'pending' })

    const changedData = await postChangeCart(cartItem)

    set({ items: changedData, status: 'fulfilled' })
  },
}))
export default useZustandCart

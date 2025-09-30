import jwtAxios from '../util/JwtUtil'

const host = 'http://localhost:8080/api/cart'

export const getCartItems = async (): Promise<CartItemResponse[]> => {
  const res = await jwtAxios.get(`${host}/items`)
  return res.data
}

export const postChangeCart = async (cartItem: CartItemRequest) => {
  const res = await jwtAxios.post(`${host}/change`, cartItem)
  return res.data
}

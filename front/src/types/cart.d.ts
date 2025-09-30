interface CartItemRequest {
  email: string
  pno: number
  qty: number
  cino?: number
  status?: string
}

interface CartItemResponse {
  cino: number
  qty: number
  pno: number
  pname: string
  price: number
  imageFile: string
}

interface CartItemsArray {
  items: CartItemResponse[]
  status?: string
}

import jwtAxios from '../util/JwtUtil'

const host = 'http://localhost:8080/api/payments'

export const postPreparePayment = async (
  memberCouponId?: number
): Promise<PaymentPrepareResponse> => {
  const body = memberCouponId ? { memberCouponId } : undefined
  const res = await jwtAxios.post(`${host}/prepare`, body)
  return res.data
}

export const postCompletePayment = async (
  paymentId: string
): Promise<PaymentSyncResponse> => {
  const res = await jwtAxios.post(`${host}/complete`, { paymentId })
  return res.data
}

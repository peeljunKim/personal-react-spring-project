import jwtAxios from '../util/JwtUtil'

const host = 'http://localhost:8080/api/me/coupons'

export const getApplicableCoupons = async (): Promise<
  CouponApplicabilityResponse[]
> => {
  const res = await jwtAxios.get(`${host}/applicable`)
  return res.data
}

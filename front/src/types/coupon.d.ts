interface CouponApplicabilityResponse {
  memberCouponId: number
  policyId: number
  policyName: string
  issueType: string
  couponStatus: string
  policyStatus: string
  discountAmount: number
  expectedDiscountAmount: number
  applicableAmount: number
  minOrderAmount: number
  applyScope: string
  useStartAt: string
  useEndAt: string
}

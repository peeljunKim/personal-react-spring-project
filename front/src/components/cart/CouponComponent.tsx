import { useEffect, useMemo, useState } from 'react'
import { getApplicableCoupons } from '../../api/CouponApi'

interface CouponComponentProps {
  cartItems: CartItemResponse[]
  orderAmount: number
  onSelectCoupon: (memberCouponId?: number) => void
}

const CouponComponent = ({
  cartItems,
  orderAmount,
  onSelectCoupon,
}: CouponComponentProps) => {
  const [coupons, setCoupons] = useState<CouponApplicabilityResponse[]>([])
  const [selectedCouponId, setSelectedCouponId] = useState<number>()
  const [couponStatus, setCouponStatus] = useState('idle')
  const cartSignature = useMemo(
    () => cartItems.map((item) => `${item.cino}:${item.qty}`).join('|'),
    [cartItems]
  )
  const selectedCoupon = coupons.find(
    (coupon) => coupon.memberCouponId === selectedCouponId
  )
  const discountAmount = selectedCoupon?.expectedDiscountAmount ?? 0
  const payableAmount = Math.max(0, orderAmount - discountAmount)

  useEffect(() => {
    let ignore = false

    setSelectedCouponId(undefined)
    onSelectCoupon(undefined)
    setCouponStatus('pending')

    getApplicableCoupons()
      .then((data) => {
        if (ignore) {
          return
        }
        setCoupons(data)
        setCouponStatus('fulfilled')
      })
      .catch((error) => {
        if (ignore) {
          return
        }
        console.error(error)
        setCoupons([])
        setCouponStatus('rejected')
      })

    return () => {
      ignore = true
    }
  }, [cartSignature, onSelectCoupon])

  const handleChangeCoupon = (memberCouponId?: number) => {
    setSelectedCouponId(memberCouponId)
    onSelectCoupon(memberCouponId)
  }

  const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

  const formatCouponEndAt = (dateTime: string) =>
    `${dateTime.replace('T', ' ').slice(0, 16)}까지`

  const formatApplyScope = (applyScope: string) => {
    if (applyScope === 'ORDER') {
      return '전체 주문'
    }
    if (applyScope === 'PRODUCT') {
      return '상품'
    }
    if (applyScope === 'CATEGORY') {
      return '카테고리'
    }
    return applyScope
  }

  return (
    <div className="mt-3 border-2 bg-white p-3">
      <div className="flex justify-between font-bold">
        <span>주문 금액</span>
        <span>{formatWon(orderAmount)}</span>
      </div>

      <div className="mt-3 border-t-2 pt-3">
        <div className="font-bold">쿠폰</div>
        {couponStatus === 'pending' && (
          <div className="mt-2 text-sm text-gray-600">쿠폰 조회 중...</div>
        )}
        {couponStatus === 'rejected' && (
          <div className="mt-2 text-sm text-red-600">
            쿠폰을 불러오지 못했습니다.
          </div>
        )}

        <label className="mt-2 flex cursor-pointer items-center gap-2 border p-2 text-sm">
          <input
            type="radio"
            name="cartCoupon"
            checked={selectedCouponId === undefined}
            onChange={() => handleChangeCoupon(undefined)}
          />
          <span>쿠폰 적용 안 함</span>
        </label>

        {coupons.length === 0 && couponStatus === 'fulfilled' && (
          <div className="mt-2 text-sm text-gray-600">
            사용 가능한 쿠폰이 없습니다.
          </div>
        )}

        <div className="mt-2 space-y-2">
          {coupons.map((coupon) => (
            <label
              key={coupon.memberCouponId}
              className="block cursor-pointer border p-2 text-sm"
            >
              <div className="flex items-start gap-2">
                <input
                  type="radio"
                  name="cartCoupon"
                  checked={selectedCouponId === coupon.memberCouponId}
                  onChange={() => handleChangeCoupon(coupon.memberCouponId)}
                />
                <div className="w-full">
                  <div className="font-bold">{coupon.policyName}</div>
                  <div>
                    최소 {formatWon(coupon.minOrderAmount)} /{' '}
                    {formatApplyScope(coupon.applyScope)}
                  </div>
                  <div>
                    예상 할인 {formatWon(coupon.expectedDiscountAmount)}
                  </div>
                  <div className="text-gray-600">
                    {formatCouponEndAt(coupon.useEndAt)}
                  </div>
                </div>
              </div>
            </label>
          ))}
        </div>
      </div>

      <div className="mt-3 border-t-2 pt-3 text-sm">
        <div className="flex justify-between">
          <span>할인 금액</span>
          <span>-{formatWon(discountAmount)}</span>
        </div>
        <div className="mt-1 flex justify-between font-bold">
          <span>예상 결제 금액</span>
          <span>{formatWon(payableAmount)}</span>
        </div>
      </div>
    </div>
  )
}

export default CouponComponent

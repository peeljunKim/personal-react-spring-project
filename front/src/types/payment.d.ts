interface PaymentPrepareResponse {
  orderId: number
  paymentId: string
  orderName: string
  totalAmount: number
  originalAmount: number
  discountAmount: number
  payableAmount: number
  currency: string
  payMethod: string
  storeId: string
  channelKey: string
  noticeUrl?: string
}

interface PaymentSyncResponse {
  paymentId: string
  orderStatus: string
  paymentStatus: string
  message: string
}

interface PortOnePaymentResponse {
  code?: string
  message?: string
  paymentId?: string
  transactionId?: string
}

interface PortOnePaymentRequest {
  storeId: string
  channelKey: string
  paymentId: string
  orderName: string
  totalAmount: number
  currency: string
  payMethod: string
  customer?: {
    email?: string
  }
  noticeUrls?: string[]
}

interface Window {
  PortOne?: {
    requestPayment: (
      payment: PortOnePaymentRequest
    ) => Promise<PortOnePaymentResponse | undefined>
  }
}

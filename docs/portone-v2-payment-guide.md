# PortOne V2 결제 처리 가이드

## 원칙

- 포트원 V2 REST API의 결제 조회(`GET /payments/{paymentId}`) 결과만 신뢰한다.
- 클라이언트 응답은 누락될 수 있으므로 `/api/payments/webhook` 웹훅으로 반드시 다시 동기화한다.
- `PORTONE_API_SECRET`, `PORTONE_WEBHOOK_SECRET`은 환경 변수로만 주입하고 DB, 프론트엔드, 로그에 남기지 않는다.
- 주문 금액과 포트원 결제 금액이 다르면 즉시 포트원 취소 API(`POST /payments/{paymentId}/cancel`)를 호출한다.

## 환경 변수

- `PORTONE_API_SECRET`: 서버의 포트원 V2 REST API 인증 시크릿
- `PORTONE_STORE_ID`: 포트원 상점 ID
- `PORTONE_CHANNEL_KEY`: 결제창 호출에 사용할 채널 키
- `PORTONE_WEBHOOK_URL`: 포트원 콘솔에 등록할 웹훅 URL
- `PORTONE_WEBHOOK_SECRET`: Standard Webhooks 검증용 시크릿
- `PORTONE_WEBHOOK_SIGNATURE_REQUIRED`: 로컬 테스트에서만 `false`로 낮출 수 있으며 기본값은 `true`

## 정상 흐름

1. 클라이언트가 `/api/payments/prepare`를 호출한다.
2. 서버가 `payment:prepare:member:{email}` 락을 획득한다.
3. 락 획득 후 트랜잭션을 시작해 장바구니를 주문 스냅샷으로 고정하고 `paymentId`를 생성한다.
4. 클라이언트는 서버가 발급한 `paymentId`, `storeId`, `channelKey`, 금액으로 PortOne V2 SDK 결제창을 연다.
5. 클라이언트 완료 콜백 또는 포트원 웹훅이 `/api/payments/complete`, `/api/payments/webhook`으로 결제 동기화를 요청한다.
6. 서버는 `payment:sync:{paymentId}` 락과 `stock:product:{productId}` 락을 획득한 뒤 트랜잭션을 시작한다.
7. DB 주문 금액과 포트원 결제 금액이 같고 재고가 충분하면 재고를 차감하고 주문을 `PAID`로 변경한다.

## 예외 처리

- 금액 불일치: 포트원 결제 조회 결과의 `amount.total`과 DB `Order.amount`가 다르면 포트원 취소 API를 먼저 호출하고 `PaymentVerificationException`을 발생시켜 현재 DB 트랜잭션을 롤백한다. 이후 별도 `REQUIRES_NEW` 트랜잭션으로 주문을 `CANCEL` 처리한다.
- 재고 부족: 상품별 락을 잡은 상태에서 재고를 재검증한다. 부족하면 포트원 취소 API를 호출하고 현재 DB 트랜잭션을 롤백한다.
- 포트원 취소 API 실패: DB를 완료 처리하지 않는다. 예외를 전파해 웹훅은 재시도되도록 하고, 운영자는 포트원 콘솔과 내부 주문 상태를 대사한다.
- 결제창 닫힘/네트워크 오류: 클라이언트 완료 콜백이 없어도 포트원 웹훅이 동일한 `paymentId`로 동기화한다.

## 멱등성

- 포트원 웹훅의 `webhook-id`를 Redis 키 `idempotency:portone:webhook:{webhookId}`로 저장한다.
- 처리 중 키는 10분 TTL의 `PROCESSING`, 성공 후 키는 7일 TTL의 `PROCESSED`로 유지한다.
- DB의 `tbl_portone_webhook_log.webhook_id`에도 유니크 제약을 둬 Redis TTL 이후 재전송도 중복 처리하지 않는다.
- 주문 동기화 자체도 `Order.status`가 이미 `PAID` 또는 `CANCEL`이면 더 이상 재고를 차감하지 않는 방식으로 멱등 처리한다.

## 락과 트랜잭션 순서

- 올바른 순서: 분산 락 획득 → 트랜잭션 시작 → DB 검증/변경 → 커밋/롤백 → 락 해제.
- 잘못된 순서: 트랜잭션 시작 → DB 조회 → 분산 락 획득. 이 경우 락을 기다리는 동안 이미 조회한 영속성 컨텍스트가 오래된 데이터를 들고 있어 Race Condition이 남을 수 있다.
- 결제 준비는 사용자 단위 키 `payment:prepare:member:{email}`만 사용해 같은 사용자의 중복 주문 생성을 막는다.
- 결제 완료는 `payment:sync:{paymentId}`와 `stock:product:{productId}`를 분리해 서로 다른 상품 결제는 병렬 처리되도록 한다.

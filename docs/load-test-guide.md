# 로컬 부하 테스트 가이드

## 개요

이 문서는 현재 프로젝트의 로컬 환경에서 백엔드 API, MySQL, Redis, Docker 기반 Prometheus/Grafana/Redis exporter를 활용해 안정 TPS, 응답 시간, 에러율, 일일 처리 가능량, Saturation Point, 병목 후보를 확인하기 위한 기준이다.

로컬 테스트의 목적은 운영 성능 보장이 아니라 "현재 단일 머신에서의 상한 추정치"와 "어떤 자원이 먼저 포화되는지"를 찾는 것이다. 운영 반영 수치는 반드시 Safety Factor를 적용한다.

## 현재 프로젝트 기준 가정

- Backend: Spring Boot, 기본 포트 `8080`
- DB: MySQL, dev 기본 DB `personal_project`, 기본 포트 `3306`
- Redis: dev 기본 포트 `6380`
- HikariCP dev 기본값: `maximum-pool-size=16`, `minimum-idle=16`
- Actuator Prometheus: `/actuator/prometheus`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Redis exporter: `http://localhost:9121`
- Prometheus scrape 설정:
  - Spring Boot: `host.docker.internal:8080/actuator/prometheus`
  - Redis exporter: `redis-exporter:9121`

## 테스트 대상 API와 요청 비율

권장 혼합 시나리오는 실제 쇼핑 트래픽에 가까운 읽기 중심 워크로드다.

| API                                                 | 비율 | 목적                         | 병목 후보                         |
| --------------------------------------------------- | ---: | ---------------------------- | --------------------------------- |
| `GET /api/products/list?page=1&size=20&count=false` |  50% | 상품 목록 조회               | DB 인덱스, 정렬, JPA 변환         |
| `GET /api/products/{pno}`                           |  25% | 상품 상세 조회               | DB 단건 조회, JWT 검증            |
| `GET /api/cart/items`                               |  10% | 장바구니 조회                | DB join                           |
| `POST /api/cart/change`                             |  10% | 장바구니 추가/수량 변경      | DB write, transaction             |
| `POST /api/payments/prepare`                        |   5% | 주문 스냅샷 생성, Redis lock | Redis lock, DB write, Hikari pool |

`/api/payments/complete`는 PortOne 외부 API 조회와 결제 검증이 포함되므로 로컬 순수 시스템 한계 측정에서는 제외한다. 즉 결제 완료 성능은 외부 API mock 또는 staging에서 별도 측정한다.

## 격리된 테스트 데이터 전략

가장 안전한 방식은 테스트 전용 schema를 쓰는 것이다.

1. `personal_project_loadtest` schema를 만든다.
2. 백엔드를 `DEV_DB_NAME=personal_project_loadtest`로 실행한다.
3. Spring Boot dev의 `ddl-auto=update`로 테이블을 생성한다.
4. `k6/sql/seed-load-test-data.sql`로 테스트 사용자와 상품을 넣는다.
5. 테스트 종료 후 `DROP DATABASE personal_project_loadtest` 또는 `k6/sql/cleanup-load-test-data.sql`로 정리한다.

테스트 데이터 식별 규칙

- 사용자: `lt_local_user_0001@load.local`
- 사용자 비밀번호: `loadtest1234!`
- 상품명: `[LOADTEST:lt_local] product 0001`
- 상품 이미지 row: `lt_local_product_0001.png`
- 주문/장바구니 정리 기준: `tbl_order.member_id`, `tbl_cart.member_owner`가 `lt_local_user_%@load.local`

정리 전략 선택

| 방식                 | 판단                                                                                      |
| -------------------- | ----------------------------------------------------------------------------------------- |
| 테스트 전용 Schema   | 권장. 로컬 운영 유사 데이터 오염 방지에 가장 명확하다.                                    |
| Transaction Rollback | 비권장. k6 HTTP 요청은 실제 commit 단위라 전체 테스트를 하나의 rollback으로 감쌀 수 없다. |
| Truncate             | 공유 schema에서는 위험하다. 테스트 전용 schema에서만 허용한다.                            |
| Soft Delete          | 비권장. 데이터가 남아 통계, 인덱스, 쿼리 플랜에 영향을 준다.                              |
| Prefix 기반 Delete   | schema 분리가 불가능할 때의 fallback이다. 반드시 `lt_` prefix로만 삭제한다.               |

## 실행 절차

### 1. 모니터링 스택 실행

```powershell
docker compose -f infra\redis-monitoring\docker-compose.yml up -d
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### 2. 테스트 전용 DB 준비

MySQL client 또는 Workbench에서 실행한다.

```sql
CREATE DATABASE IF NOT EXISTS personal_project_loadtest
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

백엔드를 한 번 실행해 테이블을 만든다.

```powershell
cd backend\personal.project
$env:DEV_DB_NAME="personal_project_loadtest"
$env:DEV_DB_MAX_POOL_SIZE="16"
$env:DEV_DB_MIN_IDLE="16"
$env:DEV_REDIS_PORT="6380"
$env:LOGGING_LEVEL_ORG_HIBERNATE_SQL="WARN"
$env:LOGGING_LEVEL_ORG_HIBERNATE_ORM_JDBC_BIND="WARN"
.\gradlew.bat bootRun
```

테이블 생성 후 seed SQL을 적용한다.

```powershell
# 예시: mysql client가 있을 때
mysql -upeeljun -p personal_project_loadtest < k6\sql\seed-load-test-data.sql
```

### 3. k6 실행

```powershell
New-Item -ItemType Directory -Force k6\results

k6 run `
  --summary-export k6\results\summary-export.json `
  --out json=k6\results\raw.ndjson `
  -e BASE_URL=http://localhost:8080 `
  -e USER_COUNT=100 `
  -e USER_EMAIL_PREFIX=lt_local_user_ `
  -e USER_DOMAIN=load.local `
  -e PASSWORD=loadtest1234! `
  k6\load-test.js
```

빠른 smoke

```powershell
k6 run `
  -e WARMUP_DURATION=20s -e WARMUP_VUS=5 `
  -e RAMP1_DURATION=20s -e RAMP1_VUS=10 `
  -e RAMP2_DURATION=20s -e RAMP2_VUS=10 `
  -e PEAK_DURATION=20s -e PEAK_VUS=10 `
  -e STRESS_DURATION=20s -e STRESS_VUS=15 `
  -e USER_COUNT=20 `
  k6\load-test.js
```

## 권장 테스트 시간과 VU 설정

로컬 머신에서는 너무 짧은 테스트가 JIT warm-up, DB buffer pool warm-up, connection pool 안정화 효과를 놓친다.

| 단계        | 권장 시간 | VU 예시 | 목적                                        |
| ----------- | --------: | ------: | ------------------------------------------- |
| Warm-up     |       5분 |      20 | JVM JIT, DB buffer, Redis connection 안정화 |
| 점진 증가 1 |       5분 |      50 | 낮은 부하 기준선 확보                       |
| 점진 증가 2 |       5분 |     100 | latency 증가 곡선 확인                      |
| Peak        |      10분 |     150 | 안정적으로 유지 가능한 피크 확인            |
| Stress      |      10분 |     220 | 의도적으로 포화 지점 탐색                   |
| Cooldown    |       5분 |       0 | 회복 시간, pending connection 해소 확인     |

## 로컬 리소스 경합 최소화

- k6, 백엔드, DB, Redis가 같은 머신을 공유하므로 결과를 운영 보장 수치로 해석하지 않는다.
- 프론트 dev server, IDE indexing, 대용량 다운로드, 백신 전체 검사처럼 CPU와 I/O를 쓰는 작업을 중지한다.
- Hibernate SQL DEBUG/TRACE 로그는 부하 테스트 중 `WARN` 이상으로 낮춘다. 현재 dev 설정은 SQL DEBUG와 bind TRACE라서 그대로 두면 로그 I/O가 병목처럼 보일 수 있다.
- Prometheus scrape interval은 현재 `15s`를 유지한다. 1s 수준으로 낮추면 모니터링 자체가 부하가 된다.
- Grafana 대시보드는 테스트 중 여러 패널을 계속 새로고침하지 않는다. 측정 중에는 Prometheus query나 Grafana refresh를 15s 이상으로 둔다.
- 가능하면 k6의 `USER_COUNT`를 최대 VU 이상으로 둔다. 같은 회원으로 `/api/payments/prepare`가 몰리면 Redis member lock 때문에 실제보다 낮은 TPS가 나온다.
- DB connection pool은 처음에는 현재 dev 기본값 `16`으로 측정하고, 다음 회차에서 `8`, `32`를 비교해 pool 병목 여부를 확인한다.
- Windows라면 작업 관리자에서 `java.exe`, `k6.exe`, Docker Desktop CPU를 같이 본다. Linux라면 `htop`에서 프로세스별 CPU를 분리해 본다.

## 모니터링 명령어

Windows PowerShell

```powershell
Get-Process java,k6,com.docker.backend -ErrorAction SilentlyContinue |
  Select-Object ProcessName,Id,CPU,WorkingSet64,PrivateMemorySize64

Get-Counter '\Processor(_Total)\% Processor Time',
            '\Memory\Available MBytes',
            '\PhysicalDisk(_Total)\Avg. Disk sec/Read',
            '\PhysicalDisk(_Total)\Avg. Disk sec/Write'

jps -l
jstat -gcutil <java-pid> 5s
jcmd <java-pid> Thread.print
jcmd <java-pid> GC.heap_info
```

Docker/Redis

```powershell
docker stats
docker logs redis_exporter --tail 100
redis-cli -p 6380 INFO stats
redis-cli -p 6380 INFO clients
redis-cli -p 6380 INFO memory
redis-cli -p 6380 SLOWLOG GET 10
```

MySQL

```sql
SHOW FULL PROCESSLIST;
SHOW GLOBAL STATUS LIKE 'Threads_connected';
SHOW GLOBAL STATUS LIKE 'Threads_running';
SHOW GLOBAL STATUS LIKE 'Created_tmp%';
SHOW GLOBAL STATUS LIKE 'Innodb_row_lock%';
SHOW ENGINE INNODB STATUS\G
```

Linux/macOS 참고

```bash
top
htop
vmstat 1
iostat -xz 1
pidstat -p <java-pid> 1
```

## Prometheus 핵심 Query

Spring HTTP

```promql
rate(http_server_requests_seconds_count[1m])
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, uri, method, status))
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[1m])) by (le, uri, method, status))
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) / sum(rate(http_server_requests_seconds_count[1m]))
```

JVM/Thread/CPU

```promql
process_cpu_usage
system_cpu_usage
jvm_memory_used_bytes
jvm_threads_live_threads
jvm_threads_peak_threads
```

HikariCP

```promql
hikaricp_connections_active
hikaricp_connections_idle
hikaricp_connections_pending
hikaricp_connections_max
hikaricp_connections_timeout_total
```

Redis

```promql
rate(redis_commands_processed_total[1m])
redis_connected_clients
redis_memory_used_bytes
rate(redis_keyspace_hits_total[1m])
rate(redis_keyspace_misses_total[1m])
```

## 병목 예측 가이드

현재 코드와 설정 기준으로 가장 가능성이 높은 병목 순서는 다음과 같다.

1. DB 또는 HikariCP connection pool
   - 상품 목록 조회는 `tbl_product`와 `product_image_list` join 및 정렬을 수행한다.
   - 장바구니/결제 준비는 transaction과 write가 섞인다.
   - Hikari max가 16이라 VU가 증가하면 active가 max에 붙고 pending이 생길 수 있다.
2. 애플리케이션 CPU/JVM thread
   - JWT 검증, JPA entity/DTO 변환, JSON 직렬화 비용이 증가한다.
   - dev SQL DEBUG/TRACE 로그가 켜져 있으면 CPU보다 로그 I/O가 먼저 튈 수 있다.
3. Redis lock
   - `/api/payments/prepare`는 `payment:prepare:member:{email}` 락을 잡는다.
   - 테스트 사용자가 부족하면 같은 회원 락에 몰려 Redis lock wait가 병목처럼 보인다.
4. Memory
   - 짧은 로컬 테스트에서는 메모리가 첫 병목일 가능성은 낮다.
   - 다만 p99 spike와 Full GC가 같이 나오면 heap 또는 allocation 문제로 본다.

판단 기준

| 관측                                                         | 해석                                  |
| ------------------------------------------------------------ | ------------------------------------- |
| CPU 80~90% 이상, Hikari pending 낮음                         | 애플리케이션 CPU 병목 가능성          |
| CPU 여유, Hikari active=max, pending 증가                    | DB connection pool 또는 DB query 병목 |
| Threads_running 증가, slow query 증가                        | DB 내부 병목                          |
| Redis ops/sec 급증, slowlog 발생, payment_prepare p95만 악화 | Redis lock 또는 Redis 병목            |
| p99 spike와 GC pause 동시 발생                               | JVM memory/GC 병목                    |
| VU 증가에도 TPS 정체, p95/p99 급증                           | Saturation Point 도달                 |

## 부하 테스트 결과

조건

- k6 max VU: 120
- 사용자: 160명
- 상품: 상품 30개
- 요청 비율: 상품 목록 50%, 상품 상세 25%, 장바구니 조회 10%, 장바구니 변경 10%, 결제 준비 5%
- Hikari max pool: 16

k6 결과

| 지표                   |                        값 |
| ---------------------- | ------------------------: |
| Total requests         |                    18,390 |
| Average TPS            |              138.50 req/s |
| Average latency        |                 103.35 ms |
| p95 latency            |                 280.39 ms |
| p99 latency            |                 363.71 ms |
| Max latency            |                 645.71 ms |
| Error rate             |                        0% |
| 50% Safety 일일 요청량 | 약 5,983,289 requests/day |

API별 p95

| API tag           |       p95 |
| ----------------- | --------: |
| `product_list`    | 318.69 ms |
| `product_detail`  | 168.17 ms |
| `cart_change`     | 180.93 ms |
| `payment_prepare` | 208.22 ms |

Prometheus 관측

| 지표                                                              |           값 |
| ----------------------------------------------------------------- | -----------: |
| `max_over_time(hikaricp_connections_active[5m])`                  |           16 |
| `max_over_time(hikaricp_connections_pending[5m])`                 |           24 |
| `increase(hikaricp_connections_timeout_total[5m])`                |            0 |
| `max_over_time(process_cpu_usage[5m])`                            |        0.109 |
| `max_over_time(jvm_threads_live_threads[5m])`                     |          139 |
| `max_over_time(rate(redis_commands_processed_total[1m])[5m:15s])` | 136.17 ops/s |
| `max_over_time(redis_connected_clients[5m])`                      |            8 |

해석

- 이 probe에서는 HTTP 에러와 latency 기준 Saturation Point는 아직 도달하지 않았다.
- 그러나 Hikari active connection이 max 16까지 도달했고 pending connection이 최대 24까지 관측됐다.
- CPU는 약 10.9% 수준으로 여유가 있었고 Redis 지표도 낮았다.
- 따라서 현재 관측된 첫 병목 후보는 CPU, Memory, Redis보다 DB connection pool 또는 DB query 처리량이다.

## Saturation Point 탐지 방법

Saturation Point는 단순히 에러가 나는 시점이 아니다. 아래 조건 중 2개 이상이 동시에 나타나는 첫 구간을 포화 지점으로 본다.

- VU를 올렸는데 TPS가 더 이상 비례 증가하지 않는다.
- p95가 baseline 대비 2배 이상 증가한다.
- p99가 p95보다 크게 벌어지고 spike가 반복된다.
- `http_req_failed`가 1%를 초과한다.
- Hikari pending connection이 0보다 커진다.
- CPU가 85% 이상으로 붙거나 GC pause가 증가한다.
- Redis slowlog 또는 DB slow query가 생긴다.

안정 TPS는 Saturation 직전 단계에서 10분 이상 유지된 `http_reqs/s * (1 - error_rate)`로 잡는다.

## 일일 처리 가능량 계산식

가장 보편적으로 쓰는 계산식만 사용한다.

```text
성공 TPS = 측정 TPS * (1 - Error Rate)

일일 요청 처리량 = 성공 TPS * 86,400초 * Safety Factor

일일 주문 처리량 = 성공 TPS * 주문성 API 비율 * 86,400초 * Safety Factor

권장 안정 TPS = Saturation 직전 구간의 성공 TPS

동시성 추정(Little's Law) = TPS * 평균 응답시간(초)

폐쇄형 VU 모델 근사
TPS ~= VU / (평균 응답시간 + Think Time)

DAU 추정 = 일일 요청 처리량 / 사용자 1명당 일 평균 요청 수

주문 기준 DAU 추정 = 일일 주문 처리량 / 사용자 1명당 일 평균 주문 수

MAU 추정 = DAU / DAU_MAU_Ratio
```

예시

```text
측정 TPS = 120 req/s
Error Rate = 0.5% = 0.005
Safety Factor = 50% = 0.5
주문성 API 비율 = 5% = 0.05

성공 TPS = 120 * (1 - 0.005) = 119.4 req/s
일일 요청 처리량 = 119.4 * 86400 * 0.5 = 5,158,080 requests/day
일일 주문 준비 처리량 = 119.4 * 0.05 * 86400 * 0.5 = 257,904 orders/day
```

Safety Factor 선택

- 70%: 로컬과 운영 사양이 유사하고 p95/p99가 안정적일 때
- 50%: 일반적인 보수 추정
- 30%: 로컬 리소스 공유, DB 차이, p99 spike, 운영 불확실성이 클 때

## 로컬 테스트 한계

- 실제 네트워크 지연이 반영되지 않는다.
- k6 클라이언트와 서버가 같은 CPU를 공유할 수 있다.
- Docker 컨테이너 간 리소스 경합이 발생할 수 있다.
- 로컬 DB와 애플리케이션이 같은 머신에서 실행될 수 있다.
- 디스크 I/O 특성이 운영 환경과 다르다.
- 실제 다중 인스턴스 트래픽 분산이 반영되지 않는다.
- 운영 규모의 connection pool 동작과 차이가 있다.
- 실제 동시성 상황을 완전히 재현하기 어렵다.

운영 성능 보정 가이드

- 로컬 TPS는 보장 수치가 아니라 상한 추정치로 취급한다.
- 보수적인 Safety Factor 30%~70%를 적용한다.
- 평균 응답 시간보다 p95, p99를 더 중요하게 해석한다.
- 운영 DB, 네트워크, 인스턴스 CPU/Memory, disk IOPS, connection pool을 별도로 고려한다.
- 배포 전 staging 또는 cloud-like 환경에서 같은 k6 스크립트로 재검증한다.
- 운영이 다중 인스턴스라면 sticky session, Redis, DB pool 총합, LB timeout을 별도 검증한다.

## 테스트 후 체크리스트

- [ ] k6 summary에서 `http_reqs/s`, `http_req_failed`, avg, p95, p99를 기록했다.
- [ ] Saturation 직전의 안정 TPS를 산정했다.
- [ ] p95/p99가 급격히 늘어나는 첫 VU 구간을 표시했다.
- [ ] Hikari active/max/pending을 확인했다.
- [ ] CPU, memory, JVM GC, thread 수를 확인했다.
- [ ] Redis ops/sec, connected clients, slowlog를 확인했다.
- [ ] MySQL processlist, Threads_running, row lock, slow query를 확인했다.
- [ ] 일일 요청량, 일일 주문량, DAU/MAU 역산식을 적용했다.
- [ ] 테스트 전용 schema를 drop하거나 cleanup SQL을 실행했다.
- [ ] 결과 파일 `k6/results/summary-compact.json`, `summary-full.json`, `raw.ndjson`를 보관했다.

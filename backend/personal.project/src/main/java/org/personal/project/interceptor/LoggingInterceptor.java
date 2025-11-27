//package org.personal.project.interceptor;
//
//import java.util.UUID;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.slf4j.MDC;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//public class LoggingInterceptor implements HandlerInterceptor {
//
//    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);
//    private static final String DEVICE_ID_HEADER = "Device-Id"; // 클라이언트가 보낼 헤더 이름
//    private static final String DEVICE_ID_KEY = "device_id"; // MDC에 저장할 목록
//    private static final String IP_KEY = "client_ip";
//    private static final String TRACE_KEY = "traceId"; // 해당 필드는 micrometer-tracing-bridge-brave 라이브러리가 알아서 관리해서 나중에 삭제할 필요 없음
//
//    // 컨트롤러 실행 전: 요청 정보 로깅 및 MDC 설정
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        // MDC에 Device ID 설정
//        String deviceId = request.getHeader(DEVICE_ID_HEADER);
//        if (deviceId == null || deviceId.isEmpty()) {
//            // 헤더에 없을 경우 임시 ID 생성 (운영 시에는 필수 헤더로 설정하는 것이 좋음)
//            deviceId = "GUEST-" + UUID.randomUUID().toString().substring(0, 8);
//        }
//        MDC.put(DEVICE_ID_KEY, deviceId);
//
//        String clientIp = getClientIp(request);
//        MDC.put(IP_KEY, clientIp);
//
//        String traceId = MDC.get(TRACE_KEY);
//
//        log.debug("methode: {} url: {} remoteAddress: {} traceId: {} (Device: {})",
//                request.getMethod(), request.getRequestURI(), clientIp, traceId, deviceId);
//
//        return true;
//    }
//
//    // 컨트롤러 실행 후: 응답 정보 로깅
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        // 컨트롤러에서 예외 없이 정상적으로 처리되었을 때의 응답 로깅
//        log.debug("요청 완료: [{}] {} Status: {}",
//                request.getMethod(), request.getRequestURI(), response.getStatus());
//    }
//
//    // 모든 작업 완료 후: MDC 정리
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        MDC.remove(DEVICE_ID_KEY);
//        MDC.remove(IP_KEY);
////        log.debug("MDC 정리 및 요청 처리 종료: {}", request.getRequestURI());
//    }
//
//    // IP 주소 추출 유틸리티 메서드
//    private String getClientIp(HttpServletRequest request) {
//        String ip = request.getHeader("X-Forwarded-For");
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        // ... (다른 헤더 체크 로직 생략 가능)
//        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr(); // 기본 IP 주소
//        }
//        // 여러 IP가 콤마로 구분되어 있을 경우, 첫 번째 IP를 사용
//        if (ip != null && ip.contains(",")) {
//            ip = ip.split(",")[0].trim();
//        }
//        return ip != null ? ip : "-";
//    }
//}
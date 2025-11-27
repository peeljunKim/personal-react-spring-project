package org.personal.project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String DEVICE_ID_HEADER = "Device-Id"; // 클라이언트에서 보내는 헤더 이름
    private static final String DEVICE_ID_KEY = "device_id"; // MDC에 저장할 목록
    private static final String IP_KEY = "client_ip";
    private static final String TRACE_KEY = "traceId"; // 해당 필드는 micrometer-tracing-bridge-brave 라이브러리가 알아서 관리해서 나중에 삭제할 필요 없음

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        if (deviceId == null || deviceId.isEmpty()) {
            // 헤더에 없을 경우 임시 ID 생성 (운영 시에는 필수 헤더로 설정하는 것이 좋음)
            deviceId = "GUEST-" + UUID.randomUUID().toString().substring(0, 8);
        }

        String clientIp = getClientIp(request); // ip 추출

        MDC.put(DEVICE_ID_KEY, deviceId);
        MDC.put(IP_KEY, clientIp);

        String traceId = MDC.get(TRACE_KEY); // micrometer-tracing-bridge-brave 가 알아서 넣어줌

        long startTime = System.currentTimeMillis();

        try {
            // 요청 시작 로그
            log.info("요청 시작: method: {} url: {} remoteAddress: {} traceId: {} (Device: {})",
                    request.getMethod(),
                    request.getRequestURI(),
                    clientIp,
                    traceId,
                    deviceId
            );

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("요청 처리 중 예외 발생: [{}] {} traceId: {} (Device: {})",
                    request.getMethod(), request.getRequestURI(), traceId, deviceId, ex);
            throw ex; // 꼭 다시 던져야 기존 예외 처리 흐름이 유지됨
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;

            // 응답 로그
            log.info("요청 완료: [{}] {} Status: {} duration: {} ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    timeTaken
            );

            MDC.remove(DEVICE_ID_KEY);
            MDC.remove(IP_KEY);
        }
    }

    // 필요 시 특정 요청은 필터를 타지 않게 할 수 있음
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        return uri.startsWith("/css")
                || uri.startsWith("/js")
                || uri.startsWith("/images")
                || uri.startsWith("/actuator/health")
                || uri.startsWith("/favicon.ico");
    }

    // IP 주소 추출 유틸
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "-";
    }
}

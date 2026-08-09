package org.personal.project.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** HTTP 요청 시작부터 응답 완료까지 공통 로그와 MDC 생명주기 관리 */
@Slf4j
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final int MAX_EXTERNAL_VALUE_LENGTH = 100;

    private final TraceContextAccessor traceContextAccessor;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        Throwable failure = null;

        ApplicationMdc.put(ApplicationMdc.DEVICE_ID, sanitize(request.getHeader(DEVICE_ID_HEADER)));
        ApplicationMdc.put(ApplicationMdc.CLIENT_IP, sanitize(request.getRemoteAddr()));
        ApplicationMdc.put(ApplicationMdc.HTTP_METHOD, request.getMethod());
        ApplicationMdc.put(ApplicationMdc.URI, sanitize(request.getRequestURI()));

        // 클라이언트 장애 문의에 활용할 현재 서버 Trace ID 제공
        traceContextAccessor.currentTraceId()
                .ifPresent(traceId -> response.setHeader(TRACE_ID_HEADER, traceId));

        // 전체 유입량과 처리 흐름 확인을 위한 요청 시작 INFO 로그
        log.info("http.request.started");

        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failure = exception;
            ApplicationMdc.put(ApplicationMdc.EXCEPTION, exception.getClass().getSimpleName());
            throw exception;
        } finally {
            int responseStatus = response.getStatus();
            // 응답 상태 결정 전에 예외가 전파된 경우 로그상 결과를 500으로 보정
            if (failure != null && responseStatus < HttpServletResponse.SC_BAD_REQUEST) {
                responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            ApplicationMdc.put(ApplicationMdc.RESPONSE_STATUS, Integer.toString(responseStatus));
            ApplicationMdc.put(ApplicationMdc.DURATION_MS, Long.toString(durationMs));

            if (failure == null) {
                log.info("http.request.completed");
            } else {
                // 실패 원인과 stack trace 보존을 위한 ERROR 로그
                log.error("http.request.failed", failure);
            }

            // 스레드 재사용 시 다음 요청으로의 MDC 누수 방지
            ApplicationMdc.removeApplicationKeys();
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        // 외부 입력의 로그 인젝션과 과도한 로그 크기 제한
        String sanitized = value.replace("\r", "").replace("\n", "");
        return sanitized.substring(0, Math.min(sanitized.length(), MAX_EXTERNAL_VALUE_LENGTH));
    }
}

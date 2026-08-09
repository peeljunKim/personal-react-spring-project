package org.personal.project.logging;

import org.slf4j.MDC;

import java.util.List;

/**
 * 요청 로그에 사용할 애플리케이션 MDC 키 관리
 * Micrometer 관리 대상인 traceId와 spanId는 목록에서 제외
 */
public final class ApplicationMdc {

    public static final String USER_ID = "userId";
    public static final String DEVICE_ID = "deviceId";
    public static final String CLIENT_IP = "clientIp";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String URI = "uri";
    public static final String CONTROLLER = "controller";
    public static final String HANDLER_METHOD = "handlerMethod";
    public static final String RESPONSE_STATUS = "responseStatus";
    public static final String DURATION_MS = "durationMs";
    public static final String EXCEPTION = "exception";

    private static final List<String> APPLICATION_KEYS = List.of(
            USER_ID,
            DEVICE_ID,
            CLIENT_IP,
            HTTP_METHOD,
            URI,
            CONTROLLER,
            HANDLER_METHOD,
            RESPONSE_STATUS,
            DURATION_MS,
            EXCEPTION
    );

    private ApplicationMdc() {
    }

    public static void put(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    // Micrometer의 Trace MDC는 보존하고 애플리케이션이 추가한 값만 정리
    public static void removeApplicationKeys() {
        APPLICATION_KEYS.forEach(MDC::remove);
    }
}

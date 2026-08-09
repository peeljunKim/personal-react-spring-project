package org.personal.project.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Micrometer가 관리하는 현재 Trace Context 제공 */
@Component
@RequiredArgsConstructor
public class TraceContextAccessor {

    private final Tracer tracer;

    public Optional<String> currentTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            // 별도 Trace ID 생성 없이 Observation이 없는 경우 빈 값 반환
            return Optional.empty();
        }
        return Optional.of(currentSpan.context().traceId());
    }
}

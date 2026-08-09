package org.personal.project.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** MVC 핸들러와 인증 사용자 정보를 요청 MDC에 추가 */
@Component
public class HandlerLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            ApplicationMdc.put(ApplicationMdc.CONTROLLER, handlerMethod.getBeanType().getSimpleName());
            ApplicationMdc.put(ApplicationMdc.HANDLER_METHOD, handlerMethod.getMethod().getName());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Security 인증 완료 후 실제 사용자만 요청 로그에 포함
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            ApplicationMdc.put(ApplicationMdc.USER_ID, authentication.getName());
        }

        return true;
    }
}

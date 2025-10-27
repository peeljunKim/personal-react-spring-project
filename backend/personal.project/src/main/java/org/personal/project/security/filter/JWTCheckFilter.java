package org.personal.project.security.filter;

import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.personal.project.dto.member.MemberDTO;
import org.personal.project.util.JWTUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {

    /**
     * http(api) 요청에 대해 필터의 실행 여부
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("check uri = {}", path);

        // Preflight 요청은 체크하지 않음
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        //api/member/ 경로의 호출은 체크하지 않음
        if (path.startsWith("/api/member/")) {
            return true;
        }

        //api/oauth/ 경로의 호출은 체크하지 않음 - oauth2.0 관련
        if (path.startsWith("/api/oauth/")) {
            return true;
        }

        //이미지 조회 경로는 체크하지 않음
        if (path.startsWith("/api/products/view/")) {
            return true;
        }

        if (path.startsWith("/jackson")) {
            return true;
        }

        if (path.startsWith("/actuator") || path.startsWith("/actuator/")) return true;

        if (path.startsWith("/favicon.ico")) return true;

        return false;
    }

    /**
     * JWT 토큰을 검증하고 인증 정보를 설정
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//        log.info("-----------------JWTCheckFilter.................");
        String authHeaderStr = request.getHeader("Authorization");

        try {
            //Bearer accestoken
//            log.info("authHeaderStr = {}", authHeaderStr);
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);

//            log.info("JWT claims: " + claims);

            String email = (String) claims.get("email");
            String pw = (String) claims.get("pw");
            String nickname = (String) claims.get("nickname");
            Boolean social = (Boolean) claims.get("social");
            List<String> roleNames = (List<String>) claims.get("roleNames");

            MemberDTO memberDTO = new MemberDTO(email, pw, nickname, social.booleanValue(),
                    roleNames);

//            log.info("-----------------------------------");
//            log.info(memberDTO);
//            log.info(memberDTO.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(memberDTO, pw, memberDTO.getAuthorities());

            /**
             * SecurityContextHolder를 사용하는 방식은 무상태인 것처럼 보이지만, 매 요청마다 상태를 재생성한다는 단점을 가지고 있습니다
             *
             * SecurityContextHolder는 서버의 메모리(스레드 로컬)에 현재 요청을 보낸
             * 사용자의 인증 정보(authenticationToken)를 저장하는 역할을 합니다.
             * 즉, 요청이 처리되는 동안만 사용자의 상태를 임시로 저장한 뒤 사용한 후 바로 삭제
             *
             * 이 과정이 없으면 @PreAuthorize와 같은 스프링 시큐리티의 고급 기능들을 제대로 활용할 수 없게 됩니다
             *
             * 1. 클라이언트가 보낸 JWT 토큰을 검증합니다.
             *
             * 2. 토큰이 유효하면, 토큰의 정보를 기반으로 사용자의 인증 객체(authenticationToken)를 만듭니다.
             *
             * 3. 이 인증 객체를 SecurityContextHolder에 저장합니다.
             *
             * 4.컨트롤러나 서비스 계층의 @PreAuthorize 같은 어노테이션이 SecurityContextHolder에 저장된 정보를 사용하여 권한을 확인합니다.
             *
             * 5. 요청 처리가 끝나면 SecurityContextHolder의 정보는 초기화됩니다.
             */
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT Check Error");
            log.error(e.getMessage());

            Gson gson = new Gson();
            String msg = gson.toJson(Map.of("error", "ERROR_ACCESS_TOKEN"));

            response.setContentType("application/json");

            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }

    }
}

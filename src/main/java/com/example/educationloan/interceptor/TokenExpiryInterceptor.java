package com.example.educationloan.interceptor;
import com.example.educationloan.config.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.util.Date;
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenExpiryInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private static final long TOKEN_EXPIRY_WARNING_MS = 10 * 60 * 1000;


    private static final String[] PUBLIC_URLS = {
                              "/api/v1/auth/login",
                              "/api/v1/auth/register",
                               "/api/v1/auth/refresh"
              };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        for (String publicUrl : PUBLIC_URLS) {
            if (uri.startsWith(publicUrl)) {
                return true;
            }
        }

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return true;
        }

        try {
            Date expiry  = jwtTokenProvider.extractExpiration(token);
            long timeLeft = expiry.getTime() - System.currentTimeMillis();
            if (timeLeft <= 0) {
                log.warn("Expired token used for URL: {}", uri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Token has expired. Please login again.\",\"data\":null}");
                return false;
            }
            if (timeLeft <= TOKEN_EXPIRY_WARNING_MS) {
                log.info("Token expiring soon for URL: {} | Time left: {}s", uri, timeLeft / 1000);
                response.setHeader("X-Token-Expiring-Soon", "true");
                response.setHeader("X-Token-Expires-In", String.valueOf(timeLeft / 1000) + "s");
            }
        } catch (Exception e) {
            log.error("Token expiry check failed: {}", e.getMessage());
        }
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}

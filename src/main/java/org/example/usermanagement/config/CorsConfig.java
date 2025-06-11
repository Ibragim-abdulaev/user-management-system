package org.example.usermanagement.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig implements Filter {

    private final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Логирование входящего запроса
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String path = request.getRequestURI();

        logger.debug("CORS request from [{}] {} {}", origin, method, path);

        // Устанавливаем глобальные CORS заголовки
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Disposition");
        response.setHeader("Access-Control-Max-Age", "3600");

        // Важно: если вы используйте wildcard "*" для Allow-Origin,
        // то нельзя устанавливать Allow-Credentials в true
        // response.setHeader("Access-Control-Allow-Credentials", "true");

        // Обрабатываем preflight запросы
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.debug("Handling OPTIONS preflight request");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Продолжаем обработку для других запросов
        logger.debug("Continuing filter chain");
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("GlobalCorsFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("GlobalCorsFilter destroyed");
    }
}
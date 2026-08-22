package com.xadrez.chess_backend.config;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isProtectedRoute(request) || isAllowed(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Limite de requisições excedido. Tente novamente em breve.\"}");
    }

    private boolean isProtectedRoute(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && ("/users".equals(request.getRequestURI()) || request.getRequestURI().matches("/games/[^/]+/moves"));
    }

    private boolean isAllowed(HttpServletRequest request) {
        String key = request.getRemoteAddr() + ':' + request.getRequestURI();
        Instant now = Instant.now();
        Window window = windows.compute(key, (ignored, current) -> current == null || current.startedAt().plus(Duration.ofMinutes(1)).isBefore(now)
                ? new Window(now, 1) : new Window(current.startedAt(), current.requests() + 1));
        return window.requests() <= MAX_REQUESTS_PER_MINUTE;
    }

    private record Window(Instant startedAt, int requests) { }
}

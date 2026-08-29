package com.velocitymotors.carbooking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class TimingHandlerInterceptor implements HandlerInterceptor {

    private static final String START_NANOS_ATTRIBUTE = "startNanos";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_NANOS_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        long startNanos = (long) request.getAttribute(START_NANOS_ATTRIBUTE);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
        log.info("Request {} {} took {} ms", request.getMethod(), request.getRequestURI(), elapsedMillis);
    }
}

package com.velocitymotors.carbooking.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class TimingHandlerInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TimingHandlerInterceptor.class);
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
        logger.info("Request {} {} took {} ms", request.getMethod(), request.getRequestURI(), elapsedMillis);
    }
}

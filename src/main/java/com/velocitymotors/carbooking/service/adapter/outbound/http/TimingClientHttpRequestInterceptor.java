package com.velocitymotors.carbooking.service.adapter.outbound.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class TimingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TimingClientHttpRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long startNanos = System.nanoTime();
        try {
            return execution.execute(request, body);
        } finally {
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            logger.info("REST call {} {} took {} ms", request.getMethod(), request.getURI(), elapsedMillis);
        }
    }
}

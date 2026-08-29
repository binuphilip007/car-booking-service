package com.velocitymotors.carbooking.service.adapter.outbound.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class TimingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        long startNanos = System.nanoTime();
        try {
            return execution.execute(request, body);
        } finally {
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            log.debug("REST call {} {} took {} ms", request.getMethod(), request.getURI(), elapsedMillis);
        }
    }
}

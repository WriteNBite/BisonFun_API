package com.writenbite.bisonfun.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.GraphqlErrorHelper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    final
    Supplier<BucketConfiguration> bucketConfiguration;
    final ProxyManager<String> proxyManager;
    final ObjectMapper objectMapper;

    public RateLimitFilter(Supplier<BucketConfiguration> bucketConfiguration, ProxyManager<String> proxyManager, ObjectMapper objectMapper) {
        this.bucketConfiguration = bucketConfiguration;
        this.proxyManager = proxyManager;
        this.objectMapper = objectMapper;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String userKey = request.getRemoteAddr();
        Bucket bucket = proxyManager.builder().build(userKey, bucketConfiguration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            TooManyRequestsException exception = new TooManyRequestsException(TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
            response.setContentType("application/json");
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(exception.getSeconds()));
            response.setStatus(429);
            GraphQLError error = GraphqlErrorBuilder.newError()
                    .message(exception.getMessage())
                    .errorType(ErrorType.FORBIDDEN)
                    .build();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errors", List.of(GraphqlErrorHelper.toSpecification(error)));
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}

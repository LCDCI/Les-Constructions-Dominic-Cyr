package com.ecp.les_constructions_dominic_cyr.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InquiriesRateLimitFilterTest {

    private InquiriesRateLimitFilter filter;
    private Cache<String, Bucket> mockCache;
    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        mockCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(10_000)
                .build();
        filter = new InquiriesRateLimitFilter(mockCache);
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    void shouldApplyRateLimitingToPostInquiries() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/inquiries");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // First 5 requests should pass
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, mockFilterChain);
            assertEquals(200, response.getStatus(), "Request " + (i + 1) + " should succeed");
        }

        // 6th request should be rate limited
        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, mockFilterChain);
        assertEquals(429, rateLimitedResponse.getStatus(), "6th request should be rate limited");
        assertTrue(rateLimitedResponse.getContentAsString().contains("Too many requests"));
    }

    @Test
    void shouldNotApplyRateLimitingToNonInquiryEndpoints() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/other-endpoint");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(request, response);
        assertNotEquals(429, response.getStatus());
    }

    @Test
    void shouldNotApplyRateLimitingToGetInquiries() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/inquiries");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(request, response);
        assertNotEquals(429, response.getStatus());
    }

    @Test
    void shouldExtractIpFromXForwardedForHeader() throws ServletException, IOException {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setMethod("POST");
        request1.setRequestURI("/api/inquiries");
        request1.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        MockHttpServletResponse response1 = new MockHttpServletResponse();

        // Exhaust rate limit for IP 10.0.0.1
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request1, response1, mockFilterChain);
        }

        // 6th request from same forwarded IP should be rate limited
        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request1, rateLimitedResponse, mockFilterChain);
        assertEquals(429, rateLimitedResponse.getStatus());

        // Request from different IP should succeed
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("POST");
        request2.setRequestURI("/api/inquiries");
        request2.addHeader("X-Forwarded-For", "10.0.0.2, 192.168.1.1");
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilterInternal(request2, response2, mockFilterChain);
        assertEquals(200, response2.getStatus());
    }

    @Test
    void shouldUseRemoteAddrWhenXForwardedForIsNotPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/inquiries");
        request.setRemoteAddr("192.168.1.100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Exhaust rate limit
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, mockFilterChain);
        }

        // 6th request should be rate limited
        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, mockFilterChain);
        assertEquals(429, rateLimitedResponse.getStatus());
    }

    @Test
    void shouldReturn429WithJsonErrorResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/inquiries");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Exhaust rate limit
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, mockFilterChain);
        }

        // 6th request should return JSON error
        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, rateLimitedResponse, mockFilterChain);

        assertEquals(429, rateLimitedResponse.getStatus());
        assertEquals("application/json", rateLimitedResponse.getContentType());
        String content = rateLimitedResponse.getContentAsString();
        assertTrue(content.contains("error"));
        assertTrue(content.contains("Too many requests"));
    }

    @Test
    void shouldRateLimitPerIpAddress() throws ServletException, IOException {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setMethod("POST");
        request1.setRequestURI("/api/inquiries");
        request1.setRemoteAddr("192.168.1.1");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setMethod("POST");
        request2.setRequestURI("/api/inquiries");
        request2.setRemoteAddr("192.168.1.2");

        // Exhaust rate limit for first IP
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request1, response, mockFilterChain);
            assertEquals(200, response.getStatus());
        }

        // First IP should be rate limited
        MockHttpServletResponse rateLimitedResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request1, rateLimitedResponse, mockFilterChain);
        assertEquals(429, rateLimitedResponse.getStatus());

        // Second IP should still work
        MockHttpServletResponse successResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request2, successResponse, mockFilterChain);
        assertEquals(200, successResponse.getStatus());
    }
}

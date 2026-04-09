package com.dyx.blog.service.impl;

import com.dyx.blog.service.IpLookupService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * IP 地址查询服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpLookupServiceImpl implements IpLookupService {

    private static final int HTTP_TIMEOUT_MILLIS = 3000;

    private final ObjectMapper objectMapper;

    @Override
    public boolean shouldLookup(String ip) {
        if (isBlank(ip)) {
            return false;
        }
        String normalized = ip.trim();
        if ("unknown".equalsIgnoreCase(normalized) || "127.0.0.1".equals(normalized)) {
            return false;
        }
        return !isPrivateIpv4(normalized);
    }

    @Override
    @Cacheable(value = "ip-lookup", key = "#apiUrl + ':' + #ip", unless = "#result == null || #result.isBlank()")
    public String resolveActualAddress(String ip, String apiUrl) {
        if (!shouldLookup(ip) || isBlank(apiUrl)) {
            return null;
        }
        String requestUrl = buildRequestUrl(apiUrl, ip);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(HTTP_TIMEOUT_MILLIS))
                .setResponseTimeout(Timeout.ofMilliseconds(HTTP_TIMEOUT_MILLIS))
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            HttpGet request = new HttpGet(requestUrl);
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300 || response.getEntity() == null) {
                    log.warn("IP 地址查询失败，响应状态异常: ip={}, status={}", ip, statusCode);
                    return null;
                }
                JsonNode root = objectMapper.readTree(response.getEntity().getContent());
                JsonNode addressNode = root.path("data").path("address");
                if (!addressNode.isTextual()) {
                    return null;
                }
                String address = normalizeNullableValue(addressNode.asText());
                return isBlank(address) ? null : address;
            }
        } catch (Exception e) {
            log.warn("IP 地址查询失败: ip={}, message={}", ip, e.getMessage());
            return null;
        }
    }

    private String buildRequestUrl(String apiUrl, String ip) {
        String separator = apiUrl.contains("?") ? "&" : "?";
        return apiUrl + separator + "ip=" + URLEncoder.encode(ip, StandardCharsets.UTF_8);
    }

    private boolean isPrivateIpv4(String ip) {
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (!ip.startsWith("172.")) {
            return false;
        }
        String[] parts = ip.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String normalizeNullableValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

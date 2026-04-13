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
import java.util.ArrayList;
import java.util.List;

/**
 * IP 地址查询服务实现。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpLookupServiceImpl implements IpLookupService {

    private static final int HTTP_TIMEOUT_MILLIS = 3000;
    private static final String DEFAULT_IP_LOOKUP_PROVIDER = "xxapi";
    private static final String IP_LOOKUP_PROVIDER_IPDATACLOUD = "ipdatacloud";

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
    @Cacheable(value = "ip-lookup", key = "#provider + ':' + #apiUrl + ':' + (#apiKey == null ? '' : #apiKey) + ':' + #ip", unless = "#result == null || #result.isBlank()")
    public String resolveActualAddress(String ip, String provider, String apiUrl, String apiKey) {
        if (!shouldLookup(ip) || isBlank(apiUrl)) {
            return null;
        }
        String normalizedProvider = normalizeProvider(provider);
        String requestUrl = buildRequestUrl(normalizedProvider, apiUrl, ip, apiKey);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(HTTP_TIMEOUT_MILLIS))
                .setResponseTimeout(Timeout.ofMilliseconds(HTTP_TIMEOUT_MILLIS))
                .build();
        try (CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
            HttpGet request = new HttpGet(requestUrl);
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300 || response.getEntity() == null) {
                    log.warn("IP 地址查询失败，响应状态异常: ip={}, provider={}, status={}", ip, normalizedProvider, statusCode);
                    return null;
                }
                JsonNode root = objectMapper.readTree(response.getEntity().getContent());
                return normalizeNullableValue(resolveAddressByProvider(root, normalizedProvider));
            }
        } catch (Exception e) {
            log.warn("IP 地址查询失败: ip={}, provider={}, message={}", ip, normalizedProvider, e.getMessage());
            return null;
        }
    }

    private String resolveAddressByProvider(JsonNode root, String provider) {
        if (IP_LOOKUP_PROVIDER_IPDATACLOUD.equals(provider)) {
            return resolveIpDataCloudAddress(root);
        }
        JsonNode addressNode = root.path("data").path("address");
        return addressNode.isTextual() ? normalizeNullableValue(addressNode.asText()) : null;
    }

    private String resolveIpDataCloudAddress(JsonNode root) {
        JsonNode locationNode = root.path("data").path("location");
        JsonNode firstStreetNode = locationNode.path("multi_street");
        JsonNode streetNode = firstStreetNode.isArray() && !firstStreetNode.isEmpty() ? firstStreetNode.get(0) : null;
        List<String> parts = new ArrayList<>();
        appendAddressPart(parts, locationNode.path("country").asText(null));
        appendAddressPart(parts, streetNode == null ? locationNode.path("province").asText(null) : streetNode.path("province").asText(null));
        appendAddressPart(parts, streetNode == null ? locationNode.path("city").asText(null) : streetNode.path("city").asText(null));
        appendAddressPart(parts, streetNode == null ? locationNode.path("district").asText(null) : streetNode.path("district").asText(null));
        appendAddressPart(parts, streetNode == null ? locationNode.path("street").asText(null) : streetNode.path("street").asText(null));
        return parts.isEmpty() ? null : String.join("", parts);
    }

    private void appendAddressPart(List<String> parts, String value) {
        String normalized = normalizeNullableValue(value);
        if (normalized != null) {
            parts.add(normalized);
        }
    }

    private String buildRequestUrl(String provider, String apiUrl, String ip, String apiKey) {
        StringBuilder builder = new StringBuilder(apiUrl);
        builder.append(apiUrl.contains("?") ? '&' : '?')
                .append("ip=")
                .append(URLEncoder.encode(ip, StandardCharsets.UTF_8));
        if (IP_LOOKUP_PROVIDER_IPDATACLOUD.equals(provider) && !isBlank(apiKey)) {
            builder.append("&key=")
                    .append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private String normalizeProvider(String provider) {
        return isBlank(provider) ? DEFAULT_IP_LOOKUP_PROVIDER : provider.trim().toLowerCase();
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

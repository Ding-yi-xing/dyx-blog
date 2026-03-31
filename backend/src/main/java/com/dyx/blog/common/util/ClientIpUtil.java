package com.dyx.blog.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析工具。
 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String remoteAddr = normalizeClientIp(request.getRemoteAddr());
        if (isTrustedProxy(remoteAddr)) {
            String forwardedIp = extractForwardedClientIp(request.getHeader("X-Forwarded-For"));
            if (!isBlank(forwardedIp)) {
                return truncate(forwardedIp, 45);
            }
            String realIp = normalizeClientIp(request.getHeader("X-Real-IP"));
            if (!isBlank(realIp)) {
                return truncate(realIp, 45);
            }
        }
        return isBlank(remoteAddr) ? "unknown" : truncate(remoteAddr, 45);
    }

    public static boolean isHttpsRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        if (request.isSecure()) {
            return true;
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && "https".equalsIgnoreCase(forwardedProto.trim());
    }

    private static String extractForwardedClientIp(String value) {
        if (isBlank(value)) {
            return null;
        }
        for (String item : value.split(",")) {
            String candidate = normalizeClientIp(item);
            if (!isBlank(candidate) && !"unknown".equalsIgnoreCase(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static String normalizeClientIp(String value) {
        if (isBlank(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        if ("unknown".equalsIgnoreCase(normalized)) {
            return null;
        }
        if (normalized.startsWith("::ffff:")) {
            normalized = normalized.substring(7);
        }
        if ("::1".equals(normalized)
                || "0:0:0:0:0:0:0:1".equals(normalized)
                || "127.0.0.1".equals(normalized)) {
            return "127.0.0.1";
        }
        return normalized;
    }

    private static boolean isTrustedProxy(String remoteAddr) {
        if (isBlank(remoteAddr)) {
            return false;
        }
        return "127.0.0.1".equals(remoteAddr)
                || "10.0.0.1".equals(remoteAddr)
                || "172.16.0.1".equals(remoteAddr)
                || "192.168.0.1".equals(remoteAddr);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

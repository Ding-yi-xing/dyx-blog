package com.dyx.blog.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析工具。
 * 优先在受信任代理场景下读取转发头，兼容本地开发与反向代理部署。
 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    /**
     * 解析客户端真实 IP。
     * 当请求来自受信任代理时，优先读取 X-Forwarded-For 与 X-Real-IP；
     * 否则直接使用 remoteAddr，并统一规整本地回环地址表现。
     */
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

    /**
     * 判断当前请求是否应视为 HTTPS。
     * 除了 Servlet 原生安全标记外，也兼容反向代理透传的 X-Forwarded-Proto。
     */
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

    /**
     * 从 X-Forwarded-For 中提取首个可信客户端 IP。
     */
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

    /**
     * 规范化 IP 字符串，兼容 IPv6 包裹形式与本地回环地址映射。
     */
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

    /**
     * 判断 remoteAddr 是否属于允许读取转发头的受信任代理来源。
     * 当前实现将回环地址与常见私有网段视为受信代理来源。
     */
    private static boolean isTrustedProxy(String remoteAddr) {
        if (isBlank(remoteAddr)) {
            return false;
        }
        String addr = remoteAddr.trim();
        // 本地回环地址
        if ("127.0.0.1".equals(addr)) {
            return true;
        }
        // 10.0.0.0/8
        if (addr.startsWith("10.")) {
            return true;
        }
        // 192.168.0.0/16
        if (addr.startsWith("192.168.")) {
            return true;
        }
        // 172.16.0.0/12
        if (addr.startsWith("172.")) {
            String[] parts = addr.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    // 忽略解析失败，按非受信处理
                }
            }
        }
        return false;
    }

    /**
     * 将结果限制在数据库字段可接受的最大长度内。
     */
    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 判断字符串是否为空白。
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

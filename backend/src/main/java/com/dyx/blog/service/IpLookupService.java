package com.dyx.blog.service;

/**
 * IP 地址查询服务。
 * 根据访客 IP 解析实际地址，并复用缓存与历史结果减少外部请求。
 */
public interface IpLookupService {

    /**
     * 根据 IP 查询实际地址。
     * 查询失败或无需查询时返回 null。
     *
     * @param ip 客户端 IP。
     * @param apiUrl 查询接口地址。
     * @return 实际地址或 null。
     */
    String resolveActualAddress(String ip, String apiUrl);

    /**
     * 判断当前 IP 是否需要发起外部查询。
     *
     * @param ip 客户端 IP。
     * @return true 表示可查询，false 表示应跳过。
     */
    boolean shouldLookup(String ip);
}

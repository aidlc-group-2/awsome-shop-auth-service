package com.awsome.shop.auth.domain.impl.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 企业邮箱域名白名单校验器（FR-A2）。
 *
 * <p>允许的域名通过配置项 {@code shop.auth.email.allowed-domains} 指定（逗号分隔）。</p>
 */
@Component
public class EmailDomainValidator {

    private final List<String> allowedDomains;

    public EmailDomainValidator(
            @Value("${shop.auth.email.allowed-domains:}") String allowedDomainsConfig) {
        this.allowedDomains = parse(allowedDomainsConfig);
    }

    private List<String> parse(String config) {
        if (config == null || config.isBlank()) {
            return List.of();
        }
        return Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    /**
     * 校验邮箱域名是否在白名单内。
     *
     * @param email 邮箱地址
     * @return true-允许；false-不允许（含格式非法、白名单未配置）
     */
    public boolean isAllowed(String email) {
        if (email == null || allowedDomains.isEmpty()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return allowedDomains.contains(domain);
    }
}

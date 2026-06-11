package com.awsome.shop.auth.domain.impl.service.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmailDomainValidator 单元测试
 */
class EmailDomainValidatorTest {

    @Test
    @DisplayName("白名单内的域名应允许")
    void shouldAllowWhitelistedDomain() {
        EmailDomainValidator validator = new EmailDomainValidator("corp.com,example.com");

        assertThat(validator.isAllowed("alice@corp.com")).isTrue();
        assertThat(validator.isAllowed("bob@example.com")).isTrue();
    }

    @Test
    @DisplayName("白名单外的域名应拒绝")
    void shouldRejectUnknownDomain() {
        EmailDomainValidator validator = new EmailDomainValidator("corp.com");

        assertThat(validator.isAllowed("alice@evil.com")).isFalse();
    }

    @Test
    @DisplayName("域名匹配应忽略大小写")
    void shouldBeCaseInsensitive() {
        EmailDomainValidator validator = new EmailDomainValidator("Corp.COM");

        assertThat(validator.isAllowed("alice@CORP.com")).isTrue();
    }

    @Test
    @DisplayName("配置含空格时应正确解析")
    void shouldTrimConfiguredDomains() {
        EmailDomainValidator validator = new EmailDomainValidator(" corp.com , example.com ");

        assertThat(validator.isAllowed("alice@corp.com")).isTrue();
        assertThat(validator.isAllowed("bob@example.com")).isTrue();
    }

    @Test
    @DisplayName("null 邮箱应拒绝")
    void shouldRejectNullEmail() {
        EmailDomainValidator validator = new EmailDomainValidator("corp.com");

        assertThat(validator.isAllowed(null)).isFalse();
    }

    @Test
    @DisplayName("格式非法的邮箱应拒绝")
    void shouldRejectMalformedEmail() {
        EmailDomainValidator validator = new EmailDomainValidator("corp.com");

        assertThat(validator.isAllowed("no-at-symbol")).isFalse();
        assertThat(validator.isAllowed("trailing-at@")).isFalse();
    }

    @Test
    @DisplayName("白名单为空时一律拒绝")
    void shouldRejectAllWhenWhitelistEmpty() {
        EmailDomainValidator validator = new EmailDomainValidator("");

        assertThat(validator.isAllowed("alice@corp.com")).isFalse();
    }
}

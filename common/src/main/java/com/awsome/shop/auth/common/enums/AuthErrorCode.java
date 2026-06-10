package com.awsome.shop.auth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证相关错误码
 */
@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS("AUTH_001", "用户名或密码错误"),
    ACCOUNT_LOCKED("AUTH_002", "账户已锁定，请 {0} 分钟后重试"),
    ACCOUNT_DISABLED("AUTH_003", "账户已被禁用"),
    INVALID_TOKEN("AUTH_004", "Token 无效或已过期"),
    USER_NOT_FOUND("AUTH_005", "用户不存在"),
    USERNAME_EXISTS("CONFLICT_001", "用户名已存在"),
    EMAIL_EXISTS("CONFLICT_002", "邮箱已被注册"),
    EMAIL_DOMAIN_NOT_ALLOWED("PARAM_101", "邮箱域名不在允许范围内"),
    INVALID_ROLE("PARAM_102", "角色取值非法，仅支持 EMPLOYEE/ADMIN");

    private final String code;
    private final String message;
}

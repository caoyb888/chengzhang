package com.chenzhang.thesis.common.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),

    // 4xx 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),

    // 5xx 服务端错误
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // 业务错误码（1xxx）
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "账号已被禁用"),
    USER_PASSWORD_ERROR(1003, "账号或密码错误"),
    USER_LOCKED(1004, "账号已被锁定，请联系管理员"),
    SMS_CODE_ERROR(1005, "验证码错误或已过期"),
    SMS_CODE_SEND_TOO_FREQUENT(1006, "验证码发送过于频繁，请60秒后重试"),

    // 论文业务错误码（2xxx）
    PAPER_NODE_NOT_READY(2001, "前置节点尚未完成，不可提交"),
    PAPER_DEADLINE_EXPIRED(2002, "提交时间窗口已关闭"),
    PAPER_TOPIC_TAKEN(2003, "该选题已被他人选择，请重新选择"),
    PAPER_COMMENT_TOO_SHORT(2004, "评语字数不足最低要求"),

    // 权限错误码（3xxx）
    DATA_PERMISSION_DENIED(3001, "无权访问该数据");

    private final Integer code;
    private final String message;
}

package com.chenzhang.thesis.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志表：auth_login_log
 */
@Data
@TableName("auth_login_log")
public class LoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long schoolId;
    private String loginAccount;
    private String loginType;
    private String loginResult;
    private String ipAddress;
    private String userAgent;
    private String failReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}

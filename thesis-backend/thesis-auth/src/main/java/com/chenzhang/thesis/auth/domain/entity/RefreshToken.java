package com.chenzhang.thesis.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新令牌表：auth_refresh_token
 */
@Data
@TableName("auth_refresh_token")
public class RefreshToken {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long schoolId;
    private String tokenValue;
    private String clientType;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime expireAt;
    private Integer isRevoked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}

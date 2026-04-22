package com.chenzhang.thesis.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户主表（跨库读取 thesis_user.user）
 */
@Data
@TableName(value = "user", schema = "thesis_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long schoolId;
    private Long teachingPointId;
    private String username;
    private String phoneHash;
    private String passwordHash;
    private String realName;
    private String userType;
    private String avatarUrl;
    private String status;
    private LocalDateTime lockUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}

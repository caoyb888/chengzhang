package com.chenzhang.thesis.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long schoolId;

    private Long teachingPointId;

    private String username;

    private String phone;

    private String phoneHash;

    private String email;

    private String passwordHash;

    private String realName;

    private String idCard;

    private String userType;

    private String studentNo;

    private String teacherNo;

    private String major;

    private String department;

    private String title;

    private String avatarUrl;

    private Integer gender;

    private String status;

    private LocalDateTime lockUntil;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime pwdChangedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}

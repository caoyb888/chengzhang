package com.chenzhang.thesis.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表/详情出参（脱敏手机号）
 */
@Data
public class UserVO {

    private Long id;

    private Long schoolId;

    private Long teachingPointId;

    private String username;

    private String phone;

    private String email;

    private String realName;

    private String userType;

    private String studentNo;

    private String teacherNo;

    private String major;

    private String department;

    private String title;

    private String avatarUrl;

    private Integer gender;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

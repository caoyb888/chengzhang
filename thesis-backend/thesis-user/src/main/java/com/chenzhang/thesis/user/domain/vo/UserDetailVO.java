package com.chenzhang.thesis.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详情出参
 */
@Data
public class UserDetailVO {

    private Long id;

    private Long schoolId;

    private Long teachingPointId;

    private String username;

    private String phone;

    private String email;

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

    private List<RoleVO> roles;
}

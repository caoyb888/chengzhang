package com.chenzhang.thesis.user.domain.dto;

import lombok.Data;

/**
 * 更新用户入参
 */
@Data
public class UpdateUserDTO {

    private Long teachingPointId;

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
}

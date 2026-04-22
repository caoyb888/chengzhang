package com.chenzhang.thesis.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建用户入参
 */
@Data
public class CreateUserDTO {

    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    private Long teachingPointId;

    @NotBlank(message = "登录账号不能为空")
    private String username;

    private String phone;

    private String email;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String idCard;

    @NotBlank(message = "用户类型不能为空")
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

package com.chenzhang.thesis.auth.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Long userId;
    private Long schoolId;
    private String username;
    private String realName;
    private String userType;
    private Long teachingPointId;
    private String avatarUrl;
    private List<String> permissions;
}

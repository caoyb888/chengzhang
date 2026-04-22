package com.chenzhang.thesis.user.domain.dto;

import lombok.Data;

/**
 * 用户查询条件
 */
@Data
public class UserQueryDTO {

    private Integer current = 1;

    private Integer size = 20;

    private String keyword;

    private String userType;

    private Long teachingPointId;

    private String major;

    private String status;
}

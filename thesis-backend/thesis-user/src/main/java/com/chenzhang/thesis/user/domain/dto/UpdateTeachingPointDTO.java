package com.chenzhang.thesis.user.domain.dto;

import lombok.Data;

/**
 * 更新教学点入参
 */
@Data
public class UpdateTeachingPointDTO {

    private String name;

    private String code;

    private String contactName;

    private String contactPhone;

    private String dataScope;

    private String status;

    private Integer sortOrder;
}

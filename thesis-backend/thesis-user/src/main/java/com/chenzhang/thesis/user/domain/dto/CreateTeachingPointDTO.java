package com.chenzhang.thesis.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建教学点入参
 */
@Data
public class CreateTeachingPointDTO {

    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @NotBlank(message = "教学点名称不能为空")
    private String name;

    private String code;

    private String contactName;

    private String contactPhone;

    private String dataScope;

    private String status;

    private Integer sortOrder;
}

package com.chenzhang.thesis.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建角色入参
 */
@Data
public class CreateRoleDTO {

    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Integer sortOrder;
}

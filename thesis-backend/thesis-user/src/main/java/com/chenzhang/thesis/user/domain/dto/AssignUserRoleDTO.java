package com.chenzhang.thesis.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配用户角色入参
 */
@Data
public class AssignUserRoleDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}

package com.chenzhang.thesis.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新角色权限入参
 */
@Data
public class UpdateRolePermissionsDTO {

    @NotNull(message = "权限ID列表不能为空")
    private List<Long> permIds;
}

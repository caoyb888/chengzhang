package com.chenzhang.thesis.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色出参（含权限数量）
 */
@Data
public class RoleVO {

    private Long id;

    private Long schoolId;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer isPreset;

    private Integer isActive;

    private Integer sortOrder;

    private Integer permCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

package com.chenzhang.thesis.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long schoolId;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer isPreset;

    private Integer isActive;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}

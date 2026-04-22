package com.chenzhang.thesis.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("permission")
public class Permission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String permCode;

    private String permName;

    private String permType;

    private String module;

    private Long parentId;

    private String routePath;

    private String icon;

    private Integer sortOrder;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}

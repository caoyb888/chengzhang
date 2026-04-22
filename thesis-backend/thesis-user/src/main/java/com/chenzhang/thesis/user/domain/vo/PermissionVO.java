package com.chenzhang.thesis.user.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点出参
 */
@Data
public class PermissionVO {

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

    private List<PermissionVO> children;
}

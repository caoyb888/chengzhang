package com.chenzhang.thesis.user.service;

import com.chenzhang.thesis.user.domain.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 查询权限树
     */
    List<PermissionVO> getPermissionTree();
}

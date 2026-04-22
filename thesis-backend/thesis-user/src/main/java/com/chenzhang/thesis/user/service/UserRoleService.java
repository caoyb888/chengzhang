package com.chenzhang.thesis.user.service;

import com.chenzhang.thesis.user.domain.dto.AssignUserRoleDTO;

/**
 * 用户角色服务接口
 */
public interface UserRoleService {

    /**
     * 分配用户角色
     */
    void assignUserRoles(AssignUserRoleDTO dto);
}

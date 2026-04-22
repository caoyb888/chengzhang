package com.chenzhang.thesis.user.service;

import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.user.domain.dto.CreateRoleDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateRolePermissionsDTO;
import com.chenzhang.thesis.user.domain.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 创建角色
     */
    Long createRole(CreateRoleDTO dto);

    /**
     * 更新角色
     */
    void updateRole(Long id, CreateRoleDTO dto);

    /**
     * 分页查询角色
     */
    PageResult<RoleVO> pageRoles(Integer current, Integer size, String keyword);

    /**
     * 获取角色详情
     */
    RoleVO getRoleDetail(Long id);

    /**
     * 删除角色
     */
    void removeRole(Long id);

    /**
     * 更新角色权限
     */
    void updateRolePermissions(Long roleId, UpdateRolePermissionsDTO dto);

    /**
     * 应用预设角色套餐权限
     */
    void applyPresetRole(String presetRoleCode, Long targetRoleId);

    /**
     * 初始化预设角色
     */
    void initPresetRoles();

    /**
     * 查询全部有效角色
     */
    List<RoleVO> listActiveRoles();
}

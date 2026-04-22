package com.chenzhang.thesis.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.user.domain.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}

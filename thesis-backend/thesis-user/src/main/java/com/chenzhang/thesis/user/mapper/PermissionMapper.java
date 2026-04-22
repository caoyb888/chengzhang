package com.chenzhang.thesis.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.user.domain.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}

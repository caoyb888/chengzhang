package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.domain.dto.CreateRoleDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateRolePermissionsDTO;
import com.chenzhang.thesis.user.domain.entity.Role;
import com.chenzhang.thesis.user.domain.entity.RolePermission;
import com.chenzhang.thesis.user.domain.vo.RoleVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.RoleMapper;
import com.chenzhang.thesis.user.mapper.RolePermissionMapper;
import com.chenzhang.thesis.user.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    private static final List<String> PRESET_ROLE_CODES = Arrays.asList(
            "SCHOOL_ADMIN", "POINT_ADMIN", "TEACHER", "ASSISTANT", "STUDENT"
    );

    @PostConstruct
    public void init() {
        initPresetRoles();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(CreateRoleDTO dto) {
        Long currentSchoolId = getCurrentSchoolId();
        if (currentSchoolId != null && !Objects.equals(currentSchoolId, dto.getSchoolId())) {
            throw new UserException(ResultCode.DATA_PERMISSION_DENIED, "无权在该学校创建角色");
        }

        Long count = roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getSchoolId, dto.getSchoolId())
                        .eq(Role::getRoleCode, dto.getRoleCode())
                        .eq(Role::getIsDeleted, 0)
        );
        if (count > 0) {
            throw new UserException("角色编码已存在");
        }

        Role role = new Role();
        role.setSchoolId(dto.getSchoolId());
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setIsPreset(0);
        role.setIsActive(1);
        role.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, CreateRoleDTO dto) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "角色不存在");
        }
        if (role.getIsPreset() == 1) {
            throw new UserException("预置角色不允许修改");
        }

        if (StrUtil.isNotBlank(dto.getRoleName())) {
            role.setRoleName(dto.getRoleName());
        }
        if (StrUtil.isNotBlank(dto.getDescription())) {
            role.setDescription(dto.getDescription());
        }
        if (dto.getSortOrder() != null) {
            role.setSortOrder(dto.getSortOrder());
        }
        role.setUpdatedAt(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Override
    public PageResult<RoleVO> pageRoles(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Role::getRoleCode, keyword)
                    .or()
                    .like(Role::getRoleName, keyword));
        }
        wrapper.orderByAsc(Role::getSortOrder)
                .orderByDesc(Role::getCreatedAt);
        IPage<Role> page = new Page<>(current, size);
        roleMapper.selectPage(page, wrapper);
        return PageResult.of(page, this::toRoleVO);
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "角色不存在");
        }
        return toRoleVO(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "角色不存在");
        }
        if (role.getIsPreset() == 1) {
            throw new UserException("预置角色不允许删除");
        }
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRolePermissions(Long roleId, UpdateRolePermissionsDTO dto) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || role.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "角色不存在");
        }

        // 删除旧权限
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        // 插入新权限
        if (dto.getPermIds() != null && !dto.getPermIds().isEmpty()) {
            List<RolePermission> list = dto.getPermIds().stream().map(permId -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermId(permId);
                rp.setCreatedAt(LocalDateTime.now());
                rp.setUpdatedAt(LocalDateTime.now());
                return rp;
            }).toList();
            for (RolePermission rp : list) {
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyPresetRole(String presetRoleCode, Long targetRoleId) {
        if (!PRESET_ROLE_CODES.contains(presetRoleCode)) {
            throw new UserException("无效的角色套餐编码");
        }
        Role presetRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getSchoolId, 0L)
                        .eq(Role::getRoleCode, presetRoleCode)
                        .eq(Role::getIsDeleted, 0)
        );
        if (presetRole == null) {
            throw new UserException("预设角色不存在");
        }

        Role targetRole = roleMapper.selectById(targetRoleId);
        if (targetRole == null || targetRole.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "目标角色不存在");
        }

        // 复制权限
        List<RolePermission> presetPerms = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, presetRole.getId())
                        .eq(RolePermission::getIsDeleted, 0)
        );

        // 删除目标角色旧权限
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, targetRoleId)
        );

        // 复制新权限
        if (!presetPerms.isEmpty()) {
            for (RolePermission rp : presetPerms) {
                RolePermission newRp = new RolePermission();
                newRp.setRoleId(targetRoleId);
                newRp.setPermId(rp.getPermId());
                newRp.setCreatedAt(LocalDateTime.now());
                newRp.setUpdatedAt(LocalDateTime.now());
                rolePermissionMapper.insert(newRp);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initPresetRoles() {
        for (String code : PRESET_ROLE_CODES) {
            Long count = roleMapper.selectCount(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getSchoolId, 0L)
                            .eq(Role::getRoleCode, code)
                            .eq(Role::getIsDeleted, 0)
            );
            if (count == 0) {
                Role role = new Role();
                role.setSchoolId(0L);
                role.setRoleCode(code);
                role.setRoleName(getPresetRoleName(code));
                role.setDescription("系统预置角色：" + getPresetRoleName(code));
                role.setIsPreset(1);
                role.setIsActive(1);
                role.setSortOrder(0);
                role.setCreatedAt(LocalDateTime.now());
                role.setUpdatedAt(LocalDateTime.now());
                roleMapper.insert(role);
                log.info("初始化预置角色: {}", code);
            }
        }
    }

    @Override
    public List<RoleVO> listActiveRoles() {
        List<Role> list = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getIsActive, 1)
                        .eq(Role::getIsDeleted, 0)
                        .orderByAsc(Role::getSortOrder)
        );
        return list.stream().map(this::toRoleVO).toList();
    }

    private RoleVO toRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setSchoolId(role.getSchoolId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setIsPreset(role.getIsPreset());
        vo.setIsActive(role.getIsActive());
        vo.setSortOrder(role.getSortOrder());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        long permCount = rolePermissionMapper.selectCount(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, role.getId())
                        .eq(RolePermission::getIsDeleted, 0)
        );
        vo.setPermCount((int) permCount);
        return vo;
    }

    private String getPresetRoleName(String code) {
        return switch (code) {
            case "SCHOOL_ADMIN" -> "学校管理员";
            case "POINT_ADMIN" -> "教学点管理员";
            case "TEACHER" -> "指导教师";
            case "ASSISTANT" -> "辅助指导教师";
            case "STUDENT" -> "学生";
            default -> code;
        };
    }

    private Long getCurrentSchoolId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object schoolId = StpUtil.getSession().get("schoolId");
        return schoolId == null ? null : Long.valueOf(schoolId.toString());
    }
}

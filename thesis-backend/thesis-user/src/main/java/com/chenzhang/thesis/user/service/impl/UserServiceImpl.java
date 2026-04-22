package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.convert.UserConvert;
import com.chenzhang.thesis.user.domain.dto.CreateUserDTO;
import com.chenzhang.thesis.user.domain.dto.ResetPasswordDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateUserDTO;
import com.chenzhang.thesis.user.domain.dto.UserQueryDTO;
import com.chenzhang.thesis.user.domain.entity.Role;
import com.chenzhang.thesis.user.domain.entity.User;
import com.chenzhang.thesis.user.domain.entity.UserRole;
import com.chenzhang.thesis.user.domain.vo.RoleVO;
import com.chenzhang.thesis.user.domain.vo.UserDetailVO;
import com.chenzhang.thesis.user.domain.vo.UserVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.RoleMapper;
import com.chenzhang.thesis.user.mapper.UserMapper;
import com.chenzhang.thesis.user.mapper.UserRoleMapper;
import com.chenzhang.thesis.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserConvert userConvert;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${thesis.user.default-password:123456}")
    private String defaultPassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(CreateUserDTO dto) {
        Long currentSchoolId = getCurrentSchoolId();
        if (currentSchoolId != null && !Objects.equals(currentSchoolId, dto.getSchoolId())) {
            throw new UserException(ResultCode.DATA_PERMISSION_DENIED, "无权在该学校创建用户");
        }

        // 检查账号唯一性
        Long existCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getSchoolId, dto.getSchoolId())
                        .eq(User::getUsername, dto.getUsername())
                        .eq(User::getIsDeleted, 0)
        );
        if (existCount > 0) {
            throw new UserException("登录账号已存在");
        }

        // 检查学号/工号唯一性
        if (StrUtil.isNotBlank(dto.getStudentNo())) {
            Long stuCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getSchoolId, dto.getSchoolId())
                            .eq(User::getStudentNo, dto.getStudentNo())
                            .eq(User::getIsDeleted, 0)
            );
            if (stuCount > 0) {
                throw new UserException("学号已存在");
            }
        }
        if (StrUtil.isNotBlank(dto.getTeacherNo())) {
            Long teaCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getSchoolId, dto.getSchoolId())
                            .eq(User::getTeacherNo, dto.getTeacherNo())
                            .eq(User::getIsDeleted, 0)
            );
            if (teaCount > 0) {
                throw new UserException("工号已存在");
            }
        }

        User user = userConvert.toUser(dto);
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        if (StrUtil.isNotBlank(dto.getPhone())) {
            user.setPhoneHash(SecureUtil.md5(dto.getPhone()));
        }
        user.setStatus(StrUtil.isBlank(dto.getStatus()) ? "ACTIVE" : dto.getStatus());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new UserException(ResultCode.USER_NOT_FOUND);
        }

        if (dto.getTeachingPointId() != null) {
            user.setTeachingPointId(dto.getTeachingPointId());
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            user.setPhone(dto.getPhone());
            user.setPhoneHash(SecureUtil.md5(dto.getPhone()));
        }
        if (StrUtil.isNotBlank(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StrUtil.isNotBlank(dto.getIdCard())) {
            user.setIdCard(dto.getIdCard());
        }
        if (StrUtil.isNotBlank(dto.getUserType())) {
            user.setUserType(dto.getUserType());
        }
        if (StrUtil.isNotBlank(dto.getStudentNo())) {
            user.setStudentNo(dto.getStudentNo());
        }
        if (StrUtil.isNotBlank(dto.getTeacherNo())) {
            user.setTeacherNo(dto.getTeacherNo());
        }
        if (StrUtil.isNotBlank(dto.getMajor())) {
            user.setMajor(dto.getMajor());
        }
        if (StrUtil.isNotBlank(dto.getDepartment())) {
            user.setDepartment(dto.getDepartment());
        }
        if (StrUtil.isNotBlank(dto.getTitle())) {
            user.setTitle(dto.getTitle());
        }
        if (StrUtil.isNotBlank(dto.getAvatarUrl())) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            user.setStatus(dto.getStatus());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public PageResult<UserVO> pageUsers(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(User::getUsername, queryDTO.getKeyword())
                    .or()
                    .like(User::getRealName, queryDTO.getKeyword())
                    .or()
                    .like(User::getPhone, queryDTO.getKeyword()));
        }
        if (StrUtil.isNotBlank(queryDTO.getUserType())) {
            wrapper.eq(User::getUserType, queryDTO.getUserType());
        }
        if (queryDTO.getTeachingPointId() != null) {
            wrapper.eq(User::getTeachingPointId, queryDTO.getTeachingPointId());
        }
        if (StrUtil.isNotBlank(queryDTO.getMajor())) {
            wrapper.eq(User::getMajor, queryDTO.getMajor());
        }
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(User::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(User::getCreatedAt);
        IPage<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        userMapper.selectPage(page, wrapper);
        return PageResult.of(page, userConvert::toUserVO);
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new UserException(ResultCode.USER_NOT_FOUND);
        }
        UserDetailVO detailVO = userConvert.toUserDetailVO(user);
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getIsDeleted, 0)
        );
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
            List<Role> roles = roleMapper.selectBatchIds(roleIds);
            List<RoleVO> roleVOs = roles.stream().map(r -> {
                RoleVO vo = new RoleVO();
                vo.setId(r.getId());
                vo.setSchoolId(r.getSchoolId());
                vo.setRoleCode(r.getRoleCode());
                vo.setRoleName(r.getRoleName());
                vo.setDescription(r.getDescription());
                vo.setIsPreset(r.getIsPreset());
                vo.setIsActive(r.getIsActive());
                vo.setSortOrder(r.getSortOrder());
                vo.setCreatedAt(r.getCreatedAt());
                vo.setUpdatedAt(r.getUpdatedAt());
                return vo;
            }).toList();
            detailVO.setRoles(roleVOs);
        }
        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new UserException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, ResetPasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new UserException(ResultCode.USER_NOT_FOUND);
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setPwdChangedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new UserException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private Long getCurrentSchoolId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object schoolId = StpUtil.getSession().get("schoolId");
        return schoolId == null ? null : Long.valueOf(schoolId.toString());
    }
}

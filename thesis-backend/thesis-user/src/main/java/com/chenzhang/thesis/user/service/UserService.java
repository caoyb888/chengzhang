package com.chenzhang.thesis.user.service;

import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.user.domain.dto.CreateUserDTO;
import com.chenzhang.thesis.user.domain.dto.ResetPasswordDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateUserDTO;
import com.chenzhang.thesis.user.domain.dto.UserQueryDTO;
import com.chenzhang.thesis.user.domain.vo.UserDetailVO;
import com.chenzhang.thesis.user.domain.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 新增用户
     */
    Long createUser(CreateUserDTO dto);

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, UpdateUserDTO dto);

    /**
     * 分页查询用户
     */
    PageResult<UserVO> pageUsers(UserQueryDTO queryDTO);

    /**
     * 获取用户详情
     */
    UserDetailVO getUserDetail(Long userId);

    /**
     * 删除用户（逻辑删除）
     */
    void removeUser(Long userId);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, ResetPasswordDTO dto);

    /**
     * 修改用户状态
     */
    void updateStatus(Long userId, String status);
}

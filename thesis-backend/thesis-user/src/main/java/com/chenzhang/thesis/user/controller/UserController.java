package com.chenzhang.thesis.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.user.domain.dto.CreateUserDTO;
import com.chenzhang.thesis.user.domain.dto.ResetPasswordDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateUserDTO;
import com.chenzhang.thesis.user.domain.dto.UserQueryDTO;
import com.chenzhang.thesis.user.domain.vo.UserDetailVO;
import com.chenzhang.thesis.user.domain.vo.UserVO;
import com.chenzhang.thesis.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    @SaCheckPermission("user:create")
    public Result<Long> createUser(@RequestBody @Validated CreateUserDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.ok(userId);
    }

    @PutMapping("/{userId}")
    @SaCheckPermission("user:update")
    public Result<Void> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDTO dto) {
        userService.updateUser(userId, dto);
        return Result.ok();
    }

    @GetMapping("/list")
    @SaCheckPermission("user:read")
    public Result<PageResult<UserVO>> listUsers(UserQueryDTO queryDTO) {
        PageResult<UserVO> result = userService.pageUsers(queryDTO);
        return Result.ok(result);
    }

    @GetMapping("/{userId}")
    @SaCheckPermission("user:read")
    public Result<UserDetailVO> getUserDetail(@PathVariable Long userId) {
        UserDetailVO detail = userService.getUserDetail(userId);
        return Result.ok(detail);
    }

    @DeleteMapping("/{userId}")
    @SaCheckPermission("user:delete")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        userService.removeUser(userId);
        return Result.ok();
    }

    @PutMapping("/{userId}/reset-password")
    @SaCheckPermission("user:reset-password")
    public Result<Void> resetPassword(@PathVariable Long userId, @RequestBody @Validated ResetPasswordDTO dto) {
        userService.resetPassword(userId, dto);
        return Result.ok();
    }

    @PutMapping("/{userId}/status")
    @SaCheckPermission("user:update-status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam String status) {
        userService.updateStatus(userId, status);
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserDetailVO> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserDetailVO detail = userService.getUserDetail(userId);
        return Result.ok(detail);
    }
}

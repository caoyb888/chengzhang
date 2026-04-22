package com.chenzhang.thesis.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.user.domain.dto.CreateRoleDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateRolePermissionsDTO;
import com.chenzhang.thesis.user.domain.vo.RoleVO;
import com.chenzhang.thesis.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/role")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @SaCheckPermission("role:create")
    public Result<Long> createRole(@RequestBody @Validated CreateRoleDTO dto) {
        Long id = roleService.createRole(dto);
        return Result.ok(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("role:update")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody CreateRoleDTO dto) {
        roleService.updateRole(id, dto);
        return Result.ok();
    }

    @GetMapping("/list")
    @SaCheckPermission("role:read")
    public Result<PageResult<RoleVO>> listRoles(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        PageResult<RoleVO> result = roleService.pageRoles(current, size, keyword);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("role:read")
    public Result<RoleVO> getRoleDetail(@PathVariable Long id) {
        RoleVO vo = roleService.getRoleDetail(id);
        return Result.ok(vo);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("role:delete")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.removeRole(id);
        return Result.ok();
    }

    @PutMapping("/{roleId}/permissions")
    @SaCheckPermission("role:update-perm")
    public Result<Void> updateRolePermissions(@PathVariable Long roleId, @RequestBody @Validated UpdateRolePermissionsDTO dto) {
        roleService.updateRolePermissions(roleId, dto);
        return Result.ok();
    }

    @PostMapping("/preset/{presetRoleCode}/apply")
    @SaCheckPermission("role:apply-preset")
    public Result<Void> applyPresetRole(@PathVariable String presetRoleCode, @RequestParam Long roleId) {
        roleService.applyPresetRole(presetRoleCode, roleId);
        return Result.ok();
    }

    @GetMapping("/active-list")
    @SaCheckPermission("role:read")
    public Result<List<RoleVO>> listActiveRoles() {
        List<RoleVO> list = roleService.listActiveRoles();
        return Result.ok(list);
    }
}

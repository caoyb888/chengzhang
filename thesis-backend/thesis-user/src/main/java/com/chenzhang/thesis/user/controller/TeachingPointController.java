package com.chenzhang.thesis.user.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.user.domain.dto.CreateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.vo.TeachingPointVO;
import com.chenzhang.thesis.user.service.TeachingPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教学点管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/teaching-point")
@RequiredArgsConstructor
@Validated
public class TeachingPointController {

    private final TeachingPointService teachingPointService;

    @PostMapping
    @SaCheckPermission("teaching-point:create")
    public Result<Long> createTeachingPoint(@RequestBody @Validated CreateTeachingPointDTO dto) {
        Long id = teachingPointService.createTeachingPoint(dto);
        return Result.ok(id);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("teaching-point:update")
    public Result<Void> updateTeachingPoint(@PathVariable Long id, @RequestBody UpdateTeachingPointDTO dto) {
        teachingPointService.updateTeachingPoint(id, dto);
        return Result.ok();
    }

    @GetMapping("/list")
    @SaCheckPermission("teaching-point:read")
    public Result<PageResult<TeachingPointVO>> listTeachingPoints(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword) {
        PageResult<TeachingPointVO> result = teachingPointService.pageTeachingPoints(current, size, keyword);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("teaching-point:read")
    public Result<TeachingPointVO> getTeachingPointDetail(@PathVariable Long id) {
        TeachingPointVO vo = teachingPointService.getTeachingPointDetail(id);
        return Result.ok(vo);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("teaching-point:delete")
    public Result<Void> deleteTeachingPoint(@PathVariable Long id) {
        teachingPointService.removeTeachingPoint(id);
        return Result.ok();
    }

    @GetMapping("/active-list")
    @SaCheckPermission("teaching-point:read")
    public Result<List<TeachingPointVO>> listActiveTeachingPoints() {
        List<TeachingPointVO> list = teachingPointService.listActiveTeachingPoints();
        return Result.ok(list);
    }
}

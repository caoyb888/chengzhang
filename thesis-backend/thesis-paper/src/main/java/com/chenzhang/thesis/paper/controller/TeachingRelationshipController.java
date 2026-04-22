package com.chenzhang.thesis.paper.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipDTO;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipQueryDTO;
import com.chenzhang.thesis.paper.domain.vo.TeachingRelationshipVO;
import com.chenzhang.thesis.paper.service.TeachingRelationshipService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 师生指导关系管理接口
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paper/relationship")
public class TeachingRelationshipController {

    private final TeachingRelationshipService teachingRelationshipService;

    /**
     * 单个创建指导关系
     */
    @PostMapping
    @SaCheckPermission("paper:relationship:create")
    public Result<Long> create(@RequestBody @Valid TeachingRelationshipDTO dto) {
        Long id = teachingRelationshipService.create(dto);
        return Result.ok(id);
    }

    /**
     * 批量创建指导关系
     */
    @PostMapping("/batch")
    @SaCheckPermission("paper:relationship:create")
    public Result<Integer> batchCreate(@RequestBody @Valid List<TeachingRelationshipDTO> dtoList) {
        int count = teachingRelationshipService.batchCreate(dtoList);
        return Result.ok(count);
    }

    /**
     * 分页查询指导关系列表
     */
    @GetMapping("/list")
    @SaCheckPermission("paper:relationship:query")
    public Result<PageResult<TeachingRelationshipVO>> list(TeachingRelationshipQueryDTO query) {
        IPage<TeachingRelationshipVO> page = teachingRelationshipService.pageList(query);
        return Result.ok(PageResult.of(page));
    }

    /**
     * 修改指导关系状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission("paper:relationship:update")
    public Result<Void> updateStatus(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @RequestParam @NotNull(message = "状态不能为空") Integer isActive) {
        teachingRelationshipService.updateStatus(id, isActive);
        return Result.ok();
    }

    /**
     * 删除指导关系（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("paper:relationship:delete")
    public Result<Void> remove(
            @PathVariable @NotNull(message = "ID不能为空") Long id) {
        teachingRelationshipService.remove(id);
        return Result.ok();
    }
}

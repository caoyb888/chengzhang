package com.chenzhang.thesis.paper.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.paper.domain.dto.BatchAddStudentsDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchCreateDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchQueryDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchUpdateDTO;
import com.chenzhang.thesis.paper.domain.vo.BatchAddStudentsResultVO;
import com.chenzhang.thesis.paper.domain.vo.BatchDetailVO;
import com.chenzhang.thesis.paper.domain.vo.BatchVO;
import com.chenzhang.thesis.paper.service.BatchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 批次管理接口
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/batch")
public class BatchController {

    private final BatchService batchService;

    /**
     * 创建批次
     */
    @PostMapping
    @SaCheckPermission("batch:create")
    public Result<Long> create(@RequestBody @Valid BatchCreateDTO dto) {
        Long batchId = batchService.createBatch(dto);
        return Result.ok(batchId);
    }

    /**
     * 修改批次
     */
    @PutMapping("/{batchId}")
    @SaCheckPermission("batch:edit")
    public Result<Void> update(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId,
            @RequestBody @Valid BatchUpdateDTO dto) {
        batchService.updateBatch(batchId, dto);
        return Result.ok();
    }

    /**
     * 查询批次详情
     */
    @GetMapping("/{batchId}")
    @SaCheckPermission("batch:read")
    public Result<BatchDetailVO> getDetail(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId) {
        BatchDetailVO detail = batchService.getBatchDetail(batchId);
        return Result.ok(detail);
    }

    /**
     * 分页查询批次列表
     */
    @GetMapping("/list")
    @SaCheckPermission("batch:list")
    public Result<PageResult<BatchVO>> list(BatchQueryDTO query) {
        IPage<BatchVO> page = batchService.pageBatches(query);
        return Result.ok(PageResult.of(page));
    }

    /**
     * 删除批次（仅 DRAFT 可删）
     */
    @DeleteMapping("/{batchId}")
    @SaCheckPermission("batch:delete")
    public Result<Void> delete(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId) {
        batchService.deleteBatch(batchId);
        return Result.ok();
    }

    /**
     * 批量添加学生到批次
     */
    @PostMapping("/{batchId}/students")
    @SaCheckPermission("batch:add-student")
    public Result<BatchAddStudentsResultVO> addStudents(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId,
            @RequestBody @Valid BatchAddStudentsDTO dto) {
        BatchAddStudentsResultVO result = batchService.addStudents(batchId, dto);
        return Result.ok(result);
    }

    /**
     * 切换批次状态
     */
    @PutMapping("/{batchId}/status")
    @SaCheckPermission("batch:edit")
    public Result<Void> updateStatus(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId,
            @RequestParam @NotBlank(message = "目标状态不能为空") String status) {
        batchService.updateBatchStatus(batchId, status);
        return Result.ok();
    }

    /**
     * 复制批次
     */
    @PostMapping("/{batchId}/copy")
    @SaCheckPermission("batch:create")
    public Result<Long> copy(
            @PathVariable @NotNull(message = "批次ID不能为空") Long batchId) {
        Long newBatchId = batchService.copyBatch(batchId);
        return Result.ok(newBatchId);
    }
}

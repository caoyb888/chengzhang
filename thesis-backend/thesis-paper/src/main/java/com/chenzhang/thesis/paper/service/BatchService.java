package com.chenzhang.thesis.paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chenzhang.thesis.paper.domain.dto.BatchAddStudentsDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchCreateDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchQueryDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchUpdateDTO;
import com.chenzhang.thesis.paper.domain.vo.BatchAddStudentsResultVO;
import com.chenzhang.thesis.paper.domain.vo.BatchDetailVO;
import com.chenzhang.thesis.paper.domain.vo.BatchVO;

/**
 * 批次管理 Service
 */
public interface BatchService {

    /**
     * 创建批次
     *
     * @param dto 入参
     * @return 批次ID
     */
    Long createBatch(BatchCreateDTO dto);

    /**
     * 修改批次
     *
     * @param batchId 批次ID
     * @param dto     入参
     */
    void updateBatch(Long batchId, BatchUpdateDTO dto);

    /**
     * 查询批次详情
     *
     * @param batchId 批次ID
     * @return 详情
     */
    BatchDetailVO getBatchDetail(Long batchId);

    /**
     * 分页查询批次列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<BatchVO> pageBatches(BatchQueryDTO query);

    /**
     * 删除批次（仅 DRAFT 可删）
     *
     * @param batchId 批次ID
     */
    void deleteBatch(Long batchId);

    /**
     * 批量添加学生到批次（同步创建 paper 记录）
     *
     * @param batchId 批次ID
     * @param dto     学生ID列表
     * @return 添加结果
     */
    BatchAddStudentsResultVO addStudents(Long batchId, BatchAddStudentsDTO dto);

    /**
     * 切换批次状态
     *
     * @param batchId      批次ID
     * @param targetStatus 目标状态
     */
    void updateBatchStatus(Long batchId, String targetStatus);

    /**
     * 复制批次（含流程配置）
     *
     * @param batchId 源批次ID
     * @return 新批次ID
     */
    Long copyBatch(Long batchId);
}

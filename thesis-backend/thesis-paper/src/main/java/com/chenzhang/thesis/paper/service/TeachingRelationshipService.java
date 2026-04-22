package com.chenzhang.thesis.paper.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipDTO;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipQueryDTO;
import com.chenzhang.thesis.paper.domain.entity.TeachingRelationship;
import com.chenzhang.thesis.paper.domain.vo.TeachingRelationshipVO;

import java.util.List;

/**
 * 师生指导关系 Service
 */
public interface TeachingRelationshipService {

    /**
     * 单个创建指导关系
     *
     * @param dto 入参
     * @return 创建后的实体ID
     */
    Long create(TeachingRelationshipDTO dto);

    /**
     * 批量创建指导关系
     *
     * @param dtoList 入参列表
     * @return 成功创建的记录数
     */
    int batchCreate(List<TeachingRelationshipDTO> dtoList);

    /**
     * 分页查询指导关系列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<TeachingRelationshipVO> pageList(TeachingRelationshipQueryDTO query);

    /**
     * 修改指导关系状态（启用/禁用）
     *
     * @param id       关系ID
     * @param isActive 状态（0-禁用 1-启用）
     */
    void updateStatus(Long id, Integer isActive);

    /**
     * 逻辑删除指导关系
     *
     * @param id 关系ID
     */
    void remove(Long id);
}

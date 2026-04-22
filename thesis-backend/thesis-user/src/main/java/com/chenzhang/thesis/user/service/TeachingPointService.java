package com.chenzhang.thesis.user.service;

import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.user.domain.dto.CreateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.vo.TeachingPointVO;

import java.util.List;

/**
 * 教学点服务接口
 */
public interface TeachingPointService {

    /**
     * 创建教学点
     */
    Long createTeachingPoint(CreateTeachingPointDTO dto);

    /**
     * 更新教学点
     */
    void updateTeachingPoint(Long id, UpdateTeachingPointDTO dto);

    /**
     * 分页查询教学点
     */
    PageResult<TeachingPointVO> pageTeachingPoints(Integer current, Integer size, String keyword);

    /**
     * 获取教学点详情
     */
    TeachingPointVO getTeachingPointDetail(Long id);

    /**
     * 删除教学点（逻辑删除）
     */
    void removeTeachingPoint(Long id);

    /**
     * 查询全部有效教学点
     */
    List<TeachingPointVO> listActiveTeachingPoints();
}

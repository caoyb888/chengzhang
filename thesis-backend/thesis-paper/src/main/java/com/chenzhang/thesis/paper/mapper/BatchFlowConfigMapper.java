package com.chenzhang.thesis.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.paper.domain.entity.BatchFlowConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批次流程节点配置 Mapper
 */
@Mapper
public interface BatchFlowConfigMapper extends BaseMapper<BatchFlowConfig> {
}

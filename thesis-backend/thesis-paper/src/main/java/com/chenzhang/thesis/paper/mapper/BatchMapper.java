package com.chenzhang.thesis.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.paper.domain.entity.Batch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 批次 Mapper
 */
@Mapper
public interface BatchMapper extends BaseMapper<Batch> {
}

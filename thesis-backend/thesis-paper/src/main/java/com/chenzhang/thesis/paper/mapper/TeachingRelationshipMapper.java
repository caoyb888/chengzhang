package com.chenzhang.thesis.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.paper.domain.entity.TeachingRelationship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 师生指导关系 Mapper
 */
@Mapper
public interface TeachingRelationshipMapper extends BaseMapper<TeachingRelationship> {
}

package com.chenzhang.thesis.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.paper.domain.entity.StudentBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生批次关联 Mapper
 */
@Mapper
public interface StudentBatchMapper extends BaseMapper<StudentBatch> {
}

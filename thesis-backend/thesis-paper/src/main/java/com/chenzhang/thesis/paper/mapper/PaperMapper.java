package com.chenzhang.thesis.paper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.paper.domain.entity.Paper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论文 Mapper
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {
}

package com.chenzhang.thesis.paper.convert;

import com.chenzhang.thesis.paper.domain.dto.BatchCreateDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchUpdateDTO;
import com.chenzhang.thesis.paper.domain.entity.Batch;
import com.chenzhang.thesis.paper.domain.vo.BatchDetailVO;
import com.chenzhang.thesis.paper.domain.vo.BatchVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 批次对象转换器
 */
@Mapper(componentModel = "spring")
public interface BatchConvert {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalStudents", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Batch toEntity(BatchCreateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalStudents", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "archivedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Batch toEntity(BatchUpdateDTO dto);

    @Mapping(source = "id", target = "batchId")
    BatchVO toVO(Batch entity);

    @Mapping(source = "id", target = "batchId")
    BatchDetailVO toDetailVO(Batch entity);

    List<BatchVO> toVOList(List<Batch> entities);
}

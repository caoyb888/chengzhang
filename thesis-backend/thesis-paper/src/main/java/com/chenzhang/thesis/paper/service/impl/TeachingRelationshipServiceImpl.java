package com.chenzhang.thesis.paper.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipDTO;
import com.chenzhang.thesis.paper.domain.dto.TeachingRelationshipQueryDTO;
import com.chenzhang.thesis.paper.domain.entity.TeachingRelationship;
import com.chenzhang.thesis.paper.domain.vo.TeachingRelationshipVO;
import com.chenzhang.thesis.paper.exception.PaperException;
import com.chenzhang.thesis.paper.mapper.TeachingRelationshipMapper;
import com.chenzhang.thesis.paper.service.TeachingRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 师生指导关系 Service 实现
 */
@Slf4j
@Service
public class TeachingRelationshipServiceImpl extends ServiceImpl<TeachingRelationshipMapper, TeachingRelationship>
        implements TeachingRelationshipService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(TeachingRelationshipDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 校验同批次同学生同教师同类型同层级是否已存在
        TeachingRelationship existing = this.getOne(
                new LambdaQueryWrapper<TeachingRelationship>()
                        .eq(TeachingRelationship::getBatchId, dto.getBatchId())
                        .eq(TeachingRelationship::getStudentId, dto.getStudentId())
                        .eq(TeachingRelationship::getTeacherId, dto.getTeacherId())
                        .eq(TeachingRelationship::getTeacherType, dto.getTeacherType())
                        .eq(TeachingRelationship::getLevel, dto.getLevel())
                        .eq(TeachingRelationship::getIsDeleted, 0)
        );
        if (existing != null) {
            throw new PaperException("该指导关系已存在");
        }

        TeachingRelationship entity = new TeachingRelationship();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsActive(1);
        entity.setAssignedBy(currentUserId);

        boolean saved = this.save(entity);
        if (!saved) {
            throw new PaperException(ResultCode.INTERNAL_ERROR, "创建指导关系失败");
        }

        log.info("[指导关系创建] id={}, batchId={}, studentId={}, teacherId={}, operator={}",
                entity.getId(), entity.getBatchId(), entity.getStudentId(), entity.getTeacherId(), currentUserId);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(List<TeachingRelationshipDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return 0;
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();

        List<TeachingRelationship> entities = dtoList.stream().map(dto -> {
            TeachingRelationship entity = new TeachingRelationship();
            BeanUtils.copyProperties(dto, entity);
            entity.setIsActive(1);
            entity.setAssignedBy(currentUserId);
            return entity;
        }).collect(Collectors.toList());

        boolean saved = this.saveBatch(entities, 500);
        if (!saved) {
            throw new PaperException(ResultCode.INTERNAL_ERROR, "批量创建指导关系失败");
        }

        log.info("[指导关系批量创建] count={}, operator={}", entities.size(), currentUserId);
        return entities.size();
    }

    @Override
    public IPage<TeachingRelationshipVO> pageList(TeachingRelationshipQueryDTO query) {
        Page<TeachingRelationship> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<TeachingRelationship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(query.getBatchId()), TeachingRelationship::getBatchId, query.getBatchId())
                .eq(Objects.nonNull(query.getStudentId()), TeachingRelationship::getStudentId, query.getStudentId())
                .eq(Objects.nonNull(query.getTeacherId()), TeachingRelationship::getTeacherId, query.getTeacherId())
                .eq(Objects.nonNull(query.getTeacherType()), TeachingRelationship::getTeacherType, query.getTeacherType())
                .eq(Objects.nonNull(query.getIsActive()), TeachingRelationship::getIsActive, query.getIsActive())
                .eq(TeachingRelationship::getIsDeleted, 0)
                .orderByDesc(TeachingRelationship::getCreatedAt);

        IPage<TeachingRelationship> entityPage = this.page(page, wrapper);

        return entityPage.convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer isActive) {
        if (isActive == null || (isActive != 0 && isActive != 1)) {
            throw new PaperException("状态值非法，只能为 0 或 1");
        }

        boolean updated = this.update(
                new LambdaUpdateWrapper<TeachingRelationship>()
                        .set(TeachingRelationship::getIsActive, isActive)
                        .eq(TeachingRelationship::getId, id)
                        .eq(TeachingRelationship::getIsDeleted, 0)
        );
        if (!updated) {
            throw new PaperException(ResultCode.NOT_FOUND, "指导关系不存在或已被删除");
        }

        log.info("[指导关系状态变更] id={}, isActive={}", id, isActive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new PaperException(ResultCode.NOT_FOUND, "指导关系不存在或已被删除");
        }

        log.info("[指导关系删除] id={}", id);
    }

    private TeachingRelationshipVO convertToVO(TeachingRelationship entity) {
        if (entity == null) {
            return null;
        }
        TeachingRelationshipVO vo = new TeachingRelationshipVO();
        BeanUtils.copyProperties(entity, vo);
        // 姓名字段暂留空，后续通过 Feign 查询用户服务填充
        return vo;
    }
}

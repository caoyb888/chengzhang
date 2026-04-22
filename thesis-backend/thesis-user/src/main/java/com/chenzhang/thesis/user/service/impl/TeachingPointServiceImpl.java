package com.chenzhang.thesis.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenzhang.thesis.common.result.PageResult;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.user.domain.dto.CreateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.dto.UpdateTeachingPointDTO;
import com.chenzhang.thesis.user.domain.entity.TeachingPoint;
import com.chenzhang.thesis.user.domain.vo.TeachingPointVO;
import com.chenzhang.thesis.user.exception.UserException;
import com.chenzhang.thesis.user.mapper.TeachingPointMapper;
import com.chenzhang.thesis.user.service.TeachingPointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 教学点服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeachingPointServiceImpl implements TeachingPointService {

    private final TeachingPointMapper teachingPointMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTeachingPoint(CreateTeachingPointDTO dto) {
        Long currentSchoolId = getCurrentSchoolId();
        if (currentSchoolId != null && !Objects.equals(currentSchoolId, dto.getSchoolId())) {
            throw new UserException(ResultCode.DATA_PERMISSION_DENIED, "无权在该学校创建教学点");
        }

        if (StrUtil.isNotBlank(dto.getCode())) {
            Long count = teachingPointMapper.selectCount(
                    new LambdaQueryWrapper<TeachingPoint>()
                            .eq(TeachingPoint::getSchoolId, dto.getSchoolId())
                            .eq(TeachingPoint::getCode, dto.getCode())
                            .eq(TeachingPoint::getIsDeleted, 0)
            );
            if (count > 0) {
                throw new UserException("教学点编码已存在");
            }
        }

        TeachingPoint point = new TeachingPoint();
        point.setSchoolId(dto.getSchoolId());
        point.setName(dto.getName());
        point.setCode(dto.getCode());
        point.setContactName(dto.getContactName());
        point.setContactPhone(dto.getContactPhone());
        point.setDataScope(StrUtil.isBlank(dto.getDataScope()) ? "POINT" : dto.getDataScope());
        point.setStatus(StrUtil.isBlank(dto.getStatus()) ? "ACTIVE" : dto.getStatus());
        point.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        point.setCreatedAt(LocalDateTime.now());
        point.setUpdatedAt(LocalDateTime.now());
        teachingPointMapper.insert(point);
        return point.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTeachingPoint(Long id, UpdateTeachingPointDTO dto) {
        TeachingPoint point = teachingPointMapper.selectById(id);
        if (point == null || point.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "教学点不存在");
        }

        if (StrUtil.isNotBlank(dto.getName())) {
            point.setName(dto.getName());
        }
        if (StrUtil.isNotBlank(dto.getCode())) {
            point.setCode(dto.getCode());
        }
        if (StrUtil.isNotBlank(dto.getContactName())) {
            point.setContactName(dto.getContactName());
        }
        if (StrUtil.isNotBlank(dto.getContactPhone())) {
            point.setContactPhone(dto.getContactPhone());
        }
        if (StrUtil.isNotBlank(dto.getDataScope())) {
            point.setDataScope(dto.getDataScope());
        }
        if (StrUtil.isNotBlank(dto.getStatus())) {
            point.setStatus(dto.getStatus());
        }
        if (dto.getSortOrder() != null) {
            point.setSortOrder(dto.getSortOrder());
        }
        point.setUpdatedAt(LocalDateTime.now());
        teachingPointMapper.updateById(point);
    }

    @Override
    public PageResult<TeachingPointVO> pageTeachingPoints(Integer current, Integer size, String keyword) {
        LambdaQueryWrapper<TeachingPoint> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(TeachingPoint::getName, keyword)
                    .or()
                    .like(TeachingPoint::getCode, keyword));
        }
        wrapper.orderByAsc(TeachingPoint::getSortOrder)
                .orderByDesc(TeachingPoint::getCreatedAt);
        IPage<TeachingPoint> page = new Page<>(current, size);
        teachingPointMapper.selectPage(page, wrapper);
        return PageResult.of(page, this::toTeachingPointVO);
    }

    @Override
    public TeachingPointVO getTeachingPointDetail(Long id) {
        TeachingPoint point = teachingPointMapper.selectById(id);
        if (point == null || point.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "教学点不存在");
        }
        return toTeachingPointVO(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeTeachingPoint(Long id) {
        TeachingPoint point = teachingPointMapper.selectById(id);
        if (point == null || point.getIsDeleted() == 1) {
            throw new UserException(ResultCode.NOT_FOUND, "教学点不存在");
        }
        teachingPointMapper.deleteById(id);
    }

    @Override
    public List<TeachingPointVO> listActiveTeachingPoints() {
        List<TeachingPoint> list = teachingPointMapper.selectList(
                new LambdaQueryWrapper<TeachingPoint>()
                        .eq(TeachingPoint::getStatus, "ACTIVE")
                        .eq(TeachingPoint::getIsDeleted, 0)
                        .orderByAsc(TeachingPoint::getSortOrder)
        );
        return list.stream().map(this::toTeachingPointVO).toList();
    }

    private TeachingPointVO toTeachingPointVO(TeachingPoint point) {
        TeachingPointVO vo = new TeachingPointVO();
        vo.setId(point.getId());
        vo.setSchoolId(point.getSchoolId());
        vo.setName(point.getName());
        vo.setCode(point.getCode());
        vo.setContactName(point.getContactName());
        vo.setContactPhone(point.getContactPhone());
        vo.setDataScope(point.getDataScope());
        vo.setStatus(point.getStatus());
        vo.setSortOrder(point.getSortOrder());
        vo.setCreatedAt(point.getCreatedAt());
        vo.setUpdatedAt(point.getUpdatedAt());
        return vo;
    }

    private Long getCurrentSchoolId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object schoolId = StpUtil.getSession().get("schoolId");
        return schoolId == null ? null : Long.valueOf(schoolId.toString());
    }
}

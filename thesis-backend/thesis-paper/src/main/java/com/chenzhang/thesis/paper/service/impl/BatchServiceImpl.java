package com.chenzhang.thesis.paper.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenzhang.thesis.common.result.Result;
import com.chenzhang.thesis.common.result.ResultCode;
import com.chenzhang.thesis.paper.convert.BatchConvert;
import com.chenzhang.thesis.paper.domain.dto.BatchAddStudentsDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchCreateDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchQueryDTO;
import com.chenzhang.thesis.paper.domain.dto.BatchUpdateDTO;
import com.chenzhang.thesis.paper.domain.entity.Batch;
import com.chenzhang.thesis.paper.domain.entity.BatchFlowConfig;
import com.chenzhang.thesis.paper.domain.entity.NodeTeacherLevel;
import com.chenzhang.thesis.paper.domain.entity.Paper;
import com.chenzhang.thesis.paper.domain.entity.StudentBatch;
import com.chenzhang.thesis.paper.domain.vo.BatchAddStudentsResultVO;
import com.chenzhang.thesis.paper.domain.vo.BatchDetailVO;
import com.chenzhang.thesis.paper.domain.vo.BatchVO;
import com.chenzhang.thesis.paper.domain.vo.FeignUserVO;
import com.chenzhang.thesis.paper.exception.PaperException;
import com.chenzhang.thesis.paper.feign.UserServiceClient;
import com.chenzhang.thesis.paper.mapper.BatchFlowConfigMapper;
import com.chenzhang.thesis.paper.mapper.BatchMapper;
import com.chenzhang.thesis.paper.mapper.NodeTeacherLevelMapper;
import com.chenzhang.thesis.paper.mapper.PaperMapper;
import com.chenzhang.thesis.paper.mapper.StudentBatchMapper;
import com.chenzhang.thesis.paper.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 批次管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl extends ServiceImpl<BatchMapper, Batch> implements BatchService {

    private final BatchConvert batchConvert;
    private final BatchMapper batchMapper;
    private final StudentBatchMapper studentBatchMapper;
    private final PaperMapper paperMapper;
    private final BatchFlowConfigMapper batchFlowConfigMapper;
    private final NodeTeacherLevelMapper nodeTeacherLevelMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBatch(BatchCreateDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long schoolId = getCurrentSchoolId();

        Batch batch = batchConvert.toEntity(dto);
        batch.setSchoolId(schoolId);
        batch.setCreatedBy(currentUserId);
        batch.setStatus("DRAFT");
        batch.setTotalStudents(0);

        batchMapper.insert(batch);

        log.info("[批次创建] batchId={}, name={}, operator={}", batch.getId(), batch.getName(), currentUserId);
        return batch.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatch(Long batchId, BatchUpdateDTO dto) {
        Long schoolId = getCurrentSchoolId();

        Batch existing = getBatchOrThrow(batchId, schoolId);
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new PaperException(2011, "仅草稿状态批次可编辑");
        }

        Batch update = batchConvert.toEntity(dto);
        update.setId(batchId);

        batchMapper.updateById(update);

        log.info("[批次更新] batchId={}, operator={}", batchId, StpUtil.getLoginIdAsLong());
    }

    @Override
    public BatchDetailVO getBatchDetail(Long batchId) {
        Long schoolId = getCurrentSchoolId();
        Batch batch = getBatchOrThrow(batchId, schoolId);
        return batchConvert.toDetailVO(batch);
    }

    @Override
    public IPage<BatchVO> pageBatches(BatchQueryDTO query) {
        Long schoolId = getCurrentSchoolId();

        Page<Batch> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Batch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Batch::getSchoolId, schoolId)
                .eq(Batch::getIsDeleted, 0)
                .eq(query.getStatus() != null && !query.getStatus().isEmpty(),
                        Batch::getStatus, query.getStatus())
                .eq(query.getPaperType() != null && !query.getPaperType().isEmpty(),
                        Batch::getPaperType, query.getPaperType())
                .like(query.getKeyword() != null && !query.getKeyword().isEmpty(),
                        Batch::getName, query.getKeyword())
                .orderByDesc(Batch::getCreatedAt);

        IPage<Batch> entityPage = batchMapper.selectPage(page, wrapper);
        return entityPage.convert(batchConvert::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long batchId) {
        Long schoolId = getCurrentSchoolId();

        Batch batch = getBatchOrThrow(batchId, schoolId);
        if (!"DRAFT".equals(batch.getStatus())) {
            throw new PaperException(2011, "仅草稿状态批次可删除");
        }

        batchMapper.deleteById(batchId);

        log.info("[批次删除] batchId={}, operator={}", batchId, StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchAddStudentsResultVO addStudents(Long batchId, BatchAddStudentsDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Long schoolId = getCurrentSchoolId();

        Batch batch = getBatchOrThrow(batchId, schoolId);

        List<Long> studentIds = dto.getStudentIds();
        if (studentIds == null || studentIds.isEmpty()) {
            return new BatchAddStudentsResultVO(0, 0, Collections.emptyList());
        }

        List<String> skipReasons = new ArrayList<>();
        int addedCount = 0;
        int skippedCount = 0;

        for (Long studentId : studentIds) {
            boolean success = processSingleStudent(batch, studentId, schoolId, currentUserId, skipReasons);
            if (success) {
                addedCount++;
            } else {
                skippedCount++;
            }
        }

        if (addedCount > 0) {
            batchMapper.update(null,
                    new LambdaUpdateWrapper<Batch>()
                            .setSql("total_students = total_students + " + addedCount)
                            .eq(Batch::getId, batchId)
            );
        }

        log.info("[批次添加学生] batchId={}, added={}, skipped={}, operator={}",
                batchId, addedCount, skippedCount, currentUserId);

        return new BatchAddStudentsResultVO(addedCount, skippedCount, skipReasons);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchStatus(Long batchId, String targetStatus) {
        Long schoolId = getCurrentSchoolId();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        Batch batch = getBatchOrThrow(batchId, schoolId);
        String currentStatus = batch.getStatus();

        boolean validTransition = switch (currentStatus) {
            case "DRAFT" -> "ACTIVE".equals(targetStatus);
            case "ACTIVE" -> "FINISHED".equals(targetStatus);
            case "FINISHED" -> "ARCHIVED".equals(targetStatus);
            default -> false;
        };

        if (!validTransition) {
            throw new PaperException(2011,
                    String.format("批次状态不允许从 %s 切换到 %s", currentStatus, targetStatus));
        }

        batchMapper.update(null,
                new LambdaUpdateWrapper<Batch>()
                        .set(Batch::getStatus, targetStatus)
                        .eq(Batch::getId, batchId)
        );

        log.info("[批次状态变更] batchId={}, {} -> {}, operator={}",
                batchId, currentStatus, targetStatus, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyBatch(Long batchId) {
        Long schoolId = getCurrentSchoolId();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        Batch sourceBatch = getBatchOrThrow(batchId, schoolId);

        Batch newBatch = new Batch();
        newBatch.setSchoolId(schoolId);
        newBatch.setName(sourceBatch.getName() + "（复制）");
        newBatch.setAcademicYear(sourceBatch.getAcademicYear());
        newBatch.setSemester(sourceBatch.getSemester());
        newBatch.setPaperType(sourceBatch.getPaperType());
        newBatch.setStartTime(sourceBatch.getStartTime());
        newBatch.setEndTime(sourceBatch.getEndTime());
        newBatch.setStatus("DRAFT");
        newBatch.setDescription(sourceBatch.getDescription());
        newBatch.setTotalStudents(0);
        newBatch.setCreatedBy(currentUserId);
        batchMapper.insert(newBatch);

        copyFlowConfigs(batchId, newBatch.getId(), schoolId);

        log.info("[批次复制] sourceBatchId={}, newBatchId={}, operator={}",
                batchId, newBatch.getId(), currentUserId);

        return newBatch.getId();
    }

    private Batch getBatchOrThrow(Long batchId, Long schoolId) {
        Batch batch = batchMapper.selectById(batchId);
        if (batch == null || batch.getIsDeleted() == 1 || !batch.getSchoolId().equals(schoolId)) {
            throw new PaperException(ResultCode.NOT_FOUND, "批次不存在");
        }
        return batch;
    }

    private Long getCurrentSchoolId() {
        Object schoolIdObj = StpUtil.getSession().get("schoolId");
        if (schoolIdObj == null) {
            throw new PaperException(ResultCode.UNAUTHORIZED, "无法获取学校信息");
        }
        return Long.valueOf(schoolIdObj.toString());
    }

    private boolean processSingleStudent(Batch batch, Long studentId,
                                         Long schoolId, Long currentUserId,
                                         List<String> skipReasons) {
        Long exists = studentBatchMapper.selectCount(
                new LambdaQueryWrapper<StudentBatch>()
                        .eq(StudentBatch::getStudentId, studentId)
                        .eq(StudentBatch::getBatchId, batch.getId())
                        .eq(StudentBatch::getIsDeleted, 0)
        );
        if (exists > 0) {
            skipReasons.add("学生ID " + studentId + " 已在该批次中");
            return false;
        }

        FeignUserVO userInfo = fetchUserInfo(studentId);
        if (userInfo == null) {
            skipReasons.add("学生ID " + studentId + " 不存在");
            return false;
        }
        if (!"STUDENT".equals(userInfo.getUserType())) {
            skipReasons.add("用户ID " + studentId + " 不是学生");
            return false;
        }

        StudentBatch sb = new StudentBatch();
        sb.setStudentId(studentId);
        sb.setBatchId(batch.getId());
        sb.setSchoolId(schoolId);
        sb.setMajor(userInfo.getMajor());
        sb.setTeachingPointId(userInfo.getTeachingPointId());
        sb.setEnrolledBy(currentUserId);
        studentBatchMapper.insert(sb);

        Paper paper = new Paper();
        paper.setSchoolId(schoolId);
        paper.setBatchId(batch.getId());
        paper.setStudentId(studentId);
        paper.setTeachingPointId(userInfo.getTeachingPointId());
        paper.setMajor(userInfo.getMajor());
        paper.setPaperType(batch.getPaperType());
        paper.setOverallStatus("NOT_STARTED");
        paper.setWordCount(0);
        paper.setSignConfirmed(0);
        paper.setTopicConfirmed(0);
        paper.setOutlineConfirmed(0);
        paper.setDraftConfirmed(0);
        paper.setFinalDraftConfirmed(0);
        paper.setFinalConfirmed(0);
        paperMapper.insert(paper);

        return true;
    }

    private FeignUserVO fetchUserInfo(Long userId) {
        try {
            Result<FeignUserVO> result = userServiceClient.getUserById(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("[查询用户信息失败] userId={}, cause={}", userId, e.getMessage());
        }
        return null;
    }

    private void copyFlowConfigs(Long sourceBatchId, Long newBatchId, Long schoolId) {
        List<BatchFlowConfig> configs = batchFlowConfigMapper.selectList(
                new LambdaQueryWrapper<BatchFlowConfig>()
                        .eq(BatchFlowConfig::getBatchId, sourceBatchId)
                        .eq(BatchFlowConfig::getIsDeleted, 0)
        );

        Map<Long, Long> configIdMapping = new HashMap<>();
        for (BatchFlowConfig config : configs) {
            Long oldConfigId = config.getId();
            config.setId(null);
            config.setBatchId(newBatchId);
            config.setSchoolId(schoolId);
            batchFlowConfigMapper.insert(config);
            configIdMapping.put(oldConfigId, config.getId());
        }

        List<NodeTeacherLevel> levels = nodeTeacherLevelMapper.selectList(
                new LambdaQueryWrapper<NodeTeacherLevel>()
                        .eq(NodeTeacherLevel::getBatchId, sourceBatchId)
                        .eq(NodeTeacherLevel::getIsDeleted, 0)
        );

        for (NodeTeacherLevel level : levels) {
            level.setId(null);
            level.setBatchId(newBatchId);
            Long newConfigId = configIdMapping.get(level.getConfigId());
            if (newConfigId != null) {
                level.setConfigId(newConfigId);
            }
            nodeTeacherLevelMapper.insert(level);
        }
    }
}

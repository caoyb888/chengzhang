package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 论文主表实体
 */
@Data
@TableName("paper")
public class Paper implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学校ID */
    private Long schoolId;

    /** 批次ID */
    private Long batchId;

    /** 学生ID */
    private Long studentId;

    /** 教学点ID */
    private Long teachingPointId;

    /** 专业 */
    private String major;

    /** 论文类型 */
    private String paperType;

    /** 当前节点 */
    private String currentNode;

    /** 整体状态 */
    private String overallStatus;

    /** 字数 */
    private Integer wordCount;

    /** 电子签名确认 */
    private Integer signConfirmed;

    /** 选题确认 */
    private Integer topicConfirmed;

    /** 大纲确认 */
    private Integer outlineConfirmed;

    /** 初稿确认 */
    private Integer draftConfirmed;

    /** 定稿确认 */
    private Integer finalDraftConfirmed;

    /** 终稿确认 */
    private Integer finalConfirmed;

    /** 总分 */
    private BigDecimal totalScore;

    /** 评分等级 */
    private String scoreLevel;

    /** 导出文件URL */
    private String exportUrl;

    /** 导出时间 */
    private LocalDateTime exportAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

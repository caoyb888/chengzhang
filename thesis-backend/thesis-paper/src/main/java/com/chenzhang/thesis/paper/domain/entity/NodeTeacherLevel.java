package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 节点导师层级配置实体
 */
@Data
@TableName("node_teacher_level")
public class NodeTeacherLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置ID */
    private Long configId;

    /** 批次ID */
    private Long batchId;

    /** 层级 */
    private Integer level;

    /** 层级名称 */
    private String levelName;

    /** 允许退回 */
    private Integer allowReject;

    /** 评审模式 */
    private String reviewMode;

    /** 抽检比例 */
    private BigDecimal sampleRate;

    /** 推送学生 */
    private Integer pushToStudent;

    /** 是否末级 */
    private Integer isFinalLevel;

    /** 超时自动通过小时数 */
    private Integer autoApproveHours;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次流程节点配置实体
 */
@Data
@TableName("batch_flow_config")
public class BatchFlowConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID */
    private Long batchId;

    /** 学校ID */
    private Long schoolId;

    /** 节点类型 */
    private String nodeType;

    /** 节点名称 */
    private String nodeName;

    /** 排序 */
    private Integer sortOrder;

    /** 是否启用 */
    private Integer isEnabled;

    /** 是否强制 */
    private Integer isRequired;

    /** 是否需要指导 */
    private Integer needGuide;

    /** 是否需要审核 */
    private Integer needReview;

    /** 允许重新提交 */
    private Integer allowResubmit;

    /** 最大重新提交次数 */
    private Integer maxResubmitCount;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 审核截止时间 */
    private LocalDateTime reviewDeadline;

    /** 最低评语字数 */
    private Integer minCommentLength;

    /** 说明 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

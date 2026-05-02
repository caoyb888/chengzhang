package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生批次关联实体
 */
@Data
@TableName("student_batch")
public class StudentBatch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生用户ID */
    private Long studentId;

    /** 批次ID */
    private Long batchId;

    /** 所属学校ID */
    private Long schoolId;

    /** 专业 */
    private String major;

    /** 教学点ID */
    private Long teachingPointId;

    /** 添加操作人ID */
    private Long enrolledBy;

    /** 加入批次时间 */
    private LocalDateTime enrolledAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}

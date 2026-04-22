package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 师生指导关系实体
 */
@Data
@TableName("teaching_relationship")
public class TeachingRelationship implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学校ID */
    private Long schoolId;

    /** 所属批次ID */
    private Long batchId;

    /** 学生用户ID */
    private Long studentId;

    /** 教师用户ID */
    private Long teacherId;

    /** 教师类型（MAIN-指导教师 ASSIST-辅助指导教师） */
    private String teacherType;

    /** 指导层级 */
    private Integer level;

    /** 教学点ID */
    private Long teachingPointId;

    /** 分配方式（IMPORT-模板导入 MANUAL-手工单个匹配） */
    private String assignType;

    /** 分配操作人ID */
    private Long assignedBy;

    /** 是否有效（0-已解除 1-有效） */
    private Integer isActive;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除（0-正常 1-已删除） */
    @TableLogic
    private Integer isDeleted;
}

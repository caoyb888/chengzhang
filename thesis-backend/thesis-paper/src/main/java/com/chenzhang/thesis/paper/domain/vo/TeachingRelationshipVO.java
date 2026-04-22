package com.chenzhang.thesis.paper.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 师生指导关系出参
 */
@Data
public class TeachingRelationshipVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 所属学校ID */
    private Long schoolId;

    /** 所属批次ID */
    private Long batchId;

    /** 学生用户ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 教师用户ID */
    private Long teacherId;

    /** 教师姓名 */
    private String teacherName;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

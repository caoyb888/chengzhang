package com.chenzhang.thesis.paper.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 单个创建指导关系入参
 */
@Data
public class TeachingRelationshipDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 所属学校ID */
    @NotNull(message = "学校ID不能为空")
    private Long schoolId;

    /** 所属批次ID */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    /** 学生用户ID */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /** 教师用户ID */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /** 教师类型（MAIN-指导教师 ASSIST-辅助指导教师） */
    @NotBlank(message = "教师类型不能为空")
    private String teacherType;

    /** 指导层级 */
    @NotNull(message = "指导层级不能为空")
    private Integer level;

    /** 教学点ID */
    private Long teachingPointId;

    /** 分配方式（IMPORT-模板导入 MANUAL-手工单个匹配） */
    @NotBlank(message = "分配方式不能为空")
    private String assignType;
}

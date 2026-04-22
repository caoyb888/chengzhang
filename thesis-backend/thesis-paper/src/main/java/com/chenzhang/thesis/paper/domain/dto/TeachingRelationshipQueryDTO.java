package com.chenzhang.thesis.paper.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 指导关系查询条件
 */
@Data
public class TeachingRelationshipQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 页码（从1开始） */
    private Integer pageNum = 1;

    /** 每页条数（默认20，最大100） */
    private Integer pageSize = 20;

    /** 所属批次ID */
    private Long batchId;

    /** 学生用户ID */
    private Long studentId;

    /** 教师用户ID */
    private Long teacherId;

    /** 教师类型（MAIN-指导教师 ASSIST-辅助指导教师） */
    private String teacherType;

    /** 是否有效（0-已解除 1-有效） */
    private Integer isActive;
}

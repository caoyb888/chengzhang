package com.chenzhang.thesis.paper.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次实体
 */
@Data
@TableName("batch")
public class Batch implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学校ID */
    private Long schoolId;

    /** 批次名称 */
    private String name;

    /** 学年 */
    private String academicYear;

    /** 学期 */
    private String semester;

    /** 论文类型 */
    private String paperType;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态 */
    private String status;

    /** 说明 */
    private String description;

    /** 学生总人数 */
    private Integer totalStudents;

    /** 创建人 */
    private Long createdBy;

    /** 归档时间 */
    private LocalDateTime archivedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;
}
